package com.zjrcu.iras.bi.platform.domain.dto;

import java.io.Serializable;
import java.util.Map;

/**
 * API数据源响应结果
 *
 * @author iras
 */
public class ApiResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * HTTP状态码
     */
    private int statusCode;

    /**
     * 响应头
     */
    private Map<String, String> headers;

    /**
     * 响应体（JSON字符串）
     */
    private String body;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 请求耗时（毫秒）
     */
    private long duration;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, int statusCode, String body) {
        this.success = success;
        this.statusCode = statusCode;
        this.body = body;
    }

    public static ApiResponse success(int statusCode, String body, long duration) {
        ApiResponse response = new ApiResponse(true, statusCode, body);
        response.setDuration(duration);
        return response;
    }

    public static ApiResponse failure(String errorMessage) {
        ApiResponse response = new ApiResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", statusCode=" + statusCode +
                ", body='" + (body != null ? body.substring(0, Math.min(100, body.length())) : null) + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", duration=" + duration +
                '}';
    }
}
