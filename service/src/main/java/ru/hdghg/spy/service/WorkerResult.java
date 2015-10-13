package ru.hdghg.spy.service;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class WorkerResult {
    private boolean status;
    private String errorMessage;

    public WorkerResult(boolean status, String errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
    }
}
