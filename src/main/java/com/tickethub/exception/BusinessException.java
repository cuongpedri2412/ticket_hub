package com.tickethub.exception;

/**
 * Exception cho lỗi nghiệp vụ (vi phạm quy tắc của hệ thống).
 * Kế thừa RuntimeException để không phải khai báo "throws" ở mọi nơi
 * và để @Transactional tự rollback khi lỗi xảy ra.
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode.defaultMessage());
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
