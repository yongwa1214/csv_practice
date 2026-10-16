package com.example.csv_project;


import com.example.csv_project.common.MyFileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class CsvController {

    private final CsvService csvService;

    // 기능
    @PostMapping("/api/files/upload")
    public ResponseEntity<?> sendZip (@RequestPart MultipartFile file) throws IOException {
        csvService.processZipFile(file); // 활성화는 이쪽
        return ResponseEntity.ok("完了しました");
    }

    //html 읽기
    @GetMapping("/")
    public String uploadPage() {
        return "upload";
    }


}
