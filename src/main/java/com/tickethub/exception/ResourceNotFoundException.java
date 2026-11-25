package com.tickethub.exception;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resource, Object id) {
        super(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy " + resource + " id=" + id);
    }
}
