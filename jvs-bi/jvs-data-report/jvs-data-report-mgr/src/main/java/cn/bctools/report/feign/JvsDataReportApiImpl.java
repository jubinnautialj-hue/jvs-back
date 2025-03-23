package cn.bctools.report.feign;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.report.api.JvsDataReportApi;
import cn.bctools.report.entity.JvsDataReport;
import cn.bctools.report.service.JvsDataReportService;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Api(tags = "内部调用")
@RestController
@RequiredArgsConstructor
@Slf4j
public class JvsDataReportApiImpl implements JvsDataReportApi {

    private final JvsDataReportService jvsDataReportService;

    @Override
    public R<Boolean> check(String id, String isTask) {
        return R.ok(false);
    }

    @Override
    public R<JSONObject> downFile(String id, Boolean isMock) {
        JvsDataReport dataReport = jvsDataReportService.getById(id);
        if(dataReport==null){
            throw new BusinessException("报表不存在");
        }
        dataReport.clear();
        return R.ok(JSONObject.parseObject(JSONObject.toJSONString(dataReport)));
    }

    @Override
    public R<Boolean> up(JSONObject upData, String id) {
        UserDto userDto = upData.getJSONObject("userDto").toJavaObject(UserDto.class);
        JSONObject jsonObject = upData.getJSONObject("data");

        JvsDataReport report = jsonObject.toJavaObject(JvsDataReport.class);

        report.setMenuId(id);
        report.setCreateBy(userDto.getRealName());
        report.setCreateById(userDto.getId());
        report.setUpdateBy(userDto.getRealName());

        String tenantId = TenantContextHolder.getTenantId();
        report.setTenantId(tenantId);
        TenantContextHolder.clear();

        long count = jvsDataReportService.count(Wrappers.lambdaQuery(JvsDataReport.class).eq(JvsDataReport::getMenuId, id));
        report.setSort((int) count +1);

        /*判断是否重复*/
        boolean exist = jvsDataReportService.count(new LambdaQueryWrapper<JvsDataReport>().eq(JvsDataReport::getId, report.getId())) > 0;
        if (exist) {
            jvsDataReportService.updateById(report);
        } else {
            jvsDataReportService.save(report);
        }
        return R.ok(Boolean.TRUE);
    }

    @Override
    public R<List<String>> getDataFactoryId(String id) {
        List<String> dataFactoryIds = new ArrayList<>();
        return R.ok(dataFactoryIds);
    }

}
