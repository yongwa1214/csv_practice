package com.example.csv_project.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    FILE_EMPTY("FILE_001", HttpStatus.BAD_REQUEST, "파일이 비어 있습니다."),
    INVALID_FILE_TYPE("FILE_002", HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다."),
    FILE_PROCESS_ERROR("FILE_003", HttpStatus.INTERNAL_SERVER_ERROR, "파일 처리에 실패했습니다. 잠시 후 다시 시도해 주세요."),
    ZIP_INVALID_PATH("FILE_004", HttpStatus.BAD_REQUEST, "압축 파일을 처리할 수 없습니다."),

    FILE_READ_ERROR("FILE_005", HttpStatus.INTERNAL_SERVER_ERROR, "파일 내용을 확인할 수 없습니다. 파일 상태를 확인해 주세요."),
    FOLDER_ALREADY_EXISTS("FILE_006", HttpStatus.BAD_REQUEST, "이미 존재하는 파일명입니다.")
    ;

    private final String code;
    private final HttpStatus status;
    private final String message;
}