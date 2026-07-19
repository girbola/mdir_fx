package com.girbola.messages;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ErrorMessage {
    private String group;
    private String message;

    public ErrorMessage(String group, String message) {
        this.group=group;
        this.message=message;
    }
}
