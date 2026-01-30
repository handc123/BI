package com.zjrcu.iras.bi.platform.service;

import java.io.Serializable;

/**
 * 导出任务状态
 *
 * @author iras
 */
public class ExportTaskStatus implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务状态: PENDING, RUNNING, COMPLETED, FAILED
     */
    private String status;

    /**
     * 进度百分比(0-100)
     */
    private int progress;

    /**
     * 错误消息(如果失败)
     */
    private String errorMessage;

    /**
     * 下载URL(如果完成)
     */
    private String downloadUrl;

    /**
     * 创建时间
     */
    private long createTime;

    /**
     * 完成时间
     */
    private long completeTime;

    public ExportTaskStatus() {
    }

    public ExportTaskStatus(String taskId, String status) {
        this.taskId = taskId;
        this.status = status;
        this.createTime = System.currentTimeMillis();
    }

    public static ExportTaskStatus pending(String taskId) {
        return new ExportTaskStatus(taskId, "PENDING");
    }

    public static ExportTaskStatus running(String taskId, int progress) {
        ExportTaskStatus status = new ExportTaskStatus(taskId, "RUNNING");
        status.setProgress(progress);
        return status;
    }

    public static ExportTaskStatus completed(String taskId, String downloadUrl) {
        ExportTaskStatus status = new ExportTaskStatus(taskId, "COMPLETED");
        status.setProgress(100);
        status.setDownloadUrl(downloadUrl);
        status.setCompleteTime(System.currentTimeMillis());
        return status;
    }

    public static ExportTaskStatus failed(String taskId, String errorMessage) {
        ExportTaskStatus status = new ExportTaskStatus(taskId, "FAILED");
        status.setErrorMessage(errorMessage);
        status.setCompleteTime(System.currentTimeMillis());
        return status;
    }

    // Getters and Setters

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(long completeTime) {
        this.completeTime = completeTime;
    }

    @Override
    public String toString() {
        return "ExportTaskStatus{" +
                "taskId='" + taskId + '\'' +
                ", status='" + status + '\'' +
                ", progress=" + progress +
                ", errorMessage='" + errorMessage + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", createTime=" + createTime +
                ", completeTime=" + completeTime +
                '}';
    }
}
