package com.example.csv_project;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CsvController {

    private final CsvService csvService;

    @PostMapping("api/csv/zip")
    public ResponseEntity<?> sendZip (@RequestBody String aa){
        return ResponseEntity.ok("성공");
    }


}
