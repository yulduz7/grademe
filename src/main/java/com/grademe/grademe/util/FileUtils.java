package com.grademe.grademe.util;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    public static void copyFile(File origin, File copy)
    {
        try (
                InputStream in = new BufferedInputStream(
                        new FileInputStream(origin));
                OutputStream out = new BufferedOutputStream(
                        new FileOutputStream(copy))) {

            byte[] buffer = new byte[1024];
            int lengthRead;
            while ((lengthRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, lengthRead);
                out.flush();
            }
        }catch(IOException e){
            e.printStackTrace();
            throw new RuntimeException("Failed in copyFile()");
        }
    }

    public static void unzip(String source, String destination){
        try {
            ZipFile zipFile = new ZipFile(source);
            zipFile.extractAll(destination);
            System.out.println("Successfully unzipped: "+destination);
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }

    public static void unzip(String source, String destination, String password){
        try {
            ZipFile zipFile = new ZipFile(source);
            if (zipFile.isEncrypted()) {
                zipFile.setPassword(password.toCharArray());
            }
            zipFile.extractAll(destination);
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }

    public static void writeStream(InputStream inputStream, File outputFile){
        try (
                InputStream in = inputStream;
                OutputStream out = new BufferedOutputStream(
                        new FileOutputStream(outputFile))) {

            byte[] buffer = new byte[1024];
            int lengthRead;
            while ((lengthRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, lengthRead);
                out.flush();
            }
        }catch(IOException e){
            e.printStackTrace();
            throw new RuntimeException("Failed in writeStream(): "+e.getMessage());
        }
    }

    public static void writeString(String content, File outputFile) {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))){
            writer.write(content);
        }catch (IOException e){
            e.printStackTrace();
            throw new RuntimeException("Failed in writeMethod(): "+e.getMessage());
        }

    }


    public static void deleteAll( File dir) {
        if(dir.delete()) {

        }else {
            File[] files = dir.listFiles();
            for(File file : files) {
                deleteAll(file);
            }
            dir.delete();
        }
    }

    public static void replaceFile(File newFile, File oldFile){
        if(oldFile.exists()){
            oldFile.delete();
        }

        try(
                InputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(newFile));
                OutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(oldFile))
        ){

            byte[] bytes = new byte[1024];
            int length;
            while ((length = bufferedInputStream.read(bytes)) > 0){
                bufferedOutputStream.write(bytes, 0, length);
                bufferedOutputStream.flush();
            }

        }catch (IOException e){
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public static List<String> getFileContent(String path){
        List<String> content = new ArrayList<>();
        File file = new File(path);
        try(BufferedReader reader = new BufferedReader(new FileReader(file))){
            String line;
            while((line = reader.readLine()) != null){
                content.add(line);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return content;
    }

}
