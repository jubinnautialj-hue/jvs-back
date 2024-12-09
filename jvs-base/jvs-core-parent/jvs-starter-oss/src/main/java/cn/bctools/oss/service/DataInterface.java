package cn.bctools.oss.service;

import cn.bctools.oss.dto.BaseFile;

/**
 * @author xh
 */
public interface DataInterface {

    /**
     * 保存一个文件
     *
     * @param baseFile
     */
    void insert(BaseFile baseFile);

}
