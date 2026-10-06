package pl.psnc.vtl.ocrtraining.domain.common.exception;

import lombok.Getter;


@Getter
public class VtlException extends Exception {
    private ErrorCode errorCode;
    private Object[] msgArgs;

    public VtlException(ErrorCode errorCode, Object... msgArgs) {
        super(errorCode.getMessage(msgArgs));

        this.errorCode = errorCode;
        this.msgArgs = msgArgs;
    }

    public VtlException(ErrorCode errorCode, Throwable cause, Object... msgArgs) {
        super(errorCode.getMessage(msgArgs), cause);

        this.errorCode = errorCode;
        this.msgArgs = msgArgs;
    }

    @Override
    public final String getMessage() {
        return errorCode.getMessage(msgArgs);
    }
}
