package cn.bctools.wps.component;

import cn.bctools.wps.config.RedirectProperties;
import cn.bctools.wps.config.WpsProperties;
import cn.bctools.wps.utils.FileUtil;
import cn.bctools.wps.utils.Token;
import cn.bctools.wps.utils.WpsUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Administrator
 */
@Slf4j
@Component
@AllArgsConstructor
public class WpsComponent {
    private final RedirectProperties redirect;
    private final WpsUtil wpsUtil;
    private final WpsProperties wpsProperties;


    /**
     * 获取预览用URL
     *
     * @param fileId     文件id
     * @param userId     用户id
     * @param checkToken 是否校验token
     */
    public Token getViewUrl(String fileId, String userId, boolean checkToken, String fileName) {
        Token t = new Token();
        String fileType = FileUtil.getFileTypeByName(fileName);
        UUID uid = UUID.randomUUID();
        String uuid = uid.toString().replace("-", "");

        Map<String, String> values = new HashMap<String, String>(5) {
            {
                put("_w_appid", wpsProperties.getAppid());
                if (checkToken) {
                    put("_w_tokentype", "1");
                }
                put(redirect.getKey(), redirect.getValue());
                put("_w_filepath", fileName);
                put("_w_userid", userId);
                put("_w_filetype", "db");
            }
        };

        String wpsUrl = wpsUtil.getWpsUrl(values, fileType, fileId);

        t.setToken(uuid);
        t.setExpiresIn(600);
        t.setWpsUrl(wpsUrl);
        return t;
    }

}
