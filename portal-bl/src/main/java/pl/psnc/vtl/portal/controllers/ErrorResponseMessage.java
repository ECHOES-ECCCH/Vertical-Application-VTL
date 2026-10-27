package pl.psnc.vtl.portal.controllers;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class ErrorResponseMessage {

    private Map<String, String> messages = new HashMap<>();

    public ErrorResponseMessage(String fieldName, String errorCode) {
        messages.put(fieldName, errorCode);
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public void add(String fieldName, String errorCode){
        messages.put(fieldName,errorCode);
    }
}
