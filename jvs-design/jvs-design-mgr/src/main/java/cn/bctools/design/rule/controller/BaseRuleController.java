package cn.bctools.design.rule.controller;

import cn.bctools.common.utils.PasswordUtil;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.function.Get;
import cn.bctools.design.rule.component.CronEnum;
import cn.bctools.design.rule.dto.CronDto;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.function.enums.JvsParamType;
import cn.bctools.log.annotation.Log;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.mapper.RuleOptionDao;
import cn.bctools.rule.po.RuleOptionPo;
import cn.bctools.rule.utils.RuleElementUtils;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.URLUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Api(tags = "逻辑引擎基础接口")
@Slf4j
@RestController
@RequestMapping("/base/rule")
@AllArgsConstructor
public class BaseRuleController {
    RuleDesignService designService;
    RuleOptionDao ruleOptionService;

    @Log
    @ApiOperation("CRON")
    @GetMapping("/cron")
    public R<List<CronDto>> cron() {
        List<CronDto> objects = Arrays.stream(CronEnum.values())
                .map(e -> new CronDto().setName(e.getName()).setCron(e))
                .collect(Collectors.toList());
        return R.ok(objects);
    }

    @SneakyThrows
    @ApiOperation("下载API示例代码")
    @GetMapping("/downloadCode/{id}")
    public void downloadCode(HttpServletResponse response, @PathVariable String id) {
        //查询，替换
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        String s1 = "attachment; filename=".concat(URLUtil.encode("demo.java", StandardCharsets.UTF_8));
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, s1);
        response.setStatus(HttpStatus.OK.value());
        ServletOutputStream outputStream = response.getOutputStream();
        InputStream inputStream = new ClassPathResource("Demo.java").getInputStream();
        IOUtils.copy(inputStream, outputStream);
        inputStream.close();
        outputStream.flush();
    }

    @Log
    @ApiOperation("自动解析字段")
    @GetMapping("/jvsParamTypes")
    public R jvsParamType() {
        List<Dict> collect = Arrays.stream(JvsParamType.values())
                .map(e -> Dict.create().set("label", e.getDesc()).set("value", e.name()))
                .collect(Collectors.toList());
        return R.ok(collect);
    }

    @Log
    @ApiOperation("自动解析字段")
    @PostMapping("/jvsParamType")
    public R<List<RuleElementVo>> jvsParamType(@RequestBody String map) {
        //自动解析下级数据
        List<RuleElementVo> elementVoList = RuleElementUtils.get(map);
        return R.ok(elementVoList);
    }

    /**
     * 获取所有的数据类型
     *
     * @return
     */
    @Log
    @ApiOperation("获取类型资源信息")
    @GetMapping("/getClassType")
    public R<List<ClassType>> getClassType() {
        return R.ok(Arrays.stream(ClassType.values()).filter(e -> e != ClassType.未识别).collect(Collectors.toList()));
    }


    @Log
    @ApiOperation("保存自定义配置参数")
    @PostMapping("/customOption/{code}")
    @Transactional(rollbackFor = Exception.class)
    public R saveCustomOption(@PathVariable String code, @RequestBody String body) {
        body = body.replace("=", "");
        String decodedPassword = PasswordUtil.decodedPassword(body, SpringContextUtil.getKey());
        JSONArray list = JSONArray.parseArray(decodedPassword);
        List<RuleOptionPo> ruleOptionPos = new ArrayList<>();
        list.forEach(e -> {
            JSONObject json = (JSONObject) e;
            String name = Get.name(RuleOptionPo::getName);
            if (json.containsKey(name)) {
                RuleOptionPo option = new RuleOptionPo().setField(code).setName(String.valueOf(json.get(name))).setMap(json);
                ruleOptionPos.add(option);
            }
        });
        ruleOptionService.delete(Wrappers.<RuleOptionPo>lambdaQuery().eq(RuleOptionPo::getField, code));
        ruleOptionPos.forEach(ruleOptionService::insert);
        return R.ok();
    }


}
