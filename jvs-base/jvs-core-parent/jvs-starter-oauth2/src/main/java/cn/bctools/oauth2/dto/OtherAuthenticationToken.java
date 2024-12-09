package cn.bctools.oauth2.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;

import java.util.Collection;

/**
 * @author guojing
 */
public class OtherAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;
    @Getter
    @Setter
    private Object principal;
    @Getter
    @Setter
    private String otherParameter;
    @Getter
    @Setter
    private String clientId;
    @Getter
    @Setter
    private String token;
    @Getter
    @Setter
    private String source;
    @Getter
    @Setter
    private String userName;
    @Getter
    @Setter
    private String password;
    @Getter
    @Setter
    private String grantType;
    @Getter
    @Setter
    private String tenantId;
    @Getter
    @Setter
    private String refreshToken;
    @Getter
    @Setter
    private String userAgent;
    @Getter
    @Setter
    private String ch;

    public OtherAuthenticationToken(String parameter, String ch, String clientId, String username, String password, String grantType, String refreshToken, String tenantId, String userAgent) {
        super(null);
        this.otherParameter = parameter;
        this.ch = ch;
        this.clientId = clientId;
        this.userName = username;
        this.password = password;
        this.grantType = grantType;
        this.tenantId = tenantId;
        this.refreshToken = refreshToken;
        this.userAgent = userAgent;
    }

    public OtherAuthenticationToken(String otherParameter, String clientId) {
        super(null);
        this.otherParameter = otherParameter;
        this.clientId = clientId;
    }


    public OtherAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        super.setAuthenticated(true);
    }

    @Override
    public String getName() {
        CustomUser principal = (CustomUser) this.principal;
        return principal.getUserDto().getAccountName();
    }

    @Override
    public Object getCredentials() {
        return null;
    }
}
