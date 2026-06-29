package cn.bctools.auth.login;

import cn.bctools.auth.entity.User;
import cn.bctools.auth.login.dto.SyncUserDto;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.hutool.http.HttpUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;

/**
 * @author Administrator
 */
public interface LoginHandler<T> {

    /**
     * 这个模式是否加密处理
     *
     * @return
     */
    default Boolean encryption() {
        return true;
    }

    /**
     * 根据code码获取用户信息
     *
     * @param code  登录参数
     * @param appId 应用ID 主要用户特殊登录的数据加密操作,和判定是否支持此应用登录
     * @param t     用户登录参数
     * @return 用户信息
     */
    User handle(String code, String appId, T t);

    /**
     * 绑定逻辑
     *
     * @param user  用户对象，是哪一个用户需要绑定
     * @param code  三方code码
     * @param appId 应用ID哪一个前端项目在绑定
     */
    default void bind(User user, String code, String appId) {
    }

    /**
     * 绑定头像
     *
     * @param nickname 昵称
     * @param fileUrl  头像地址
     * @return
     */
    static String getDurableAvatar(String nickname, String fileUrl) {
        if (StringUtils.isBlank(fileUrl)) {
            //使用默认头像
            return "/jvs-ui-public/img/headImg.png";
        }
        try {
            OssTemplate ossTemplate = SpringContextUtil.getBean(OssTemplate.class);
            //头像可能一直下载不下来. 1秒超时直接放弃使用默认头像
            byte[] bytes = HttpUtil.createGet(fileUrl, true)
                    .timeout(1000)
                    .executeAsync().bodyBytes();
            BaseFile baseFile = ossTemplate.putFile("jvs-public", "/wx/avatar", nickname + ".jpg", new ByteArrayInputStream(bytes));
            return ossTemplate.fileJvsPublicLink(baseFile.getFileName());
        } catch (Exception e) {
            //使用默认头像
            return "/jvs-ui-public/img/headImg.png";
        }
    }

    /**
     * 根据不同的类型同步用户组织结构
     *
     * @return
     */
    default SyncUserDto syncUserDeptAll() {
        return null;
    }
}
