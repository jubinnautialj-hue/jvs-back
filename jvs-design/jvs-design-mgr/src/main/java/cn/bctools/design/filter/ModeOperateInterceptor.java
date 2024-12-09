package cn.bctools.design.filter;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.util.CurrentAppUtils;
import cn.bctools.design.util.ModeUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 模式操作拦截器
 */
public class ModeOperateInterceptor implements HandlerInterceptor {

    private static final PathMatcher PATH_MATCHER = new AntPathMatcher();

    private static List<ModeDisableUri> modeDisableUris = null;

    /**
     * 禁用接口
     */
    @Getter
    @AllArgsConstructor
    public enum ModeDisableUri {
        // 创建目录
        ADD_MENU_TYPE(HttpMethod.POST, "/app/design/{appId}/JvsApp/add/type"),
        // 创建表单
        ADD_FORM(HttpMethod.POST, "/app/design/{appId}/form"),
        // 创建列表页
        ADD_PAGE(HttpMethod.POST, "/app/design/{appId}/page/create"),
        // 创建流程设计
        ADD_FLOW(HttpMethod.POST, "/app/design/{appId}/workflow"),
        // 快捷创建流程设计
        ADD_QUICK_FLOW(HttpMethod.POST, "/app/design/{appId}/workflow/quick-create"),
        // 创建图表
        ADD_CHART(HttpMethod.POST, "/app/design/{appId}/chart/design"),
        // 创建报表
        ADD_REPORT(HttpMethod.POST, "/app/design/{appId}/report/design"),
        // 创建逻辑引擎
        ADD_RULE(HttpMethod.PUT, "/app/design/{appId}/rule"),
        // 创建列表页按钮创建表单
        ADD_PAGE_GENERATE_FORM(HttpMethod.GET, "/app/design/{appId}/page/generateForm/{dataModelId}/{designId}/{buttonName}"),
        // 创建自定义页面
        ADD_URL(HttpMethod.POST, "/app/design/{appId}/jvsAppUrl"),
        // 创建外部页面接入配置
        ADD_EXTERNAL(HttpMethod.POST, "/app/design/{appId}/external/page"),
        // 创建通知配置
        ADD_NOTICE(HttpMethod.POST, "/app/design/{appId}/data/notice"),
        // 创建在线打印模板
        ADD_PRINT_TEMPLATE(HttpMethod.POST, "/app/design/{appId}/print/template"),
        // 创建源码开发标识
        ADD_IDENTIFICATION(HttpMethod.POST, "/app/design/{appId}/identification"),
        // 创建模型
        ADD_DATA_MODEL(HttpMethod.POST, "/app/design/{appId}/data/model"),
        // 依据模型字段自动生成列表和表单设计
        ADD_DATA_MODEL_CRUD_PAGE(HttpMethod.POST, "/app/design/{appId}/data/model/generate/crud/design/{modelId}"),

        // 删除目录
        DEL_MENU_TYPE(HttpMethod.DELETE, "/app/design/{appId}/JvsApp/del/type/{id}"),
        // 删除模型
        DEL_DATA_MODEL(HttpMethod.DELETE, "/app/design/{appId}/data/model/{id}"),
        // 删除模型字段
        DEL_DATA_MODEL_FIELD(HttpMethod.DELETE, "/app/design/{appId}/data/model/{modelId}/{fieldKey}"),
        // 删除设计
        DEL_DESIGN(HttpMethod.DELETE, "/app/design/{appId}/JvsApp/del"),
        // 删除流程设计
        DEL_FLOW(HttpMethod.DELETE, "/app/design/{appId}/workflow/{id}"),
        // 根据凭证删除逻辑
        DEL_RULE_SECRET(HttpMethod.DELETE,"/app/design/{appId}/rule/secret/{secret}"),
        // 删除逻辑
        DEL_RULE(HttpMethod.DELETE,"/app/design/{appId}/rule/{id}"),
        // 删除外部页面接入配置
        DEL_EXTERNAL(HttpMethod.DELETE, "/app/design/{appId}/external/page/{id}"),
        // 删除通知配置
        DEL_NOTICE(HttpMethod.DELETE, "/app/design/{appId}/data/notice/{id}"),
        // 删除在线打印模板
        DEL_PRINT_TEMPLATE(HttpMethod.DELETE, "/app/design/{appId}/print/template/{id}"),
        // 删除源码开发标识
        DEL_IDENTIFICATION(HttpMethod.DELETE, "/app/design/{appId}/identification/{id}"),

        // 修改源码开发标识
        UPDATE_IDENTIFICATION(HttpMethod.PUT, "/app/design/{appId}/identification"),
        ;
        private final HttpMethod httpMethod;
        private final String uri;
    }

    private List<ModeDisableUri> getModeDisableUris() {
        if (ObjectNull.isNotNull(modeDisableUris)) {
            return modeDisableUris;
        }
        modeDisableUris = Arrays.stream(ModeDisableUri.values()).collect(Collectors.toList());
        return modeDisableUris;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        JvsApp jvsApp = CurrentAppUtils.getApp();
        // 应用未升级轻应用版本功能，直接放行
        if (ObjectNull.isNotNull(jvsApp) && Boolean.FALSE.equals(jvsApp.getEnableVersionFeature())) {
            return Boolean.TRUE;
        }

        // 应用已启用轻应用版本功能，限制非开发模式部分接口操作
        // 开发模式直接放行
        if (AppVersionTypeEnum.DEV.equals(ModeUtils.getMode())) {
            return Boolean.TRUE;
        }
        // 非开发模式，禁用部分接口
        String requestUri = request.getRequestURI();
        boolean disable = getModeDisableUris().stream().anyMatch(u -> {
            boolean matchUri = PATH_MATCHER.matchStart(u.getUri(), requestUri);
            boolean matchMethod = u.httpMethod.matches(request.getMethod());
            return matchUri && matchMethod;
        });
        if (disable) {
            InterceptorUtil.throwException(response.getOutputStream(), "请切换到开发模式");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
