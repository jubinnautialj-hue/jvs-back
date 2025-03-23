package cn.bctools.design.filter;

import cn.bctools.auth.api.enums.ModeTypeEnum;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.project.dto.SwitchModeDto;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.function.service.ModeTypeService;
import cn.hutool.core.util.URLUtil;
import org.springframework.context.annotation.Bean;
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
 * 设计套件统一处理
 *
 * @Author: GuoZi
 */
@Order
@Component
public class DesignFilter extends GenericFilterBean {

    public static final String HEADER_DESIGN_ID = "designId";
    public static final String HEADER_DESIGN_OPERATOR = "operator";
    public static final String HEADER_PAGE_DESIGN_ID = "pageDesignId";
    public static final String HEADER_MODE = "mode";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String designId = URLUtil.decode(request.getHeader(HEADER_DESIGN_ID));
        String operator = URLUtil.decode(request.getHeader(HEADER_DESIGN_OPERATOR));
        String pageDesignId = URLUtil.decode(request.getHeader(HEADER_PAGE_DESIGN_ID));
        String mode = URLUtil.decode(request.getHeader(HEADER_MODE));
        if (ObjectNull.isNull(mode)) {
            //如果请求头没有可以通过 url参数
            mode = URLUtil.encode(request.getParameter(HEADER_MODE));
        }
        // 记录设计id与操作类型
        DynamicDataUtils.setDesignId(designId);
        DynamicDataUtils.setOperator(operator);
        DynamicDataUtils.setPageDesignId(pageDesignId);
        // 设置轻应用模式
        // 优先设置用户token关联的模式，若没有，则使用请求头中的模式
        ModeUtils.setMode();
        if (ObjectNull.isNull(ModeUtils.getMode())) {
            // 若请求头中也没有模式，或者匹配不到模式，则默认使用正式模式
            AppVersionTypeEnum versionType = AppVersionTypeEnum.getType(mode);
            ModeUtils.setSwitchModel(new SwitchModeDto().setMode(versionType));
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * 定时任务获取环境变量时获取的模式信息
     *
     * @return
     */
    @Bean
    public ModeTypeService typeService() {
        return () -> ObjectNull.isNotNull(ModeUtils.getMode()) ? ModeTypeEnum.getType(ModeUtils.getMode().name()) : ModeTypeEnum.GA;
    }

}
