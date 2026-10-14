package com.example.csv_project.common;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;


@Component
public class MyFileUtils {
    private final String uploadPath;

    public MyFileUtils(
            @Value("${constants.file.directory}") String uploadPath) {
        this.uploadPath = uploadPath;
    }

    // fileTitle 이름으로 폴더 생성
    public void makeFolders(String fileTitle) {
        File folder = new File(uploadPath, fileTitle);

        if (!folder.exists()) {
            folder.mkdirs();
        }
    }
}
