package com.example.csv_project.common;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class MyFileUtils {
    private final String uploadPath;

    public MyFileUtils(@Value("${constants.file.directory}") String uploadPath) {
        this.uploadPath = uploadPath;
        System.out.println("Upload Path: " + uploadPath);
    }



    // 기본 저장 경로 반환
    public String getUploadPath() {
        return uploadPath;
    }

    // 폴더 생성
    public void makeFolders(String path) throws IOException {
        Files.createDirectories(Paths.get(uploadPath, path));
    }

    // 저장 경로 반환
    public Path getPath(String path) {
        return Paths.get(uploadPath, path);
    }
}
