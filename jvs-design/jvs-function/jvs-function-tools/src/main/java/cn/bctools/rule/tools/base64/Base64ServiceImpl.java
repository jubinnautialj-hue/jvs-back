package cn.bctools.rule.tools.base64;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.oss.cons.OssSystemCons;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.constant.RuleConstant;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.entity.enums.type.OutputType;
import cn.bctools.rule.entity.enums.type.RuleFile;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * @author bctools.cn
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "Base64",
        group = RuleGroup.加密插件,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.TEXT,
        order = 2,
//        iconUrl = "rule-ipvgateway",
        explain = "支持文件对象加密，文本内容加密，内容解密，解密后生成对应文件"

)
public class Base64ServiceImpl implements BaseCustomFunctionInterface<Base64Dto> {

    OssTemplate ossTemplate;

    @Override
    public Object execute(Base64Dto dto, Map<String, Object> params) {
        //判断是文件对象，还是内容
        if (dto.getBody() instanceof String) {
            //判断加密或解密
            String string = dto.getBody().toString();
            if (dto.isType()) {
                //文本加密
                return Base64.encode(string.getBytes());
            } else {
                //解密
                if (ObjectNull.isNull(dto.getFileName())) {
                    String s = Base64.decodeStr(string);
                    if (JSONUtil.isTypeJSON(s)) {
                        return JSONObject.parseObject(s);
                    }
                    return s;
                }
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                //将文件内容解密到数据中
                Base64.decodeToStream(string, out, true);
                ByteArrayInputStream byteInputStream = new ByteArrayInputStream(out.toByteArray());
                BaseFile baseFile = ossTemplate.put(OssSystemCons.OSS_BUCKET_NAME, RuleConstant.OSS_BUCKET_NAME_PATH + "base64", byteInputStream,
                        IdWorker.getIdStr() + dto.getFileName(), true);
                String fileUrl = ossTemplate.fileLink(baseFile.getFileName(), OssSystemCons.OSS_BUCKET_NAME);
                //返回文件对象
                return new RuleFile().setBucketName(OssSystemCons.OSS_BUCKET_NAME)
                        .setSize(baseFile.getSize())
                        .setFileName(baseFile.getFileName())
                        .setName(baseFile.getName())
                        .setOutputType(OutputType.preview)
                        .setOriginalName(dto.getFileName() + StrUtil.DOT + "png")
                        .setUrl(fileUrl);
            }
        } else if (dto.getBody() instanceof JSONObject) {
            //如果是文件对象
            if (dto.isType()) {
                RuleFile ruleFile = JSON.parseObject(JSON.toJSONString(dto.getBody()), RuleFile.class);
                dto.setBody(ruleFile);
                //如果为文件，加密
                String fileUrl = ossTemplate.fileLink(((RuleFile) dto.getBody()).getFileName(), ((RuleFile) dto.getBody()).getBucketName());
                //将文件内容进行加密，返回 base64字符串
                return Base64.encode(HttpUtil.downloadBytes(fileUrl));
            } else {
                throw new BusinessException("不支持文件对象解密数据参数异常");
            }
        } else {
            throw new BusinessException("不支持文件对象解密数据参数异常");
        }
    }

}
