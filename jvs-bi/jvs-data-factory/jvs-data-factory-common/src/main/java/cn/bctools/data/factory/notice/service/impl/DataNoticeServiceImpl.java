package cn.bctools.data.factory.notice.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.data.factory.entity.JvsDataFactory;
import cn.bctools.data.factory.notice.bo.ReceiverBo;
import cn.bctools.data.factory.notice.dto.DataNoticeDto;
import cn.bctools.data.factory.notice.dto.DataNotifyDto;
import cn.bctools.data.factory.notice.dto.NoticeCacheDto;
import cn.bctools.data.factory.notice.dto.NoticeExtendTemplateDto;
import cn.bctools.data.factory.notice.enums.NoticeTypeEnum;
import cn.bctools.data.factory.notice.enums.TriggerTypeEnum;
import cn.bctools.data.factory.notice.mapper.DataNoticeMapper;
import cn.bctools.data.factory.notice.po.DataNoticePo;
import cn.bctools.data.factory.notice.send.SendNotifyHandler;
import cn.bctools.data.factory.notice.service.DataNoticeService;
import cn.bctools.data.factory.notice.service.NoticeReceiverHandler;
import cn.bctools.data.factory.notice.utils.NoticeExtendUtils;
import cn.bctools.data.factory.notice.utils.NoticeUtil;
import cn.bctools.data.factory.service.JvsDataFactoryService;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Author: ZhuXiaoKang
 * @Description: 消息通知
 */
@Slf4j
@Service
@AllArgsConstructor
public class DataNoticeServiceImpl extends ServiceImpl<DataNoticeMapper, DataNoticePo> implements DataNoticeService {
    private final Map<String, SendNotifyHandler> flowHandlerMap;
    private final JvsDataFactoryService jvsDataFactoryService;
    private final NoticeReceiverHandler noticeReceiverHandler;

    @Override
    public void saveDataNotice(DataNoticeDto dto) {
        // 校验配置
        NoticeExtendUtils.checkParam(dto.getExtend());
        // 保存
        DataNoticePo po = BeanCopyUtil.copy(dto, DataNoticePo.class);
        po.setExtend(dto.getExtend());
        po.setVariable(NoticeExtendUtils.getTemplateVariable(dto.getExtend()));
        saveOrUpdate(po);
    }

    @Override
    public List<DataNoticePo> getAllByNo(String riskFlowNo) {
        return this.list(Wrappers.lambdaQuery(DataNoticePo.class).eq(DataNoticePo::getDataFactoryId, riskFlowNo).eq(DataNoticePo::getEnabled, Boolean.TRUE));
    }

    @Override
    public void sendNotice(NoticeCacheDto noticeCacheDto) {
        TenantContextHolder.setTenantId(noticeCacheDto.getTenantId());
        List<TriggerTypeEnum> triggerTypeList = noticeCacheDto.getTriggerTypeList();
        //查询数据集数据
        JvsDataFactory dataFactory = jvsDataFactoryService.getById(noticeCacheDto.getJvsDataFactoryId());
        //获取消息
        List<DataNoticePo> allById = this.getAllByNo(noticeCacheDto.getJvsDataFactoryId());
        List<DataNoticePo> waitExecList = allById.stream().filter(e -> triggerTypeList.stream().anyMatch(v -> v.equals(e.getTriggerSetting().getType()))).collect(Collectors.toList());

        //发送消息
        Map<String, Object> data = JSONObject.parseObject(JSONObject.toJSONString(dataFactory), Map.class);
        data.put("operateUser", noticeCacheDto.getOperateUser());
        data.put("operateType", noticeCacheDto.getOperateMethod().getDesc());
        waitExecList.forEach(e -> {
            List<ReceiverBo> receiver = e.getReceiver();
            List<UserDto> users = noticeReceiverHandler.getUser(receiver);
            List<NoticeExtendTemplateDto> extend = e.getExtend();
            extend.forEach(v -> {
                String title = NoticeUtil.replacementText(v.getTitleHtml(), data);
                String content = NoticeUtil.replacement(v.getType(), v.getContentHtml(), data);
                Map<String, Map<String, String>> templateVariable = NoticeUtil.replacementTemplateVariable(e.getVariable(), data);
                // 发送通知
                send(v.getType(), new DataNotifyDto()
                        .setTenantId(noticeCacheDto.getTenantId())
                        .setClientId(SpringContextUtil.getApplicationContextName())
                        .setUsers(users)
                        .setTitle(title)
                        .setContent(content)
                        .setTemplateVariable(templateVariable)
                        .setTemplate(v));
            });
        });
    }

    private void send(NoticeTypeEnum noticeType, DataNotifyDto dto) {
        SendNotifyHandler sendNotifyHandler = Optional.ofNullable(flowHandlerMap.get(noticeType.getImplName())).orElseThrow(() -> new BusinessException("不支持的处理类型"));
        try {
            sendNotifyHandler.send(dto);
        } catch (Exception e) {
            log.error("发送[{}]消息失败, exception: {}", noticeType.getDesc(), e);
        }
    }

}
