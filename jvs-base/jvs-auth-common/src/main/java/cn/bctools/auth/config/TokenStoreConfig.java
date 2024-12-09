package cn.bctools.auth.config;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Administrator
 */
@Slf4j
@Component
public class TokenStoreConfig {
    /**
     * 缓存密码,用户登陆缓存校验,如果N个人使用的密码都为123456  但加密的值,可能有很多个值,但匹配时,不需要去重新处理匹配解密
     */
    static Cache<CharSequence, Set<String>> cache = CacheUtil.newFIFOCache(500);

    /**
     * 处理加密，可以修改密码方式，提升性能。
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(4) {
            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                if (cache.containsKey(rawPassword)) {
                    //如果匹配到了,直接返回成功
                    if (cache.get(rawPassword).contains(encodedPassword)) {
                        return true;
                    }
                }
                //没有匹配到,直接处理解密
                boolean matches = super.matches(rawPassword, encodedPassword);
                if (matches) {
                    //将匹配的值 ,再放入缓存中
                    Set<String> pass = cache.get(rawPassword, () -> new HashSet<>());
                    pass.add(encodedPassword);
                    cache.put(rawPassword, pass);
                }
                return matches;
            }
        };
    }

}
