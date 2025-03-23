package cn.bctools.document.service.impl;

import cn.bctools.auth.api.api.AuthDeptServiceApi;
import cn.bctools.auth.api.api.AuthJobServiceApi;
import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.auth.api.dto.SysDeptDto;
import cn.bctools.auth.api.dto.SysJobDto;
import cn.bctools.auth.api.dto.UserGroupDto;
import cn.bctools.common.entity.dto.DeptDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.IdGenerator;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.document.dto.AddAuthConfigDto;
import cn.bctools.document.dto.IdentifyingAuthDto;
import cn.bctools.document.entity.DcAuthConfig;
import cn.bctools.document.entity.DcIdentifying;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.enums.DataAuthTypeEnum;
import cn.bctools.document.entity.enums.DcLibraryReadEnum;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.entity.enums.IdentifyingKeyEnum;
import cn.bctools.document.mapper.DcAuthConfigMapper;
import cn.bctools.document.service.DcAuthConfigService;
import cn.bctools.document.service.DcIdentifyingService;
import cn.bctools.document.service.DcLibraryService;
import cn.bctools.document.service.MessageConfigService;
import cn.bctools.document.vo.req.DcLocationOtherDto;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Auto Generator
 */
@Service
@Slf4j
@AllArgsConstructor
public class DcAuthConfigServiceImpl extends ServiceImpl<DcAuthConfigMapper, DcAuthConfig> implements DcAuthConfigService {
    private final AuthUserServiceApi authUserServiceApi;
    private final AuthDeptServiceApi authDeptServiceApi;
    private final AuthJobServiceApi authJobServiceApi;
    private final DcIdentifyingService dciIdentifyingService;
    private final MessageConfigService messageConfigService;


    @Override
    public Boolean childrenIsAuth(String id) {
        LambdaQueryWrapper<DcAuthConfig> eq = getDcAuthConfigLambdaQueryWrapper();
        eq.apply("JSON_CONTAINS(path_id, CONCAT('\"',{0},'\"'))", id);
        long count = this.count(eq);
        return count > 0;
    }

    /**
     * 需要获取当前用户的信息作为查询条件的公共方法
     *
     * @return 返回查询条件
     */
    private LambdaQueryWrapper<DcAuthConfig> getDcAuthConfigLambdaQueryWrapper() {
        UserDto userDto = UserCurrentUtils.getCurrentUser();
        //群组id
        List<UserGroupDto> userGroupDtoList = Optional.ofNullable(authUserServiceApi.userGroup(userDto.getId()).getData()).orElse(new ArrayList<>());
        //获取部门id 通过当前用户的部门id 获取上级所有的部门id
        List<String> deptIds = new ArrayList<>();
        List<SysDeptDto> data = authDeptServiceApi.getAll().getData();
        if (!data.isEmpty() && !userDto.getDept().isEmpty()) {
            List<String> list = userDto.getDept().stream().map(DeptDto::getDeptId).collect(Collectors.toList());
            List<SysDeptDto> deptDto = data.stream().filter(e -> list.contains(e.getId())).collect(Collectors.toList());
            if (!deptDto.isEmpty()) {
                //获取父级
                deptIds.addAll(getDeptIds(deptDto, data));
            }
            //添加自己
            deptIds.addAll(list);
        }
        List<String> roles = UserCurrentUtils.init().getRoles();
        List<String> jobList = authJobServiceApi.getUserById(userDto.getId()).getData().stream().map(SysJobDto::getId).collect(Collectors.toList());
        //获取有权限的数据
        return new LambdaQueryWrapper<DcAuthConfig>()
                .and(v ->
                        v.eq(DcAuthConfig::getDataAuthType, DataAuthTypeEnum.user)
                                .eq(DcAuthConfig::getUserId, userDto.getId())
                                .or(!jobList.isEmpty(), e -> e.eq(DcAuthConfig::getDataAuthType, DataAuthTypeEnum.job).in(DcAuthConfig::getUserId, jobList))
                                .or(!deptIds.isEmpty(), e -> e.eq(DcAuthConfig::getDataAuthType, DataAuthTypeEnum.dept).in(DcAuthConfig::getUserId, deptIds))
                                .or(!roles.isEmpty(), e -> e.eq(DcAuthConfig::getDataAuthType, DataAuthTypeEnum.role).in(DcAuthConfig::getUserId, roles))
                                .or(!userGroupDtoList.isEmpty(), e -> e.eq(DcAuthConfig::getDataAuthType, DataAuthTypeEnum.group)
                                        .in(DcAuthConfig::getUserId, userGroupDtoList.stream().map(UserGroupDto::getId).collect(Collectors.toList()))));
    }

    @Override
    public List<String> parentLevelToGetAuth(String id, Boolean fileIs) {
        DcLibraryService libraryService = SpringContextUtil.getBean(DcLibraryService.class);
        DcLibrary byId = libraryService.getById(id);
        String knowId = id;
        if (byId.getType().equals(DcLibraryTypeEnum.directory)) {
            knowId = byId.getKnowledgeId();
        }
        DcLibrary know = libraryService.getById(knowId);
        if (know.getShareRole().equals(DcLibraryReadEnum.all) || know.getShareRole().equals(DcLibraryReadEnum.register)) {
            return libraryService.list(new LambdaQueryWrapper<DcLibrary>().select(DcLibrary::getId).eq(DcLibrary::getKnowledgeId, knowId))
                    .stream()
                    .map(DcLibrary::getId)
                    .collect(Collectors.toList());
        }

        //获取有没有子集数据或者本身设置了权限的
        LambdaQueryWrapper<DcAuthConfig> queryWrapper = new QueryWrapper<DcAuthConfig>().lambda()
                .apply("JSON_CONTAINS(path_id, CONCAT('\"',{0},'\"'))", id)
                .or()
                .eq(DcAuthConfig::getDcId, id)
                .or()
                .eq(DcAuthConfig::getDcId, knowId);
        //获取上级是否有权限
        for (String s : byId.getPathId()) {
            queryWrapper = queryWrapper.or().apply("JSON_CONTAINS(path_id, CONCAT('\"',{0},'\"'))", s);
        }
        List<String> ids = this.list(queryWrapper).stream().map(DcAuthConfig::getDcId).collect(Collectors.toList());
        //通过文档id进行权限过滤
        List<DcAuthConfig> authConfigById = this.getAuthConfigById(ids);
        if (authConfigById.isEmpty()) {
            return new ArrayList<>();
        }

        List<DcLibraryTypeEnum> typeEnums = Arrays.asList(DcLibraryTypeEnum.knowledge, DcLibraryTypeEnum.directory);
        //获取文件
        List<DcAuthConfig> dcIds = authConfigById.stream().filter(e -> !typeEnums.contains(e.getType())).collect(Collectors.toList());
        //判断是否存在文件夹 如果存在文件夹或者知识库 需要获取下面的所有文件
        Map<DcLibraryTypeEnum, List<DcAuthConfig>> map = authConfigById.stream()
                .filter(e -> typeEnums.contains(e.getType()))
                .collect(Collectors.groupingBy(DcAuthConfig::getType, Collectors.toList()));
        map.forEach((k, v) -> {
            List<String> collect = v.stream().map(DcAuthConfig::getDcId).collect(Collectors.toList());
            LambdaQueryWrapper<DcLibrary> wrapper = new LambdaQueryWrapper<DcLibrary>().select(DcLibrary::getId);
            if (k.equals(DcLibraryTypeEnum.knowledge)) {
                wrapper.in(DcLibrary::getKnowledgeId, collect);
            } else {
                wrapper.in(DcLibrary::getParentId, collect);
            }
            //只需要获取当前目录下面的数据
            wrapper = wrapper.apply("JSON_CONTAINS(path_id, CONCAT('\"',{0},'\"'))", id);
            List<DcAuthConfig> list = libraryService.list(wrapper).stream().map(e -> new DcAuthConfig().setDcId(e.getId()).setType(e.getType())).collect(Collectors.toList());
            if (!list.isEmpty()) {
                dcIds.addAll(list);
            }
        });
        Stream<DcAuthConfig> stream = dcIds.stream();
        if (fileIs) {
            stream = stream.filter(e -> DcLibraryTypeEnum.isFile(e.getType()));
        }
        return stream.map(DcAuthConfig::getDcId).distinct().collect(Collectors.toList());
    }

    @Override
    public List<AddAuthConfigDto.UserInfo> getUser(String id) {
        //获取文件或者文件夹信息
        DcLibraryService dcLibraryService = SpringContextUtil.getBean(DcLibraryService.class);
        DcLibrary dcLibrary = dcLibraryService.getById(id);
        LambdaQueryWrapper<DcAuthConfig> queryWrapper = new LambdaQueryWrapper<DcAuthConfig>().eq(DcAuthConfig::getDcId, id);
        if (!dcLibrary.getType().equals(DcLibraryTypeEnum.knowledge)) {
            //需要获取上级的用户
            for (String s : dcLibrary.getPathId()) {
                queryWrapper = queryWrapper.or().apply("JSON_CONTAINS(path_id, CONCAT('\"',{0},'\"'))", s);
            }
        }
        return this.list(queryWrapper)
                .stream()
                .map(e -> new AddAuthConfigDto.UserInfo().setDataAuthType(e.getDataAuthType()).setUserId(e.getUserId()))
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<DcAuthConfig> getAuthConfig(IdentifyingKeyEnum identifyingKeyEnum) {
        List<DcAuthConfig> authConfigById = getAuthConfigById(new ArrayList<>());
        if (authConfigById.isEmpty()) {
            return authConfigById;
        }
        return authConfigById.stream()
                .filter(e -> e.getType().equals(DcLibraryTypeEnum.knowledge))
                //过滤需要判断的标识是否存在
                .filter(e -> e.getAuthSign()
                        .stream()
                        .filter(v -> v.getIdentifyingKey().equals(identifyingKeyEnum) && v.getIsSelect()).count() > 0)
                .collect(Collectors.toList());
    }


    @Override
    public void addOrUpdateAuthConfig(DcLibrary dcLibrary, List<AddAuthConfigDto> authConfigs, Boolean isAddPossessor) {
        //获取默认的查看权限
        List<DcIdentifying> defaultIdentifying = dciIdentifyingService.typeGetList(IdentifyingKeyEnum.view);
        List<DcAuthConfig> list = new ArrayList<>();
        //重构权限
        if (authConfigs == null) {
            return;
        }
        List<DcAuthConfig> collect = authConfigs.stream().filter(e -> e.getUserInfoList() != null && !e.getUserInfoList().isEmpty())
                .flatMap(e -> {
                    //设置默认查看权限
                    if (e.getIdcIdentifying() == null || e.getIdcIdentifying().isEmpty()) {
                        List<IdentifyingAuthDto> idcIdentifying = JSONArray.parseArray(JSONObject.toJSONString(defaultIdentifying), IdentifyingAuthDto.class);
                        log.info("权限copy的数据为:{}", JSONObject.toJSONString(idcIdentifying));
                        idcIdentifying.stream().peek(v -> v.setIsSelect(true)).collect(Collectors.toList());
                        e.setIdcIdentifying(idcIdentifying);
                    }
                    String keyGroup = IdGenerator.getIdStr();
                    return e.getUserInfoList().stream()
                            .map(v ->
                                    new DcAuthConfig()
                                            .setDataAuthType(v.getDataAuthType())
                                            .setHeadImg(v.getHeadImg())
                                            .setName(v.getName())
                                            .setPathId(dcLibrary.getPathId())
                                            .setGroupKey(keyGroup)
                                            .setAuthSign(JSONArray.parseArray(JSONObject.toJSONString(e.getIdcIdentifying()), IdentifyingAuthDto.class))
                                            .setDcId(dcLibrary.getId())
                                            .setType(dcLibrary.getType())
                                            .setKnowledgeId(dcLibrary.getType().equals(DcLibraryTypeEnum.knowledge) ? dcLibrary.getId() : dcLibrary.getKnowledgeId())
                                            .setUserId(v.getUserId()));
                })
                .collect(Collectors.toList());
        list.addAll(collect);
        if (isAddPossessor) {
            //如果是知识库,创建所属人
            List<DcIdentifying> identifyingList = dciIdentifyingService.basisTypeToPossessor(null, Boolean.TRUE);
            List<IdentifyingAuthDto> authSign = JSONArray.parseArray(JSONObject.toJSONString(identifyingList), IdentifyingAuthDto.class);
            //设置默认值
            authSign.stream().peek(e -> e.setIsSelect(true).setSelect(true)).collect(Collectors.toList());
            UserDto user = UserCurrentUtils.getCurrentUser();
            list.add(new DcAuthConfig()
                    .setDataAuthType(DataAuthTypeEnum.user)
                    .setPathId(new ArrayList<>())
                    .setAuthSign(authSign)
                    .setHeadImg(user.getHeadImg())
                    .setName(user.getRealName())
                    .setDcId(dcLibrary.getId())
                    .setKnowledgeId(dcLibrary.getKnowledgeId())
                    .setUserId(UserCurrentUtils.getUserId()));
        }
        //查询原有的所有权限重新不包括所有者
        LambdaQueryWrapper<DcAuthConfig> queryWrapper = new LambdaQueryWrapper<DcAuthConfig>()
                .eq(DcAuthConfig::getDcId, dcLibrary.getId());
        //如果知识库 需要排除所有者权限
        if (dcLibrary.getType().equals(DcLibraryTypeEnum.knowledge)) {
            queryWrapper.ne(DcAuthConfig::getUserId, dcLibrary.getPossessor());
        }
        //如果不创建所属人 就表示是新建知识库 新建知识库不同步消息的人员信息
        if (!(dcLibrary.getType().equals(DcLibraryTypeEnum.knowledge) && isAddPossessor)) {
            //获取原来的用户信息 做对比
            List<DcAuthConfig> dcAuthConfigs = this.list(queryWrapper);
            List<String> userIds = authConfigs.stream().flatMap(e -> e.getUserInfoList().stream().map(AddAuthConfigDto.UserInfo::getUserId)).collect(Collectors.toList());
            //获取没有了的用户id
            List<String> removeUserIds = dcAuthConfigs.stream()
                    .map(DcAuthConfig::getUserId)
                    .filter(e -> !userIds.contains(e))
                    .collect(Collectors.toList());
            messageConfigService.syncUserInfo(removeUserIds, dcLibrary);
        }
        this.remove(queryWrapper);
        if (!list.isEmpty()) {
            log.info("权限入库的数据为:{}", JSONObject.toJSONString(list));
            this.saveBatch(list);
        }
    }

    @Override
    public void moveUpdateAuthConfig(DcLocationOtherDto dcLocationOtherDto, List<String> pathIds) {
        LambdaQueryWrapper<DcAuthConfig> queryWrapper = new QueryWrapper<DcAuthConfig>().lambda()
                .apply("JSON_CONTAINS(path_id, CONCAT('\"',{0},'\"'))", dcLocationOtherDto.getId())
                .or()
                //添加本身
                .eq(DcAuthConfig::getDcId, dcLocationOtherDto.getId());
        List<DcAuthConfig> dcAuthConfigs = this.list(queryWrapper);
        if (!dcAuthConfigs.isEmpty()) {
            //修改所有下级的路径数据与
            dcAuthConfigs.stream().peek(e -> {
                List<String> newPathId = BeanUtil.copyToList(pathIds, String.class);
                if (!e.getDcId().equals(dcLocationOtherDto.getId())) {
                    List<String> pathId = e.getPathId();
                    int index = pathId.indexOf(dcLocationOtherDto.getId());
                    List<String> list = pathId.subList(index, pathId.size());
                    newPathId.addAll(list);
                } else {
                    e.setParentId(dcLocationOtherDto.getParentId());
                }
                e.setPathId(newPathId).setKnowledgeId(dcLocationOtherDto.getKnowledgeId());
            }).collect(Collectors.toList());
            //修改数据
            this.updateBatchById(dcAuthConfigs);
        }
    }


    @Override
    public List<DcAuthConfig> getAuthConfigById(List<String> dcIds) {
        LambdaQueryWrapper<DcAuthConfig> eq = getDcAuthConfigLambdaQueryWrapper();
        //文档id不为空 指定查询权限
        if (!dcIds.isEmpty()) {
            eq.and(e -> e.in(DcAuthConfig::getDcId, dcIds));
        }
        return this.list(eq);
    }

    /**
     * 通过部门id 获取上级部门id
     *
     * @param deptId 部门id
     */
    public List<String> getDeptIds(List<SysDeptDto> deptId, List<SysDeptDto> data) {
        //获取上级id
        List<String> list = deptId.stream().map(SysDeptDto::getParentId).filter(StrUtil::isNotBlank).collect(Collectors.toList());
        if (!list.isEmpty()) {
            //获取上级部门详细
            List<SysDeptDto> deptDto = data.stream().filter(e -> list.contains(e.getId())).collect(Collectors.toList());
            list.addAll(getDeptIds(deptDto, data));
        }
        return list;

    }
}
