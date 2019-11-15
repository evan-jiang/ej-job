package com.ej.job.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum HttpStatus {
    _200("200", "SUCCESS"), FAIL("FAIL", "FAIL");

    private String code;
    private String status;

    private static final Map<String, HttpStatus> CACHE;

    static {
        CACHE = new HashMap<>();
        HttpStatus[] values = HttpStatus.values();
        for (HttpStatus httpStatus : values) {
            CACHE.put(httpStatus.code, httpStatus);
        }
    }


    HttpStatus(String code, String status) {
        this.code = code;
        this.status = status;
    }

    public static HttpStatus getEnum(String code) {
        if (CACHE.containsKey(code)) {
            return CACHE.get(code);
        }
        return FAIL;
    }

}
