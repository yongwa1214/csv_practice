package com.example.csv_project.common;


import com.example.csv_project.common.error.CustomException;
import com.example.csv_project.common.error.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;

@Component
public class MyFileUtils {
    private final String uploadPath;

    public MyFileUtils(
            @Value("${constants.file.directory}") String uploadPath) {
        this.uploadPath = uploadPath;
    }

    // fileTitle 이름으로 폴더 생성
    public Path makeFolders(String fileTitle) {
        File folder = new File(uploadPath);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        folder = new File(uploadPath, fileTitle);

        if (folder.exists()) {
            throw new CustomException(ErrorCode.FOLDER_ALREADY_EXISTS);
        }

        folder.mkdirs();

        return folder.toPath();
    }

    public String getUploadPath() {
        return uploadPath;
    }
}
