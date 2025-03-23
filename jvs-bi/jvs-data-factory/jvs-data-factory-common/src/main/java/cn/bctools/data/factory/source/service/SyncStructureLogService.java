package cn.bctools.data.factory.source.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.entity.SyncStructureLog;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author admin
 * @description 表结构同步日志
 */
public interface SyncStructureLogService extends IService<SyncStructureLog> {

    /**
     * 对比表结构与保存
     *
     * @param newStructure 新的结构
     * @param oldStructure 历史结构
     * @param dataSourceId 数据源id
     */
    void comparisonAndSave(List<DataSourceStructure> oldStructure, List<DataSourceStructure> newStructure, String dataSourceId, UserDto userDto);


}
