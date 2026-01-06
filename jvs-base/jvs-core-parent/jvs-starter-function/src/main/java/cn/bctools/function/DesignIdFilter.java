package cn.bctools.function;

import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.function.handler.IJvsFunction;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 记录设计id
 *
 * @Author: GuoZi
 */
@Order
@Component
public class DesignIdFilter extends GenericFilterBean {

    public static final String HEADER_DESIGN_ID = "designId";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String designId = request.getHeader(HEADER_DESIGN_ID);
        SystemThreadLocal.set(IJvsFunction.KEY_DESIGN_ID, designId);
        filterChain.doFilter(servletRequest, servletResponse);
    }

}
