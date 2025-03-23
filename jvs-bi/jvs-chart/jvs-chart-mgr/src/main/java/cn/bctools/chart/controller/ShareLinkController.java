package cn.bctools.chart.controller;


import cn.bctools.chart.chart.po.ChartReturnObj;
import cn.bctools.chart.dto.MonomerDto;
import cn.bctools.chart.dto.ShareLinkData;
import cn.bctools.chart.entity.ChartPage;
import cn.bctools.chart.service.ChartPageService;
import cn.bctools.chart.util.LinkUtil;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.log.annotation.Log;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * @author admin
 */
@Api(tags = "图表-公开链接")
@RestController
@AllArgsConstructor
@RequestMapping("/share/link")
@Slf4j
public class ShareLinkController {
    private final static String DESIGN_KEY = "json";
    private final ChartPageService chartPageService;

    @Log
    @ApiOperation("开启或关闭链接")
    @PostMapping("/open/{id}/{shareIs}")
    public R<ShareLinkData> openShareLink(@RequestBody ShareLinkData shareLinkData, @ApiParam("当前开启的数据id") @PathVariable String id, @ApiParam("是否开启") @PathVariable Boolean shareIs) {
        String longLink = shareLinkData.getLongLink();
        String shortLink = null;
        if (shareIs) {
            shortLink = LinkUtil.generateShortLink(longLink);
            shareLinkData.setShortLink("/#/sharelink/" + shortLink);
        }
        chartPageService.update(new UpdateWrapper<ChartPage>().lambda().eq(ChartPage::getId, id).set(ChartPage::getShareIs, shareIs).set(ChartPage::getShortLink, shareLinkData.getShortLink()).set(ChartPage::getLongLink, longLink).set(ChartPage::getShortLinkValue, shortLink)
                //这里如果是关闭链接需要清空到期时间与密码
                .set(!shareIs, ChartPage::getExpirationTime, null).set(!shareIs, ChartPage::getPassword, null));
        return R.ok(shareLinkData);
    }

    @Log
    @ApiOperation("[第二步]校验链接是否有效")
    @PostMapping("/no/auth/check/{id}")
    public R openShareLink(@ApiParam("当前开启的数据id") @PathVariable String id, @RequestBody ShareLinkData shareLinkData) {
        ChartPage byId = chartPageService.getById(id);
        if (byId == null) {
            return R.failed("此数据不存在");
        }
        if (!byId.getShareIs()) {
            return R.failed("此链接已经关闭");
        }
        if (StrUtil.isNotBlank(byId.getPassword())) {
            if (StrUtil.isBlank(shareLinkData.getPassword())) {
                return R.failed("请输入密码");
            }
            if (!byId.getPassword().equals(shareLinkData.getPassword())) {
                return R.failed("密码错误");
            }
        }
        if (ObjectUtil.isNotNull(byId.getExpirationTime())) {
            int i = LocalDateTime.now().compareTo(byId.getExpirationTime());
            if (i > 0) {
                return R.failed("链接已经失效");
            }
        }
        return R.ok();
    }

    @Log
    @ApiOperation("[第三步]基础信息获取")
    @GetMapping("/no/auth/detail/{id}")
    public R<ChartPage> detail(@PathVariable String id) {
        ChartPage byId = chartPageService.getById(id);
        return R.ok(byId);
    }

    @Log
    @ApiOperation("[第一步]通过短链接获取长链接")
    @PostMapping("/no/auth/get/long/link/{sortLink}")
    public R<ShareLinkData> getLongLink(@ApiParam("短链接码") @PathVariable String sortLink) {
        ChartPage one = chartPageService.getOne(new LambdaQueryWrapper<ChartPage>().eq(ChartPage::getShortLinkValue, sortLink));
        if (one != null) {
            if (ObjectUtil.isNotNull(one.getExpirationTime())) {
                int i = LocalDateTime.now().compareTo(one.getExpirationTime());
                if (i > 0) {
                    return R.failed("链接已经失效");
                }
            }
            ShareLinkData shareLinkData = new ShareLinkData().setLongLink(one.getLongLink()).setTenantId(one.getTenantId()).setId(one.getId());
            if (StrUtil.isNotBlank(one.getPassword())) {
                shareLinkData.setPassword("******");
            }
            return R.ok(shareLinkData);
        }
        return R.failed("此链接不存在");
    }

    @Log(back = false)
    @ApiOperation("[第四步]分享链接访问时数据获取接口")
    @PostMapping("/no/auth/link/monomer/{name}")
    public R<ChartReturnObj> linkComponentPreview(@RequestBody MonomerDto monomerDto, @ApiParam("当前设计的名称") @PathVariable("name") String chartName) {
        ChartReturnObj chartReturnObj = new ChartReturnObj();
        //获取json
        String json = monomerDto.getQueryData().get(DESIGN_KEY);
        String tenantId = monomerDto.getTenantId();
        if (StrUtil.isBlank(tenantId)) {
            chartReturnObj.setError("未找到租户id");
            return R.ok(chartReturnObj);
        }
        TenantContextHolder.setTenantId(tenantId);
        //刪除设计数据
        monomerDto.getQueryData().remove(DESIGN_KEY);
        chartReturnObj = chartPageService.dataTranslation(json, monomerDto, chartName);
        return R.ok(chartReturnObj);
    }

}
