package com.example.csv_project;

import com.example.csv_project.common.MyFileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TestApi {

    @Autowired
    private MyFileUtils myFileUtils;

    @Test
    void test1() {
        String fileTitle = "testFolder";

        myFileUtils.makeFolders(fileTitle);
    }
}