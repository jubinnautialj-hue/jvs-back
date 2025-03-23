package cn.bctools.data.factory.source.service;

import cn.bctools.data.factory.source.data.po.ExcelReadDataPo;
import cn.bctools.data.factory.source.entity.ExcelCommitLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * excel 上传历史 服务类
 * </p>
 *
 * @author admin
 * @since 2024-06-11
 */
public interface ExcelCommitLogService extends IService<ExcelCommitLog> {
    /**
     * 保存
     */
    void save(ExcelReadDataPo readDataPo, String lotNo);

    /**
     * 删除某一次提交的数据
     * 此方法
     *
     * @param documentName 名称
     * @param lotNo        批次号
     */
    void delByLotNo(String documentName, String lotNo);


}
