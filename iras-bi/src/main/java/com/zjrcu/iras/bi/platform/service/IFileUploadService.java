package com.zjrcu.iras.bi.platform.service;

import com.zjrcu.iras.bi.platform.domain.dto.FileUploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传服务接口
 *
 * @author iras
 */
public interface IFileUploadService {

    /**
     * 上传文件并创建文件数据源
     *
     * @param file 上传的文件
     * @param name 数据源名称
     * @param remark 备注
     * @return 文件上传结果
     */
    FileUploadResult uploadFile(MultipartFile file, String name, String remark);

    /**
     * 预览文件数据源数据
     *
     * @param dataSourceId 数据源ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 预览数据
     */
    List<List<Object>> previewFileData(Long dataSourceId, int pageNum, int pageSize);

    /**
     * 删除文件数据源及其文件
     *
     * @param dataSourceId 数据源ID
     * @return 是否成功
     */
    boolean deleteFileDataSource(Long dataSourceId);

    /**
     * 清理超过90天未使用的文件数据源
     *
     * @return 清理的数据源数量
     */
    int cleanupUnusedFileSources();
}
