package com.irontomato.siteclone.controller;

public class ActionResult<T> {
    private boolean success;

    private int code;

    private String message;

    private T data;

    public ActionResult() {
    }

    public ActionResult(boolean success, int code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> ActionResult<T> success(T data){
        return new ActionResult<>(true, 0, "", data);
    }

    public static ActionResult<?> fail(int code, String message) {
        return new ActionResult<>(false, code, message, null);
    }
}
