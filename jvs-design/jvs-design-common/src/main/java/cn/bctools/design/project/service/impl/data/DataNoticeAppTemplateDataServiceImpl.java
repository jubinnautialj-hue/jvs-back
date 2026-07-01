package cn.bctools.design.project.service.impl.data;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.notice.entity.DataNoticePo;
import cn.bctools.design.notice.service.DataNoticeService;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.TemplateBo;
import cn.bctools.design.project.service.AppTemplateDataService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 应用模板——消息通知
 */
@Service
@AllArgsConstructor
public class DataNoticeAppTemplateDataServiceImpl extends AppTemplateDataBase implements AppTemplateDataService<DataNoticePo> {
    private final DataNoticeService dataNoticeService;

    @Override
    public List<DataNoticePo> list(String jvsAppId, List<String> ids) {
        List<DataNoticePo> dataNoticePoList = dataNoticeService.list(Wrappers.<DataNoticePo>lambdaQuery().eq(DataNoticePo::getJvsAppId, jvsAppId));
        if (CollectionUtils.isEmpty(dataNoticePoList)) {
            return Collections.emptyList();
        }
        List<String> listIds = dataNoticePoList.stream()
                .peek(this::clearDefaultData)
                .map(DataNoticePo::getId).collect(Collectors.toList());
        ids.addAll(listIds);
        return dataNoticePoList;
    }

    @Override
    public void save(JvsApp jvsApp, List<String> existsIds, TemplateBo templateBo, TemplateBo targetVersionTemplateBo) {
        // 删除
        deleteByAppId(dataNoticeService, DataNoticePo::getJvsAppId, jvsApp.getId());
        // 新增
        List<DataNoticePo> dataNoticePos = templateBo.getDataNoticePos();
        if (ObjectNull.isNull(dataNoticePos)) {
            return;
        }
        // 清空默认数据
        dataNoticePos.forEach(this::clearDefaultData);
        saveBatch(dataNoticeService, dataNoticePos);
    }
}
