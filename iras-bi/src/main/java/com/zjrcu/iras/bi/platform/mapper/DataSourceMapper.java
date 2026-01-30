package com.zjrcu.iras.bi.platform.mapper;

import com.zjrcu.iras.bi.platform.domain.DataSource;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DataSourceMapper {

    DataSource selectDataSourceById(Long id);

    List<DataSource> selectDataSourceList(DataSource dataSource);

    int insertDataSource(DataSource dataSource);

    int updateDataSource(DataSource dataSource);

    int deleteDataSourceById(Long id);

    int deleteDataSourceByIds(Long[] ids);
}
