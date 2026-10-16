package com.example.csv_project.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    FILE_EMPTY("FILE_001", HttpStatus.BAD_REQUEST, "파일이 비어 있습니다."),
    INVALID_FILE_TYPE("FILE_002", HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다."),
    FILE_PROCESS_ERROR("FILE_003", HttpStatus.INTERNAL_SERVER_ERROR, "파일 처리 중 오류가 발생했습니다."),
    ZIP_INVALID_PATH("FILE_004", HttpStatus.BAD_REQUEST, "잘못된 경로의 ZIP 엔트리가 감지되었습니다.");


    private final String code;
    private final HttpStatus status;
    private final String message;
}
