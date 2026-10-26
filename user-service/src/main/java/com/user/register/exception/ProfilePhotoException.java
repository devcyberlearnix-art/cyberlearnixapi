package com.user.register.exception;

public class ProfilePhotoException extends RuntimeException {
    private final ErrorCode errorCode;

    public enum ErrorCode {
        FILE_EMPTY("PHOTO_001", "No file uploaded"),
        FILE_TOO_LARGE("PHOTO_002", "File size exceeds 5MB limit"),
        INVALID_FILE_TYPE("PHOTO_003", "Only JPG, PNG, or WEBP files are allowed"),
        UPLOAD_FAILED("PHOTO_004", "Failed to upload photo to storage"),
        USER_NOT_FOUND("PHOTO_005", "User not found"),
        INVALID_IMAGE("PHOTO_006", "Invalid image file"),
        NO_PHOTO_TO_REMOVE("PHOTO_007", "No profile photo to remove");

        private final String code;
        private final String message;

        ErrorCode(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    public ProfilePhotoException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ProfilePhotoException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }

    public ProfilePhotoException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
