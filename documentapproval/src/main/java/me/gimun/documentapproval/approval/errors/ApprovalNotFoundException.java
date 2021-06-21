package me.gimun.documentapproval.approval.errors;


public class ApprovalNotFoundException extends RuntimeException {
    public ApprovalNotFoundException() {
    }

    public ApprovalNotFoundException(String message) {
        super(message);
    }

    public ApprovalNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApprovalNotFoundException(Throwable cause) {
        super(cause);
    }

    public ApprovalNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}