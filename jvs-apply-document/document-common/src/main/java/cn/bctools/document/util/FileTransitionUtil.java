package cn.bctools.document.util;


import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.document.config.CommonConfig;
import cn.bctools.document.vo.OnlyOfficeFileTransitionVo;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.util.HashMap;

/**
 * 文件转换工具类
 */
@Slf4j
public class FileTransitionUtil {

    public static String transition(OnlyOfficeFileTransitionVo onlyOfficeFileTransitionVo) {
        CommonConfig commonConfig = SpringContextUtil.getBean(CommonConfig.class);
        HashMap<String, String> headMap = new HashMap<>(2);
        headMap.put("Accept", "application/json");
        headMap.put("Content-Type", "application/json;charset=UTF-8");

        //如果是pdf转图片 需要设置宽高
        if("pdf".equals(onlyOfficeFileTransitionVo.getFiletype()) && isImage(onlyOfficeFileTransitionVo.getOutputtype())){
            float width = PDRectangle.A1.getWidth();
            float height = PDRectangle.A1.getHeight();
            onlyOfficeFileTransitionVo.setThumbnail(new OnlyOfficeFileTransitionVo
                    .Thumbnail()
                    .setFirst(Boolean.FALSE)
                    .setWidth((int) width).setHeight((int) height)
            );
        }

        HttpResponse execute = HttpUtil.createPost(commonConfig.getOnlyOfficeUrl())
                .addHeaders(headMap).body(JSONUtil.toJsonStr(onlyOfficeFileTransitionVo)).execute();
        log.info("文件转换返回值{}",execute.body());
        //获取链接
        JSONObject jsonObject = JSONObject.parseObject(execute.body());
        if (!jsonObject.containsKey("fileUrl")) {
            throw new BusinessException("转换失败!");
        }
        return jsonObject.getString("fileUrl");
    }

    /**
     * 根据文件扩展名判断文件是否图片格式
     * @param suffix 文件类型
     * @return 是否是图片
     */
    public static boolean isImage(String suffix) {
        if(StrUtil.isBlank(suffix)){
            return Boolean.FALSE;
        }

        String[] imageExtension = new String[]{"jpeg", "jpg", "bmp", "png"};

        for (String e : imageExtension){
            if (suffix.toLowerCase().equals(e)) {
                return true;
            }
        }
        return false;
    }
}
