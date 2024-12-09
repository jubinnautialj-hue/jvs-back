package cn.bctools.design.identification.service.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.identification.entity.Identification;
import cn.bctools.design.identification.mapper.IdentificationMapper;
import cn.bctools.design.identification.service.IdentificationService;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.handler.IJvsDesigner;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.project.service.JvsAppVersionService;
import cn.bctools.design.sqlInjector.MapperMethodHandler;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.rule.entity.enums.RuleExceptionEnum;
import cn.bctools.rule.exception.RuleException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 */
@Service
@AllArgsConstructor
public class IdentificationServiceImpl extends ServiceImpl<IdentificationMapper, Identification> implements IdentificationService, IJvsDesigner {

    private final JvsAppService appService;
    private final JvsAppVersionService appVersionService;
    private final MapperMethodHandler mapperMethodHandler;
    private static final PathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void checkExistsIdentifier(boolean whetherInsert, String appId, String identifier, String designId) {
        List<Identification> identifications = list(Wrappers.<Identification>lambdaQuery().eq(Identification::getIdentifier, identifier));
        if (ObjectNull.isNull(identifications)) {
            return;
        }
        // 校验是否是其它应用的标识
        // 标识租户级唯一，所以若标识已存在，那么identifications集合必定是同一个应用的不同版本，所以获取那个应用id都算是同一个应用
        String identificationAppId = identifications.get(0).getJvsAppId();
        // 若当前应用及其所有版本应用都不包含该标识所属应用，则不可以创建标识
        List<String> appIds = identifications.stream().map(Identification::getJvsAppId).collect(Collectors.toList());
        boolean contains = appVersionService.getVersionTypeAppIdByAffiliationId(appId)
                .stream()
                .anyMatch(appIds::contains);
        if (Boolean.FALSE.equals(contains)) {
            JvsApp jvsApp = appService.getAppById(identificationAppId);
            throw new BusinessException("该标识符在应用中已存在", jvsApp.getName());
        }

        // 是当前应用的标识
        Optional<Identification> identificationOptional = identifications.stream().filter(identification -> identification.getJvsAppId().equals(appId)).findFirst();
        if (identificationOptional.isPresent()) {
            Identification identification = identificationOptional.get();
            // 新增校验，标识已存在，则直接抛异常
            if (whetherInsert) {
                JvsApp jvsApp = appService.getAppById(identification.getJvsAppId());
                throw new BusinessException("该标识符在应用中已存在", jvsApp.getName());
            }

            // 修改校验
            if (Boolean.FALSE.equals(designId.equals(identification.getDesignId()))) {
                JvsApp jvsApp = appService.getAppById(identification.getJvsAppId());
                throw new BusinessException("该标识符在应用中已存在", jvsApp.getName());
            }
        }
    }

    @Override
    public List<Identification> findByIdentifiers(List<String> identifiers) {
        return list(Wrappers.<Identification>lambdaQuery().in(Identification::getIdentifier, identifiers));
    }


    @Override
    public void updateDesignName(String designId, String designName) {
        if (ObjectNull.isNull(designId)) {
            return;
        }
        update(Wrappers.<Identification>lambdaUpdate()
                .set(Identification::getDesignName, designName)
                .eq(Identification::getDesignId, designId));
    }

    @Override
    public Identification getByIdentifierRule(String ruleIdentification, String appId, Map<String, Object> map) {
        List<Identification> list = list(Wrappers.query(new Identification().setDesignType(DesignType.rule).setJvsAppId(appId)));
        for (Identification e : list) {
            if (PATH_MATCHER.match(e.getIdentifier(), ruleIdentification)) {
                //将数据替换到参数中
                map.putAll(PATH_MATCHER.extractUriTemplateVariables(e.getIdentifier(), ruleIdentification));
                return e;
            }
        }
        return null;
    }

    /**
     * 如果查询出来是多个，只取第一个
     *
     * @param appIdentification
     * @return
     */
    @Override
    public Identification getByIdentifierApp(String appIdentification) {
        List<Identification> identifications = list(Wrappers.query(new Identification().setIdentifier(appIdentification)));
        List<String> modeAppIds = appVersionService.getVersionTypeAppIds(ModeUtils.getMode());
        Optional<Identification> optionalIdentification = identifications.stream()
                .filter(identification -> modeAppIds.contains(identification.getJvsAppId())).findFirst();
        if (optionalIdentification.isPresent()) {
            return optionalIdentification.get();
        }
        throw new RuleException(RuleExceptionEnum.请求地址不存在或未配置标识);
    }

    @Override
    public void delete(String appId, String designId) {
        remove(Wrappers.query(new Identification().setDesignId(designId).setJvsAppId(appId)));
    }

    @Override
    public void beforeAppDeleted(String appId) {
        mapperMethodHandler.deletePhysical(this, Wrappers.<Identification>lambdaQuery().eq(Identification::getJvsAppId, appId));
    }

    @Override
    public List<Identification> getIdentificationModel(String... identifiers) {
        // 根据标识查询标识设计
        List<Identification> identifications = this.list(Wrappers.<Identification>lambdaQuery()
                .in(ObjectNull.isNotNull(Arrays.stream(identifiers).collect(Collectors.toList())), Identification::getIdentifier, identifiers));
        if (ObjectNull.isNull(identifications)) {
            return identifications;
        }
        // 根据当前模式获取所有应用id
        List<String> modeAppIds = appVersionService.getVersionTypeAppIds(ModeUtils.getMode());
        // 从标识的查询结果中，过滤出当前模式的应用id相关的标识
        return identifications.stream()
                .filter(identification -> modeAppIds.contains(identification.getJvsAppId()))
                .collect(Collectors.toList());
    }

}
