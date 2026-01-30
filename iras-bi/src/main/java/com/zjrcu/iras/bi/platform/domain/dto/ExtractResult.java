package com.zjrcu.iras.bi.platform.domain.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据抽取结果
 *
 * @author iras
 */
public class ExtractResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 抽取行数
     */
    private long rowCount;

    /**
     * 执行时间(毫秒)
     */
    private long executionTime;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 重试次数
     */
    private int retryCount;

    public ExtractResult() {
    }

    public ExtractResult(boolean success, long rowCount) {
        this.success = success;
        this.rowCount = rowCount;
    }

    /**
     * 创建成功结果
     *
     * @param rowCount 抽取行数
     * @return 抽取结果
     */
    public static ExtractResult success(long rowCount) {
        ExtractResult result = new ExtractResult();
        result.setSuccess(true);
        result.setRowCount(rowCount);
        result.setEndTime(new Date());
        return result;
    }

    /**
     * 创建失败结果
     *
     * @param errorMessage 错误消息
     * @return 抽取结果
     */
    public static ExtractResult failure(String errorMessage) {
        ExtractResult result = new ExtractResult();
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        result.setEndTime(new Date());
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public long getRowCount() {
        return rowCount;
    }

    public void setRowCount(long rowCount) {
        this.rowCount = rowCount;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    @Override
    public String toString() {
        return "ExtractResult{" +
                "success=" + success +
                ", rowCount=" + rowCount +
                ", executionTime=" + executionTime +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", errorMessage='" + errorMessage + '\'' +
                ", retryCount=" + retryCount +
                '}';
    }
}
