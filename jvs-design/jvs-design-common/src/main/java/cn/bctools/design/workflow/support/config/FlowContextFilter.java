package cn.bctools.design.workflow.support.config;

import cn.bctools.design.workflow.utils.FlowThreadLocalUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * @author zhuxiaokang
 * 工作流Filter
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class FlowContextFilter extends GenericFilterBean {

    @Override
    @SneakyThrows
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
        filterChain.doFilter(servletRequest, servletResponse);
        // 清空线程中的工作流运行信息
        FlowThreadLocalUtil.clearAll();
    }
}
