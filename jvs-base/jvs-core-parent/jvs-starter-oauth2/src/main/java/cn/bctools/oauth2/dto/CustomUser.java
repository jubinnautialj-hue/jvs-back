package cn.bctools.oauth2.dto;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.entity.dto.UserInfoDto;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author guojing
 */
public class CustomUser extends UserInfoDto implements UserDetails, OAuth2AuthenticatedPrincipal, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public Map<String, Object> getAttributes() {
        return new HashMap<>(0);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<>();
    }

    @Override
    public String getPassword() {
        UserDto userDto = super.getUserDto();
        return userDto.getPassword();
    }

    @Override
    public String getUsername() {
        return super.getUserDto().getAccountName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return !getUserDto().getCancelFlag();
    }

    @Override
    public boolean isAccountNonLocked() {
        return isAccountNonExpired();
    }

    /**
     * 判断是否过期
     *
     * @return
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return isAccountNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return isAccountNonExpired();
    }

    @Override
    public String getName() {
        return getUsername();
    }
}
