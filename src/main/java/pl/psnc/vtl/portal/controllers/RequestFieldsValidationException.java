package pl.psnc.vtl.portal.controllers;

import lombok.Getter;

@Getter
public class RequestFieldsValidationException extends RuntimeException{

    private final ErrorResponseMessage errorResponseMessage;

    public RequestFieldsValidationException(ErrorResponseMessage errorResponseMessage){
        super("ValidationException");
        this.errorResponseMessage = errorResponseMessage;

    }


}
