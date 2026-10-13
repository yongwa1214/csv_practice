package com.example.csv_project;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class CsvController {

    private final CsvService csvService;

    // test csv하나만 읽습니다.
    @PostMapping("api/csv/zip")
    public ResponseEntity<?> sendZip (@RequestPart MultipartFile file){
        csvService.testAndFixEncoding(file);
        return ResponseEntity.ok("성공");
    }


}
