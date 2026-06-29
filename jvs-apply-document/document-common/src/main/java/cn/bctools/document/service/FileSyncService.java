package cn.bctools.document.service;


import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.document.dto.FileSyncDto;
import cn.bctools.document.entity.DcLibrary;

import java.util.List;

/**
 * @Author: ZhuXiaoKang
 * @Description: 文件同步
 */
public interface FileSyncService {

    /**
     * 文件同步
     *
     * @param fileSyncDto 同步入参
     * @param userDto     当前用户
     */
    void fileSync(FileSyncDto fileSyncDto, UserDto userDto);

    /**
     * 获取文件夹
     *
     * @return 所有文件夹
     */
    List<String> getFolder();
}
