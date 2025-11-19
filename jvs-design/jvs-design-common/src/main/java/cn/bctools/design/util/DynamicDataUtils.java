package cn.bctools.design.util;

import cn.bctools.common.enums.DeptEnum;
import cn.bctools.common.entity.dto.DeptDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.entity.dto.UserInfoDto;
import cn.bctools.common.enums.DataScopeType;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.common.utils.function.Get;
import cn.bctools.design.constant.DynamicDataConstant;
import cn.bctools.design.crud.entity.DesignRole;
import cn.bctools.design.data.entity.DynamicDataPo;
import cn.bctools.design.data.fields.dto.DataConditionDto;
import cn.bctools.design.data.fields.dto.QueryConditionDto;
import cn.bctools.design.data.fields.dto.enums.DataConditionType;
import cn.bctools.design.data.fields.enums.DataQueryType;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.data.util.RoleUtils;
import cn.bctools.design.project.dto.DesignRoleSettingDto;
import cn.bctools.design.project.handler.DesignHandler;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.web.utils.WebUtils;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 数据管理工具类
 *
 * @Author: GuoZi
 */
public class DynamicDataUtils {
    /**
     * 将所有返回给前端的展示数据进行添加_1后缀，用于前端展示，
     */
    public static final String SUFFIX_ECHO = "_1";

    /**
     * 正则表达式相关的特殊字符
     */
    public static final char[] REGULAR_CHARS = "\\^$*+?.{}[]()|".toCharArray();

    public static final String KEY_DESIGN_ID = "dynamic_data_design_id";
    public static final String KEY_DATAMODEL_ID = "dynamic_data_model_id";
    public static final String KEY_OPERATOR = "dynamic_data_operator";
    public static final String KEY_DESIGNROLESETTINGDTO = "KEY_DESIGNROLESETTINGDTO";
    public static final String KEY_AUTH_CRITERIA = "dynamic_data_auth_criteria";
    public static final String KEY_AUTH_FREE = "dynamic_data_auth_free";
    public static final String KEY_PAGE_DESIGN_ID = "dynamic_data_page_design_id";
    public static final DesignHandler DESIGN_HANDLER = SpringContextUtil.getBean(DesignHandler.class);

    private DynamicDataUtils() {
    }

    /**
     * 获取该字段对应的回显字段
     *
     * @param fieldKey 字段名
     * @return 回显字段名
     */
    public static String getEchoFieldKey(String fieldKey) {
        return fieldKey + SUFFIX_ECHO;
    }

    /**
     * 构建组合查询条件
     *
     * @param conditions 动态查询条件
     * @return Criteria对象
     */
    public static Criteria buildDynamicGroupCriteria(List<List<QueryConditionDto>> conditions) {
        List<Criteria> groupCriteriaList = new ArrayList<>();
        conditions.forEach(conditionGroup -> {
            List<Criteria> criteriaList = buildDynamicCriteriaList(conditionGroup);
            if (ObjectNull.isNotNull(criteriaList)) {
                Criteria[] criteriaArr = new Criteria[criteriaList.size()];
                groupCriteriaList.add(new Criteria().andOperator(criteriaList.toArray(criteriaArr)));
            }
        });
        if (CollectionUtils.isEmpty(groupCriteriaList)) {
            return trueCriteria();
        }
        if (groupCriteriaList.size() == 1) {
            return groupCriteriaList.get(0);
        } else {
            Criteria[] criteriaArr = new Criteria[groupCriteriaList.size()];
            return new Criteria().orOperator(groupCriteriaList.toArray(criteriaArr));
        }
    }

    /**
     * 构建查询条件
     *
     * @param conditions 动态查询条件
     * @return Criteria对象
     */
    public static Criteria buildDynamicCriteria(List<QueryConditionDto> conditions) {
        List<Criteria> criteriaList = buildDynamicCriteriaList(conditions);
        if (ObjectNull.isNull(criteriaList)) {
            return trueCriteria();
        }
        Criteria[] criteriaArr = new Criteria[criteriaList.size()];
        return new Criteria().andOperator(criteriaList.toArray(criteriaArr));
    }

    public static List<Criteria> buildDynamicGroupCriteriaList(List<List<QueryConditionDto>> conditions) {
        if (ObjectUtils.isEmpty(conditions)) {
            return null;
        }
        List<List<Criteria>> criteriaGroupList = new ArrayList<>();
        conditions.forEach(conditionGroup -> {
            List<Criteria> criteriaList = buildDynamicCriteriaList(conditionGroup);
            if (ObjectNull.isNotNull(criteriaList)) {
                criteriaGroupList.add(criteriaList);
            }
        });
        if (CollectionUtils.isEmpty(criteriaGroupList)) {
            return null;
        }
        if (criteriaGroupList.size() == 1) {
            return criteriaGroupList.get(0);
        }
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaGroupList.forEach(criteria -> {
            Criteria[] criteriaArr = new Criteria[criteria.size()];
            criteriaList.add(trueCriteria().andOperator(criteria.toArray(criteriaArr)));
        });
        return criteriaList;
    }

    public static List<Criteria> buildDynamicCriteriaList(List<QueryConditionDto> conditions) {
        if (ObjectUtils.isEmpty(conditions)) {
            return null;
        }
        conditions = conditions.stream().filter(e -> ObjectNull.isNotNull(e.getFieldKey(), e.getEnabledQueryTypes())).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(conditions)) {
            return null;
        }
        List<Criteria> criteriaList = new ArrayList<>();
        for (QueryConditionDto condition : conditions) {
            String fieldId = condition.getFieldKey();
            Object queryValue = condition.getValue();
            DataQueryType queryType = condition.getEnabledQueryTypes();
            if (!DataQueryType.isNull.equals(queryType) && !DataQueryType.between.equals(queryType) && !DataQueryType.eq.equals(queryType)) {
                if (StringUtils.isBlank(fieldId) || ObjectUtils.isEmpty(queryValue) || Objects.isNull(queryType)) {
                    continue;
                }
                //如果是多值为查询条件,则直接改变查询的类型为in 或 not in， 如果查询条件是数组直接转换
                if (queryValue instanceof List) {
                    //判断否是自定义的格式,如果是自定义的，则需要将其解析为数组
                    Set<Object> newQueryvalue = ((List<?>) queryValue).stream().flatMap(e -> {
                        Object o = DataConditionType.get(e);
                        if (o instanceof List) {
                            return ((List<?>) o).stream();
                        } else {
                            ArrayList<Object> objects = new ArrayList<>();
                            objects.add(o);
                            return objects.stream();
                        }
                    }).collect(Collectors.toSet());
                    //如果转换后的数据与原始数据一致，
                    if (new HashSet<>(newQueryvalue).containsAll((Collection<?>) queryValue)) {
                        switch (queryType) {
                            case in:
                            case notIn:
                            case like:
                                //不处理
                                break;
                            default:
                                //其他类型全部为in
                                queryType = DataQueryType.in;

                        }
                    } else {
                        if (queryType.equals(DataQueryType.in)) {
                            //替换新的查询条件
                            queryValue = newQueryvalue;
                        } else if (queryType.equals(DataQueryType.notIn)) {
                            //替换新的查询条件
                            queryValue = newQueryvalue;
                        } else {
                            queryValue = newQueryvalue;
                        }
                    }
                } else {
                    queryValue = DataConditionType.get(queryValue);
                }
            } else {
                if (queryValue instanceof BigDecimal) {
                    queryValue = ((BigDecimal) queryValue).doubleValue();
                }
                queryValue = DataConditionType.get(queryValue);
            }

            Criteria criteria = Criteria.where(fieldId);
            switch (queryType) {
                case eq:
                    //做字符串拼接处理，如果是包含,则进行分隔处理
                    if (queryValue instanceof String) {
                        if (DynamicDataConstant.DATA_EMPTY.equals(queryValue) || "".equals(queryValue)) {
                            criteria = new Criteria().orOperator(Criteria.where(fieldId).isNull(), Criteria.where(fieldId).is(""));
//                        } else if (queryValue.toString().contains(",")) {
//                            String[] split = queryValue.toString().split(",");
//                            criteria.in(new ArrayList<String>(Arrays.asList(split)));
//                            break;
                        } else {
                            criteria.is(queryValue);
                        }
                    } else {
                        if (queryValue instanceof List) {
                            criteria.in((Collection<?>) (queryValue));
                        } else {
                            criteria.is(queryValue);
                        }
                    }
                    break;
                case ne:
                    if (DynamicDataConstant.DATA_EMPTY.equals(queryValue)) {
                        criteria = new Criteria().andOperator(Criteria.where(fieldId).ne(null), Criteria.where(fieldId).ne(""));
                    } else {
                        criteria.ne(queryValue);
                    }
                    break;
                case gt:
                    criteria.gt(queryValue);
                    break;
                case ge:
                    criteria.gte(queryValue);
                    break;
                case lt:
                    criteria.lt(queryValue);
                    break;
                case le:
                    criteria.lte(queryValue);
                    break;
                case allin:
                    if (queryValue instanceof List) {
                        List value = (List) condition.getValue();
                        criteria.regex(parseRegular(value.get(0).toString()));
                        for (int i = 1; i < value.size(); i++) {
                            //添加其它的
                            Criteria regex = Criteria.where(fieldId).regex(parseRegular(value.get(i).toString()));
                            criteriaList.add(regex);
                        }
                    } else {
                        //不是数组不处理
                        criteria.regex(parseRegular(queryValue.toString()));
                    }
                    break;
                case in:
                    if (queryValue instanceof Collection) {
                        criteria.in((Collection) queryValue);
                    } else {
                        if (JSONUtil.isTypeJSONObject(queryValue.toString())) {
                            if (JSONUtil.isTypeJSONArray(queryValue.toString())) {
                                criteria.in(JSONArray.parseArray(JSONObject.toJSONString(queryValue)));
                            }
                        } else {
                            criteria.is(queryValue);
                        }
                    }
                    break;
                case notIn:
                    if (queryValue instanceof List) {
                        criteria.nin((Collection) queryValue);
                    } else {
                        Pattern compile = Pattern.compile("^((?!" + queryValue.toString() + ").)*$", Pattern.CASE_INSENSITIVE);
                        criteria.regex(compile);
                    }
                    break;
                case like:
                    if (queryValue instanceof List) {
                        List<String> list = (List<String>) queryValue;
                        List<Criteria> criteriaLikes = new ArrayList<>();
                        for (String e : list) {
                            criteriaLikes.add(Criteria.where(fieldId).regex(parseRegular(e)));
                        }
                        criteria = new Criteria().orOperator(criteriaLikes);
                        break;
                    }
                    // 模糊查询, mongoTemplate只支持正则匹配
                    criteria.regex(parseRegular(queryValue.toString()));
                    break;
                case isNull:
                    criteria = new Criteria().orOperator(Criteria.where(fieldId).isNull(), Criteria.where(fieldId).is(""));
                    break;
                case isNotNull:
                    criteria = new Criteria().andOperator(Criteria.where(fieldId).ne(null), Criteria.where(fieldId).ne(""));
                    break;
                case between:
                    // 范围查询可参考官方文档: https://www.mongodb.com/docs/manual/reference/operator/query/elemMatch/
                    List queryValues = null;
                    if (queryValue instanceof List) {
                        queryValues = (List) condition.getValue();
                    } else {
                        if (JSONUtil.isTypeJSON(condition.getValue().toString())) {
                            if (JSONUtil.isTypeJSONArray(condition.getValue().toString())) {
                                queryValues = JSONArray.parseArray(JSONObject.toJSONString(condition.getValue()));
                            }
                        } else {
                            queryValues = Collections.emptyList();
                        }
                    }
                    if (ObjectNull.isNotNull(queryValues) && queryValues.size() == 2) {
                        criteria = new Criteria(fieldId).gte(queryValues.get(0)).lte(queryValues.get(1));
                    }
                    break;
                default:
                    break;
            }
            criteriaList.add(criteria);
        }
        return criteriaList;

    }

    /**
     * 构建查询条件
     *
     * @param criteria 动态查询条件
     * @return Criteria对象
     */
    public static Criteria initCriteria(Criteria criteria) {
        return eq(criteria, Get.name(DynamicDataPo::getDelFlag), false);
    }

    /**
     * 构建查询条件(eq)
     *
     * @param criteria 动态查询条件
     * @param key      查询字段
     * @param value    匹配内容
     * @return Criteria对象
     */
    public static Criteria eq(Criteria criteria, String key, Object value) {
        if (Objects.isNull(criteria)) {
            criteria = Criteria.where(key);
        } else {
            criteria = criteria.and(key);
        }
        return criteria.is(value);
    }

    /**
     * 构建查询条件(ne)
     *
     * @param criteria 动态查询条件
     * @param key      查询字段
     * @param value    匹配内容
     * @return Criteria对象
     */
    public static Criteria ne(Criteria criteria, String key, Object value) {
        if (Objects.isNull(criteria)) {
            criteria = Criteria.where(key);
        } else {
            criteria = criteria.and(key);
        }
        return criteria.ne(value);
    }

    /**
     * 构建查询条件(ne)
     *
     * @param criteria 动态查询条件
     * @param key      查询字段
     * @param value    匹配内容
     * @return Criteria对象
     */
    public static Criteria like(Criteria criteria, String key, Object value) {
        if (Objects.isNull(criteria)) {
            criteria = Criteria.where(key);
        } else {
            criteria = criteria.and(key);
        }
        return criteria.regex(".*(" + value.toString() + ").*");
    }

    /**
     * 数据权限过滤
     */
    public static void handleDataScope() {
        handleDataScope(DynamicDataUtils.getDesignId());
    }

    /**
     * 数据权限过滤
     *
     * @param dataModelId 模型id
     */
    public static Set<String> dataModelScope(String dataModelId) {
        return handleDataScope(DESIGN_HANDLER.getRoleSetting(dataModelId));
    }

    /**
     * 数据权限过滤
     *
     * @param designId 设计id
     */
    public static void handleDataScope(String designId) {
        handleDataScope(DESIGN_HANDLER.getDesignRole(designId));
    }

    /**
     * 接口操作权限校验
     */
    public static void checkPermit() {
        // 超级管理员
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        if (currentUser.getAdminFlag()) {
            return;
        }
        // 设计id (表单id, 列表页id)
        String designId = getDesignId();
        // 操作 (按钮名称)
        String operator = getOperator();
        if (StringUtils.isBlank(designId)) {
            throw new BusinessException("权限校验未通过缺少权限校验参数");
        }
        if (StringUtils.isBlank(operator)) {
            throw new BusinessException("缺少操作标识");
        }
        DesignRoleSettingDto settingDto = DESIGN_HANDLER.getDesignRole(designId);

        //权限处理
        //如果是创建人，不校验权限
        if (UserCurrentUtils.getUserId().equals(settingDto.getJvsAppCreateById()) || UserCurrentUtils.getCurrentUser().getAdminFlag()) {
            SystemThreadLocal.set(KEY_AUTH_CRITERIA, null);
            return;
        }
        DynamicDataUtils.handleDesignDataScope(settingDto.getDataModelRole());
    }

    /**
     * 数据权限过滤
     *
     * @param settingDto 权限组配置
     * @return
     */
    public static Set<String> handleDataScope(DesignRoleSettingDto settingDto) {
        // 兼容未启用轻应用版本功能的配置（统一权限配置后，没有跳过数据权限的配置）
        if (Boolean.FALSE.equals(settingDto.getEnableVersionFeature())) {
            // 跳过数据权限 (列表设计id不为空 && (未设置列表“是否跳过数据权限” || 跳过数据权限))
            String designId = WebUtils.getRequest().getHeader("designId");
            //获取设计 id为空
            boolean designIdNull = ObjectNull.isNull(designId);
            //获取列表的设计 id是否不为空
            boolean notBlank = StringUtils.isNotBlank(DynamicDataUtils.getPageDesignId());
            //获取是否跳过数据权限是否为空
            boolean aNull = ObjectNull.isNull(settingDto.getStepDataPermission());
            //获取不跳过数据权限
            boolean equals = Boolean.FALSE.equals(settingDto.getStepDataPermission());
            // 列表不为空  && ( 跳过数据权限为空 || 不跳过数据权限)
            boolean notProcessPermissions = notBlank && (aNull || equals);
            //  (列表不为空  && ( 跳过数据权限为空 || 不跳过数据权限)) &&  获取设计 id为空
            if (notProcessPermissions && designIdNull) {
                SystemThreadLocal.set(KEY_AUTH_CRITERIA, null);
                return new HashSet<>();
            }
        }
        //权限处理如果是创建人，不校验权限
        String userId = UserCurrentUtils.getUserId();
        if (userId.equals(settingDto.getJvsAppCreateById()) || UserCurrentUtils.getCurrentUser().getAdminFlag()) {
            SystemThreadLocal.set(KEY_AUTH_CRITERIA, null);
            return new HashSet<>();
        }
        //如果没有配置数据权限， 并启用了工作流，则使用工作流默认权限
        if (ObjectNull.isNotNull(settingDto.getEnableWorkflow()) && settingDto.getEnableWorkflow() && ObjectNull.isNull(settingDto.getDataModelRole())) {
            Criteria criteria = new Criteria().orOperator(
                    //创建人是自己，
                    Criteria.where(Get.name(DynamicDataPo::getCreateById)).is(userId),
                    //任务发起人是自己，
                    Criteria.where("jvsFlowTask.createById").regex(".*?" + userId + ".*"),
                    //待办人有自己
                    Criteria.where("jvsFlowTask.flowTaskPersons").regex(".*?" + userId + ".*"),
                    //抄送人有自己
                    Criteria.where("jvsFlowTask.carbonCopyPersons").regex(".*?" + userId + ".*"),
                    //历史审批人有自己
                    Criteria.where("jvsFlowTask.historyPersons").regex(".*?" + userId + ".*")
            );
            List<Criteria> objects = new ArrayList<>();
            objects.add(criteria);
            SystemThreadLocal.set(KEY_AUTH_CRITERIA, objects);
            return new HashSet<>();
        }
        DynamicDataUtils.handleDesignDataScope(settingDto.getDataModelRole());
        if (ObjectNull.isNotNull(settingDto.getDataModelRole())) {
            return settingDto.getDataModelRole().stream().filter(e -> ObjectNull.isNotNull(e.getConditionList())).flatMap(e -> e.getConditionList().stream()).map(DataConditionDto::getKey).collect(Collectors.toSet());
        } else {
            return new HashSet<>();
        }
    }


    /**
     * 处理设计套件级别的数据权限, 并将MongoDB的查询类放到线程上下文中
     *
     * @param role 设计套件权限组
     */
    public static void handleDesignDataScope(List<DesignRole> role) {
        if (ObjectNull.isNotNull(role) && role.stream().filter(e -> ObjectNull.isNotNull(e.getScopeList())).findAny().isPresent()) {
            List<Criteria> criteriaList = role.stream().filter(RoleUtils::hasDataPermit).flatMap(e -> DynamicDataUtils.buildCriteria(e).stream())
                    .filter(ObjectNull::isNotNull)
                    .collect(Collectors.toList());
            if (ObjectNull.isNull(criteriaList)) {
                SystemThreadLocal.set(KEY_AUTH_CRITERIA, null);
            } else {
                //如果只有一组数据满足时，直接拼接
                SystemThreadLocal.set(KEY_AUTH_CRITERIA, criteriaList);
            }
        } else {
            SystemThreadLocal.set(KEY_AUTH_CRITERIA, null);
        }
    }

    /**
     * 处理正则的特殊字符
     *
     * @param str 待处理字符串
     * @return 处理后的字符串
     */
    public static String parseRegular(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        for (char regularChar : REGULAR_CHARS) {
            str = str.replace("" + regularChar, "\\" + regularChar);
        }
        return str;
    }

    /**
     * 根据权限组类型生成查询条件
     *
     * @param designRole 设计套件权限组
     * @return MongoDB查询条件
     */
    private static List<Criteria> buildCriteria(DesignRole designRole) {
        if (Objects.isNull(designRole)) {
            return null;
        }
        List<DataScopeType> scopeList = designRole.getScopeList();
        if (ObjectUtils.isEmpty(scopeList)) {
            return null;
        }
        final List<DataConditionDto> conditionList = designRole.getConditionList();
        List<Criteria> criteriaList = scopeList.stream().map(r -> DynamicDataUtils.buildCriteria(r, conditionList)).collect(Collectors.toList());
        return criteriaList;
    }

    /**
     * 根据数据权限类型生成查询条件
     *
     * @param dataScopeType 数据权限分类
     * @param conditionList 自定义数据条件
     * @return MongoDB查询条件
     */
    private static Criteria buildCriteria(DataScopeType dataScopeType, List<DataConditionDto> conditionList) {
        if (Objects.isNull(dataScopeType)) {
            return falseCriteria();
        }
        UserInfoDto<? extends UserDto> userInfo = UserCurrentUtils.init();
        UserDto user = userInfo.getUserDto();
        switch (dataScopeType) {
            case all:
            case all_dept:
            case all_job:
                return trueCriteria();
            case curr_job:
                return Criteria.where(Get.name(DynamicDataPo::getJobId)).is(user.getJobId());
            case flowTaskPersons:
                //审核人或抄送人是自己的是情况下才显示
                String userId = user.getId();
                Criteria criteria = new Criteria().orOperator(
                        //创建人是自己，
                        Criteria.where(Get.name(DynamicDataPo::getCreateById)).is(userId),
                        //任务发起人是自己，
                        Criteria.where("jvsFlowTask.createById").regex(".*?" + userId + ".*"),
                        //待办人有自己
                        Criteria.where("jvsFlowTask.flowTaskPersons").regex(".*?" + userId + ".*"),
                        //抄送人有自己
                        Criteria.where("jvsFlowTask.carbonCopyPersons").regex(".*?" + userId + ".*"),
                        //历史审批人有自己
                        Criteria.where("jvsFlowTask.historyPersons").regex(".*?" + userId + ".*")
                );
                return criteria;
            case curr_dept:
                return Criteria.where(Get.name(DynamicDataPo::getDeptId)).in(user.getDept().stream().filter(e-> DeptEnum.dept.equals(e.getType())).map(DeptDto::getDeptId).collect(Collectors.toSet()));
            case self:
                return Criteria.where(Get.name(DynamicDataPo::getCreateById)).is(user.getId());
            case curr_dept_tree:
                List<String> childDeptIds = userInfo.getChildDeptIds();
                if (ObjectUtils.isEmpty(childDeptIds)) {
                    return falseCriteria();
                }
                return Criteria.where(Get.name(DynamicDataPo::getDeptId)).in(childDeptIds);
            case form_item:
                return buildCriteria(conditionList);
            case task_pending_approval:
                return Criteria.where("jvsFlowTask.flowTaskPersons").regex(".*?" + user.getId() + ".*");
            default:
                break;
        }
        return falseCriteria();
    }

    /**
     * 根据自定义数据条件生成查询条件
     *
     * @param conditionList 自定义数据条件
     * @return MongoDB查询条件
     */
    private static Criteria buildCriteria(List<DataConditionDto> conditionList) {
        if (ObjectUtils.isEmpty(conditionList)) {
            return falseCriteria();
        }
        Criteria criteria = null;
        for (DataConditionDto conditionDto : conditionList) {
            String key = conditionDto.getKey();
            //目前已经支持了日期查询条件。区间
            Object value = conditionDto.getValue().get(0);
            Object o = null;
            //替换规则的值
            List<Object> collect = conditionDto.getValue().stream().flatMap(e -> {
                Object obj = DataConditionType.get(e);
                if (obj instanceof Collection) {
                    return ((Collection<?>) obj).stream();
                } else {
                    return Collections.singletonList(obj).stream();
                }
            }).collect(Collectors.toList());
            if (collect.size() == 1) {
                //如果转换失败表示不是范围的数据筛选，否则需要替换 o
                Object val = collect.get(0);
                if (val.equals(value)) {
                    o = val;
                } else {
                    o = collect;
                }
            } else {
                o = collect;
            }


            if ((!conditionDto.getOperator().equals(DataQueryType.in)) && (!conditionDto.getOperator().equals(DataQueryType.notIn))) {
                if (!value.equals(o)) {
                    if (o instanceof List) {
                        conditionDto.setOperator(DataQueryType.in);
                    }
                }
            }
            value = o;

            DataQueryType operator = conditionDto.getOperator();
            if (StringUtils.isBlank(key) || Objects.isNull(operator)) {
                continue;
            }

            switch (operator) {
                case eq:
                    criteria = eq(criteria, key, value);
                    break;
                case ne:
                    criteria = ne(criteria, key, value);
                    break;
                case in:
                    if (value instanceof Collection) {
                        criteria = Criteria.where(key).in((Collection<?>) value);
                    } else {
                        criteria = Criteria.where(key).is(value);
                    }
                    break;
                case notIn:
                    if (value instanceof List) {
                        criteria.nin(value);
                    } else {
                        if (Objects.isNull(criteria)) {
                            criteria = Criteria.where(key);
                        } else {
                            criteria = criteria.and(key);
                        }
                        Pattern compile = Pattern.compile("^((?!" + value + ").)*$", Pattern.CASE_INSENSITIVE);
                        criteria.regex(compile);
                    }
                    break;
                case like:
                    criteria = like(criteria, key, value);
                    break;
                default:
                    break;
            }
        }
        if (Objects.isNull(criteria)) {
            criteria = falseCriteria();
        }
        return criteria;
    }

    public static Criteria trueCriteria() {
        return Criteria.where(DynamicDataService.MONGO_ID).exists(true);
    }

    public static Criteria falseCriteria() {
        return Criteria.where(DynamicDataService.MONGO_ID).exists(false);
    }

    public static String getDesignId() {
        return SystemThreadLocal.get(KEY_DESIGN_ID);
    }

    public static String getOperator() {
        return SystemThreadLocal.get(KEY_OPERATOR);
    }

    public static void setDesignId(String designId) {
        if (StringUtils.isBlank(designId)) {
            return;
        }
        SystemThreadLocal.set(KEY_DESIGN_ID, designId);
    }

    public static void setOperator(String operator) {
        if (StringUtils.isBlank(operator)) {
            return;
        }
        SystemThreadLocal.set(KEY_OPERATOR, operator);
    }

    public static void setDataModelId(String dataModelId) {
        if (StringUtils.isBlank(dataModelId)) {
            return;
        }
        SystemThreadLocal.set(KEY_DATAMODEL_ID, dataModelId);
    }

    public static String getDataModelId() {
        return SystemThreadLocal.get(KEY_DATAMODEL_ID);
    }

    /**
     * 数据权限放行
     */
    public static void freePermit() {
        SystemThreadLocal.set(KEY_AUTH_FREE, true);
    }

    public static List<Criteria> getAuthCriteria() {
        Boolean isFree = SystemThreadLocal.get(KEY_AUTH_FREE);
        if (Boolean.TRUE.equals(isFree)) {
            List<Criteria> objects = new ArrayList<>();
            objects.add(trueCriteria());
            return objects;
        }
        Object o = SystemThreadLocal.get(KEY_AUTH_CRITERIA);
        if (ObjectNull.isNull(o)) {
            return new ArrayList<>();
        }
        return (List<Criteria>) o;
    }

    /**
     * 清空回显字段
     *
     * @param data 数据
     */
    public static void clearEcho(Map<String, Object> data) {
        if (ObjectUtils.isEmpty(data)) {
            return;
        }
        data.keySet().removeIf(key -> key.endsWith(SUFFIX_ECHO));
    }

    public static void setDto(DesignRoleSettingDto designRole) {
        SystemThreadLocal.set(KEY_DESIGNROLESETTINGDTO, designRole);
    }

    public static DesignRoleSettingDto getDto() {
        return SystemThreadLocal.get(KEY_DESIGNROLESETTINGDTO);
    }

    public static String getPageDesignId() {
        return SystemThreadLocal.get(KEY_PAGE_DESIGN_ID);
    }

    public static void setPageDesignId(String pageDesignId) {
        if (StringUtils.isBlank(pageDesignId)) {
            return;
        }
        SystemThreadLocal.set(KEY_PAGE_DESIGN_ID, pageDesignId);
    }

    /**
     * 查询条件
     *
     * @param list         多个查询条件
     * @param authCriteria 数据权限查询条件
     * @param andOr        为true 为与  false 为 或
     * @return
     */
    public static Query andOr(List<Criteria> list, Criteria authCriteria, Boolean andOr) {
        return new Query(andOrCriteria(list, authCriteria, andOr));
    }

    public static Criteria andOrCriteria(List<Criteria> list, Criteria authCriteria, Boolean andOr) {
        if (Objects.isNull(authCriteria)) {
            authCriteria = trueCriteria();
        }
        //将数据权限和列表查询进行组件
        if (ObjectNull.isNull(list)) {
            return authCriteria;
        }
        //将数据权限和列表查询进行组件
        if (andOr) {
            if (ObjectNull.isNull(list)) {
                return authCriteria;
            }
            list.add(authCriteria);
            return DynamicDataUtils.trueCriteria().andOperator(list);
        } else {
            return trueCriteria().orOperator(list).andOperator(authCriteria);
        }
    }
}
