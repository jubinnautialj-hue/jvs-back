package cn.bctools.auth.component;

import cn.bctools.auth.entity.User;
import cn.bctools.auth.login.LoginHandler;
import cn.bctools.auth.login.auth.OtherLoginHandler;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.PasswordUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author Administrator
 */
@Slf4j
@Component
public class OtherAuthComponent {

    @Resource
    Map<String, LoginHandler> handlerMap;
    @Resource
    OtherLoginHandler otherLoginHandler;

    /**
     * 根据标识获取用户
     *
     * @param type
     * @param code
     * @param appId
     * @return
     */
    public User getUser(String type, String code, String appId) {
        LoginHandler loginHandler = handlerMap.get(type);
        //如果扩展的没有, 就找默认的类型
        if (ObjectNull.isNull(loginHandler)) {
            return otherLoginHandler.handle(code, appId, type);
        }

        if (loginHandler.encryption()) {
            String decodedPassword = PasswordUtil.decodedPassword(code, appId);
            Object obj = JSONObject.parseObject(decodedPassword, getParameterClass(loginHandler.getClass()));
            return loginHandler.handle(code, appId, obj);
        } else {
            return loginHandler.handle(code, appId, null);
        }
    }


    /**
     * 获取接口的泛型的类
     */
    public Class getParameterClass(Class<?> e) {
        Type obj = e.getGenericInterfaces()[0];
        ParameterizedType parameterizedType = (ParameterizedType) obj;
        return (Class) parameterizedType.getActualTypeArguments()[0];
    }

}
