package com.grademe.grademe.server;

import com.grademe.grademe.beans.Report;
import com.grademe.grademe.beans.TestCase;
import com.grademe.grademe.util.FileUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class EvaluationEngine {

    private boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

    public void evaluateProject(File project) {
        try {
            ProcessBuilder builder = new ProcessBuilder();
            if (isWindows) {
                System.out.println("Evaluation in Windows:");
                builder.command("cmd.exe", "/c", "mvn clean test");
            } else {
                System.out.println("Evaluation in Linux:");
                builder.command("sh", "-c", "mvn clean test");
            }

            builder.directory(project);
            Process process = builder.start();
            streamResults(process.getInputStream());
            process.destroy();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

    }

    private void streamResults(InputStream inputStream){
        try{
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            inputStream.close();
            inputStreamReader.close();
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Report getReportObject(File project,long projectId){
        try{
            System.out.println("Getting Reports..");
            //Get Test class content and revers lines
            List<String> testClassContent = FileUtils.getFileContent(project.getAbsolutePath()+"/src/test/java/TestProject.java");
            Collections.reverse(testClassContent);
            int totalTestCases;
            int passTestCases = 0;
            int grade;

            Report report = new Report();
            report.setProjectId(projectId);
            List<TestCase> testCases = new ArrayList<>();

            File reportXml = new File(project.getAbsolutePath()+"/target/surefire-reports/TEST-TestProject.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(reportXml);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("testcase");
            totalTestCases = nList.getLength();

            for(int i = 0; i < totalTestCases; i++)
            {
                TestCase caseBean = new TestCase();


                Node testcase = nList.item(i);
                if(testcase.hasChildNodes())
                {
                    Element caseEl = (Element)testcase;
                    caseBean.setCaseName(caseEl.getAttribute("name"));
                    caseBean.setPass(false);
                    caseBean.setMessage("FAILED: "+testcase.getTextContent().trim());
                    testCases.add(caseBean);

                }else {
                    passTestCases++;
                    Element caseEl = (Element)testcase;
                    caseBean.setCaseName(caseEl.getAttribute("name"));
                    caseBean.setPass(true);
                    caseBean.setMessage("PASS: ");
                    testCases.add(caseBean);
                }

                String caseName = caseBean.getCaseName().split(Pattern.quote("."))[1];
                System.out.println("CASE NAME: "+caseName);
                caseBean.setCaseDesc(getCaseDescByName(testClassContent, caseName));
            }

            grade = (passTestCases * 100) / totalTestCases;
            report.setGrade(grade);
            report.setTestCases(testCases);
            return report;

        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private String getCaseDescByName(List<String> content, String caseName){
        boolean searchDesc = false;
        for(String line : content){
            if(line.contains("void "+caseName+"()")){
                System.out.println("Setting true..");
                searchDesc = true;
            }
            if(searchDesc && line.contains("@DisplayName")){
                System.out.println("Returning: "+line.split("\"")[1].trim());
                return line.split("\"")[1].trim();
            }
        }
        return caseName;
    }

}
