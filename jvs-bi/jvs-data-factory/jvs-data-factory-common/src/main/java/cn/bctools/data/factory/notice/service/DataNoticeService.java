package cn.bctools.data.factory.notice.service;

import cn.bctools.data.factory.notice.dto.DataNoticeDto;
import cn.bctools.data.factory.notice.dto.NoticeCacheDto;
import cn.bctools.data.factory.notice.po.DataNoticePo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Author: ZhuXiaoKang
 * @Description: 消息通知
 */
public interface DataNoticeService extends IService<DataNoticePo> {

    /**
     * 模型消息模板——新增或修改消息通知配置
     *
     * @param dto
     */
    void saveDataNotice(DataNoticeDto dto);

    /**
     * 获取绑定的消息数据
     *
     * @param dataFactoryId 数据集id
     * @return {@link  DataNoticePo}
     */
    List<DataNoticePo> getAllByNo(String dataFactoryId);

    /**
     * 发送消息
     *
     * @param noticeCacheDto
     */
    void sendNotice(NoticeCacheDto noticeCacheDto);

}
