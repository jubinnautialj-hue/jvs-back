package cn.bctools.gateway.dto;

import cn.bctools.oauth2.dto.CustomUser;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 只做网关资源校验其它不需要做处理
 *
 * @author wl
 */
@Data
@Accessors(chain = true)
public class CheckToken implements Authentication {

    private CustomUser principal;

    public CheckToken() {
    }

    public CheckToken(CustomUser principal) {
        this.principal = principal;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public boolean isAuthenticated() {
        return false;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

    }

    @Override
    public String getName() {
        return null;
    }
}
