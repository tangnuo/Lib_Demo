package com.caowj.lib_network.bean;


public class HttpBaseResult<T> {
    public static final int STATUS_UnknownHostException = -12;
    public static final int STATUS_NoRouteToHostException = -13;
    public static int STATUS_EXCEPTION = -1;
    public static int STATUS_FAILURE = -2;
    public static int STATUS_NETWORK_UNCONNECTED =-10;
    public static int STATUS_NETWORK_READTIME_OUT =-11;
    public static int STATUS_OK = 200;

    private int status;
    private String code;
    private long timestamp;
    private String message;
    private ResponseResult<T> result;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public ResponseResult<T> getResult() {
        return result;
    }

    public void setResult(ResponseResult<T> result) {
        this.result = result;
    }

    public boolean isSuccess(){
        return status == STATUS_OK;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
