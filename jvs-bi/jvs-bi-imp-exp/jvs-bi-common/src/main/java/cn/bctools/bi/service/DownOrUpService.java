package cn.bctools.bi.service;

import cn.bctools.bi.dto.DownIdTypeDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.hutool.core.io.FileUtil;

import java.util.List;
import java.util.Map;

/**
 * 上传与下载公共类
 */
public interface DownOrUpService {
    /**
     * 通过不同的类型id 获取所有关联id 用于后续打包文件
     *
     * @param id 数据id
     * @return 所有关联id
     */
    List<DownIdTypeDto> downGetId(String id, Boolean isMock);

    /**
     * 通过不同的类型id 获取打包后的文件信息
     *
     * @param id 数据id
     * @return 返回文件信息
     */
    void downFile(List<String> id, Boolean isMock);

    /**
     * 获取json文件路径
     */
    String getJsonPath();

    /**
     * 替换数据源id
     *
     * @param mapId    需要替换的id与新id
     * @param filePath 当前需要替换的文件路径
     */
    default void replaceDataSourceId(List<Map<String, String>> mapId, String filePath) {
        String value = FileUtil.readUtf8String(filePath);
        for (int i = 0; i < mapId.size(); i++) {
            Map<String, String> map = mapId.get(i);
            for (String key : map.keySet()) {
                String newId = map.get(key);
                value = value.replaceAll(key, newId);
            }
        }
        //重新写入
        FileUtil.writeUtf8String(value, filePath);
    }

    /**
     * 上传文件的设计逻辑
     *
     * @param nameOrId 菜单名称或者菜单id
     * @return 当前设计的名称  只有大屏 图表 报表会返回
     */
    String up(String nameOrId, UserDto userDto);

}
