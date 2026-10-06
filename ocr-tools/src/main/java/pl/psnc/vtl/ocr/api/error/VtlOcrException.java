package pl.psnc.vtl.ocr.api.error;

import lombok.Getter;

public abstract class VtlOcrException extends RuntimeException {

    @Getter
    private ErrorCode errorCode;

    private Object[] msgArgs;


    public VtlOcrException(ErrorCode errorCode, Object... msgArgs) {
        super();

        this.errorCode = errorCode;
        this.msgArgs = msgArgs;
    }

    public VtlOcrException(ErrorCode errorCode, Throwable cause, Object... msgArgs) {
        super(cause);

        this.errorCode = errorCode;
        this.msgArgs = msgArgs;
    }

    @Override
    public final String getMessage() {
        return errorCode.getMessage(msgArgs);
    }
}
