package com.zjrcu.iras.bi.platform.domain.dto;

import java.io.Serializable;

/**
 * 数据源连接测试结果
 *
 * @author iras
 */
public class ConnectionTestResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 消息
     */
    private String message;

    /**
     * 连接耗时（毫秒）
     */
    private long duration;

    /**
     * 数据库版本信息（如果可用）
     */
    private String version;

    public ConnectionTestResult() {
    }

    public ConnectionTestResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ConnectionTestResult(boolean success, String message, long duration) {
        this.success = success;
        this.message = message;
        this.duration = duration;
    }

    public static ConnectionTestResult success(String message, long duration) {
        return new ConnectionTestResult(true, message, duration);
    }

    public static ConnectionTestResult success(String message, long duration, String version) {
        ConnectionTestResult result = new ConnectionTestResult(true, message, duration);
        result.setVersion(version);
        return result;
    }

    public static ConnectionTestResult failure(String message) {
        return new ConnectionTestResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "ConnectionTestResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", duration=" + duration +
                ", version='" + version + '\'' +
                '}';
    }
}
