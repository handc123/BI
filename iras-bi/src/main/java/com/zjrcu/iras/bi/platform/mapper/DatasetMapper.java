package com.zjrcu.iras.bi.platform.mapper;

import com.zjrcu.iras.bi.platform.domain.Dataset;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DatasetMapper {

    Dataset selectDatasetById(Long id);

    List<Dataset> selectDatasetList(Dataset dataset);

    int insertDataset(Dataset dataset);

    int updateDataset(Dataset dataset);

    int deleteDatasetById(Long id);

    int deleteDatasetByIds(Long[] ids);
}
