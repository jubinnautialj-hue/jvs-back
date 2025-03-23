package cn.bctools.document.auth;

import cn.bctools.document.auth.cofig.DataAuthConfig;
import cn.bctools.document.auth.dto.LibraryAuthDto;
import cn.bctools.document.auth.util.AuthSystemTool;
import cn.bctools.document.dto.IdentifyingAuthDto;
import cn.bctools.document.entity.DcAuthConfig;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.enums.DcLibraryReadEnum;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.service.DcAuthConfigService;
import cn.bctools.document.service.DcLibraryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * [Description]:
 * 文库权限拦截器
 *
 * @author : admin
 * @date : 2020-12-04 11:44
 */
@Configuration
@AllArgsConstructor
@Lazy(false)
@Slf4j
public class LibraryAuthInterceptor implements HandlerInterceptor {

    private final DataAuthConfig dataAuthConfig;
    private final DcLibraryService dcLibraryService;
    private final DcAuthConfigService dcaAuthConfigService;
    static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUrl = request.getRequestURI();
        //判断是否需要拦截
        Optional<String> first = dataAuthConfig.getLibraryUrl().stream().filter(e -> ANT_PATH_MATCHER.match(e, requestUrl)).findFirst();
        if (first.isPresent()) {
            LibraryAuthDto libraryAuthDto = new LibraryAuthDto();
            //获取用户可以查看的数据
            List<DcAuthConfig> list = dcaAuthConfigService.getAuthConfigById(new ArrayList<>());
            //获取全网或者全平台知识库
            List<String> dcLibraries = dcLibraryService.list(new LambdaQueryWrapper<DcLibrary>()
                            .eq(DcLibrary::getType, DcLibraryTypeEnum.knowledge)
                            .and(e -> e.eq(DcLibrary::getShareRole, DcLibraryReadEnum.all).or().eq(DcLibrary::getShareRole, DcLibraryReadEnum.register)))
                    .stream().map(DcLibrary::getId).collect(Collectors.toList());
            //个人知识库
            List<String> owner = dcLibraryService.getOwner();
            libraryAuthDto.setIds(owner);
            //当前用户可以访问的所有知识库包括全网全平台
            List<String> allIds = list.stream().map(e -> e.getType().equals(DcLibraryTypeEnum.knowledge) ? e.getDcId() : e.getKnowledgeId()).collect(Collectors.toList());
            //合并权限
            Map<String, List<DcAuthConfig>> map = list.stream().collect(Collectors.groupingBy(DcAuthConfig::getDcId, Collectors.toList()));
            Map<String, List<IdentifyingAuthDto>> authSign = new HashMap<>();
            map.forEach((k, v) -> {
                LinkedHashMap<String, IdentifyingAuthDto> collect = v.stream().flatMap(e -> e.getAuthSign().stream()).collect(Collectors.toMap(IdentifyingAuthDto::getId, Function.identity(), (e1, e2) -> e1.getIsSelect() ? e1 : e2.getIsSelect() ? e2 : e1, LinkedHashMap::new));
                authSign.put(k, new ArrayList<>(collect.values()));
            });
            allIds.addAll(dcLibraries);
            allIds = allIds.stream().distinct().collect(Collectors.toList());
            //设置所有可以访问文库数据
            libraryAuthDto.setAllIds(allIds);
            libraryAuthDto.setAuthSign(authSign);
            AuthSystemTool.setLibraryAuth(libraryAuthDto);
        }
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) {
        AuthSystemTool.removeLibraryAuth();
    }


}

