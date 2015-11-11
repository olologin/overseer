package ru.hdghg.model;


public class WorkerResult {
    private boolean status;
    private String errorMessage;

    public WorkerResult(boolean status, String errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return String.format("WorkerResult{status='%s', errorMessage='%s'}", status, errorMessage);
    }
}
