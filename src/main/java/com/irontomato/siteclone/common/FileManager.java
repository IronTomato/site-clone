package com.irontomato.siteclone.common;


import java.io.*;
import java.nio.file.Files;

public class FileManager {

    private String rootDir;

    private Digester digester;

    public FileManager(String rootDir, Digester digester) {
        this.rootDir = rootDir;
        this.digester = digester;
    }

    public String store(byte[] data) {
        String digest = digester.digest(data);
        String filePath = filePath(digest);
        File file = new File(filePath);
        if (!file.exists()) {
            FileOutputStream fos = null;
            try {
                Files.createDirectories(file.getParentFile().toPath());
                file.createNewFile();
                fos = new FileOutputStream(file);
                fos.write(data);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                throw new RuntimeException("Store file error." + filePath);
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        return digest;
    }

    public byte[] getAsBytes(String digest) {
        String filePath = filePath(digest);
        File file = new File(filePath);

        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Read file error. " + filePath);
        }

    }

    public InputStream getAsInputSteam(String digest) throws FileNotFoundException {
        String filePath = filePath(digest);
        File file = new File(filePath);
        if (file.exists()) {
            return new FileInputStream(file);
        } else {
            throw new FileNotFoundException(filePath + " Not Found.");
        }
    }

    private String filePath(String digest) {
        return String.join(File.separator,
                rootDir,
                digest.substring(0, 2),
                digest.substring(2, 4),
                digest.substring(4));
    }
}
