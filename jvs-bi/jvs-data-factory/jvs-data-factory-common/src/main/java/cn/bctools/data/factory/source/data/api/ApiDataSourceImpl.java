package cn.bctools.data.factory.source.data.api;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.data.factory.source.data.po.InParameterJsonPo;
import cn.bctools.data.factory.source.data.po.SettingJsonPo;
import cn.bctools.data.factory.source.data.service.DataSourceExecuteInterface;
import cn.bctools.data.factory.source.dto.ApiDataSourceDto;
import cn.bctools.data.factory.source.dto.ApiDataSourceExecDto;
import cn.bctools.data.factory.source.dto.ExecApiPo;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.enums.RequestTypeEnums;
import cn.bctools.data.factory.source.service.SysJarService;
import cn.bctools.data.factory.source.util.JarExecUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Component(value = "apiDataSource")
@Slf4j
public class ApiDataSourceImpl implements DataSourceExecuteInterface {
    @Resource
    SysJarService sysJarService;

    /**
     * 执行逻辑 主要是用于获取json
     *
     * @param execApiPo api接口数据
     */
    public String test(ExecApiPo execApiPo) {
        String execResult;
        ApiDataSourceExecDto apiDataSourceExecDto = new ApiDataSourceExecDto();
        apiDataSourceExecDto.setUrl(execApiPo.getExecuteName())
                .setRequestMethod(execApiPo.getSettingJson().getRequestMethod())
                .setOtherJson(execApiPo.getOtherJson());
        String authJson = execApiPo.getAuthJson();
        if (StrUtil.isNotBlank(authJson) && !authJson.isEmpty()) {
            apiDataSourceExecDto.setAuthKey(authJson);
        }
        List<InParameterJsonPo> inParameterJsonPo = Optional.ofNullable(execApiPo.getInParameterJson()).orElse(new ArrayList<>());
        List<ApiDataSourceExecDto.Parameter> inParameterList = inParameterJsonPo.stream().map(e -> {
            ApiDataSourceExecDto.Parameter parameter = null;
            Object value = e.getValue();
            if (ObjectUtil.isEmpty(value) && e.getRequiredIs()) {
                value = e.getDefaultValue();
            }
            if (ObjectUtil.isNotEmpty(value)) {
                parameter = new ApiDataSourceExecDto.Parameter().setValue(ObjectUtil.isNotEmpty(e.getValue()) ? e.getValue() : e.getDefaultValue())
                        .setKey(e.getKey())
                        .setInParameterType(e.getRequestTypeEnums());
            }
            return parameter;
        }).filter(ObjectUtil::isNotNull).collect(Collectors.toList());
        if (!inParameterList.isEmpty()) {
            apiDataSourceExecDto.setInParameter(inParameterList);
        }
        if (StrUtil.isBlank(execApiPo.getSysJarId())) {
            return JarExecUtil.exec(apiDataSourceExecDto);
        }
        //获取数据源
        File jarFile = sysJarService.getJarFile(execApiPo.getSysJarId());
        try {
            execResult = JarExecUtil.exec(jarFile, JSONObject.toJSONString(apiDataSourceExecDto));
        } catch (Exception exception) {
            log.error("执行api调用错误", exception);
            throw new BusinessException("协议包内部错误!");
        } finally {
            boolean delete = jarFile.delete();
            log.info("删除jar包:{}", delete);
        }
        return execResult;
    }


    @Override
    public void check(String json) {
        ExecApiPo execApiPo = JSONObject.parseObject(json, ExecApiPo.class);
        test(execApiPo);
    }

    @Override
    public List<DataSourceStructure> syncTableStructure(DataSource dataSource) {
        return null;
    }

    @Override
    public Page<Map<String, Object>> findAll(DataSource dataSource, DataSourceStructure dataSourceStructure, long size, long current) {
        //判断是否存在分页
        SettingJsonPo settingJson = dataSourceStructure.getSettingJson();
        if (settingJson.getPageIs()) {
            //设置值
            dataSourceStructure.getInParameterJson().stream().peek(e -> {
                switch (e.getInParameterTypeEnums()) {
                    case size:
                        e.setValue(size);
                        break;
                    case current:
                        e.setValue(current);
                        break;
                    default:
                }
            }).collect(Collectors.toList());
        }
        ApiDataSourceDto apiDataSourceDto = dataSource.getCustomJson().toJavaObject(ApiDataSourceDto.class);
        ExecApiPo execApiPo = BeanCopyUtil.copy(dataSourceStructure, ExecApiPo.class);
        execApiPo.setAuthJson(apiDataSourceDto.getAuthJson())
                .setSysJarId(apiDataSourceDto.getSysJarId());
        //url 替换
        List<InParameterJsonPo> parameterJsonPos = execApiPo.getInParameterJson().stream().filter(e -> e.getRequestTypeEnums().equals(RequestTypeEnums.params)).collect(Collectors.toList());
        if (!parameterJsonPos.isEmpty()) {
            String url = execApiPo.getExecuteName();
            List<String> matches = ReUtil.findAll("\\{([a-zA-Z0-9]+)\\}", url, 1).stream().distinct().collect(Collectors.toList());
            if (!matches.isEmpty()) {
                List<InParameterJsonPo> jsonPos = parameterJsonPos.stream().filter(e -> matches.contains(e.getKey())).collect(Collectors.toList());
                if (jsonPos.isEmpty() || jsonPos.size() != matches.size()) {
                    throw new BusinessException("替换url时未找到数据");
                }
                for (int i = 0; i < matches.size(); i++) {
                    String s = matches.get(i);
                    //获取值
                    InParameterJsonPo inParameterJsonPo = jsonPos.stream().filter(e -> e.getKey().equals(s)).findFirst().get();
                    url = url.replaceAll("\\{" + s + "}", inParameterJsonPo.getValue().toString());
                }
                execApiPo.setExecuteName(url);
            }
        }
        String json = this.test(execApiPo);
        log.info("请求接口的返回值为:{}", json);
        Page<Map<String, Object>> page = new Page<>();
        //如果存在分页需要获取总数量
        if (settingJson.getPageIs()) {
            if (StrUtil.isNotBlank(settingJson.getCodeKey())) {
                if (!this.getCode(json, settingJson.getCodeKey(), settingJson.getCodeValue())) {
                    throw new BusinessException("获取数据错误");
                }
            }
            String totalKey = settingJson.getTotalKey();
            Integer total;
            try {
                total = getTotal(json, totalKey);
            } catch (Exception e) {
                log.info("获取总条数值，错误", e);
                throw new BusinessException("获取总条数值，错误");
            }
            page.setTotal(total)
                    .setCurrent(current)
                    .setSize(size);
            if (total == 0) {
                return page;
            }
        }
        //获取结果集
        List<Map> data = getData(json, settingJson.getDataKey());
        List<Map<String, Object>> collect = data.stream().map(e -> {
            Map<String, Object> map = new HashMap<>(e.size());
            e.forEach((k, v) -> map.put(k.toString(), v));
            return map;
        }).collect(Collectors.toList());
        //如果不存在分页直接 使用数据量作为 总条数
        if (!settingJson.getPageIs()) {
            page.setTotal(collect.size());
        }
        page.setRecords(collect);
        return page;
    }

    private Integer getTotal(String json, String totalKey) {
        String byPath = this.getByPath(json, totalKey);
        byPath = Optional.ofNullable(byPath).orElseThrow(() -> new BusinessException("返回值格式错误，返回值:{}", json));
        return Integer.valueOf(byPath);
    }

    private Boolean getCode(String json, String codeKey, String codeValue) {
        String byPath = this.getByPath(json, codeKey);
        return byPath.equals(codeValue);
    }

    private String getByPath(String json, String path) {
        if (!JSONValidator.from(json).getType().equals(JSONValidator.Type.Object)) {
            throw new BusinessException("返回值格式错误，返回值:{}", json);
        }
        String value = null;
        String[] split = path.split("\\.");
        JSONObject jsonObject = JSONObject.parseObject(json);
        for (int i = 0; i < split.length; i++) {
            if (i == split.length - 1) {
                value = jsonObject.getString(split[i]);
            } else {
                jsonObject = jsonObject.getJSONObject(split[i]);
            }
        }
        return value;
    }

    private List<Map> getData(String json, String dataKey) {
        String byPath = this.getByPath(json, dataKey);
        byPath = Optional.ofNullable(byPath).orElseThrow(() -> new BusinessException("返回值格式错误，返回值:{}", json));
        if (JSONValidator.from(byPath).getType().equals(JSONValidator.Type.Object)) {
            return Arrays.asList(JSONObject.parseObject(byPath, Map.class));
        }
        return JSONObject.parseArray(byPath, Map.class);
    }


    @Override
    public Long getCount(DataSource dataSource, DataSourceStructure dataSourceStructure) {

        return null;
    }
}
