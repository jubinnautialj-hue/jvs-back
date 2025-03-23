package cn.bctools.data.factory.consanguinity.view.service.impl;

import cn.bctools.chart.api.JvsChartApi;
import cn.bctools.chart.api.dto.IsDeleteDto;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.consanguinity.view.dto.Neo4jDto;
import cn.bctools.data.factory.consanguinity.view.dto.Neo4jEdgesDto;
import cn.bctools.data.factory.consanguinity.view.dto.Neo4jNodeDto;
import cn.bctools.data.factory.consanguinity.view.entity.ConsanguinityViewEntity;
import cn.bctools.data.factory.consanguinity.view.service.ConsanguinityViewService;
import cn.bctools.data.factory.entity.JvsDataFactory;
import cn.bctools.data.factory.enums.ConsanguinityViewTypeEnum;
import cn.bctools.data.factory.service.JvsDataFactoryService;
import cn.bctools.report.api.JvsDataReportApi;
import cn.bctools.report.api.ReportLineageViewApi;
import cn.bctools.screen.api.ScreenLineageViewApi;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Slf4j
public class ConsanguinityViewServiceImpl implements ConsanguinityViewService {
    private final DorisJdbcTemplate dorisJdbcTemplate;
    private final JvsDataFactoryService jvsDataFactoryService;
    private final static String INSERT_SQL = "INSERT INTO consanguinity_view (`id`,`title`,`designId`,`subordinateTile`,`subordinateId`,`tenantId`,`groupId`,`viewType`,`type`) VALUES(?,?,?,?,?,?,?,?,?)";
    private final static String DELETE_SQL = "DELETE FROM consanguinity_view where id='{}'";
    private final static String QUERY_SQL = "select * from consanguinity_view where groupId='{}'";

    @Override
    public void save(List<ConsanguinityViewEntity> entity) {
        entity.forEach(e -> dorisJdbcTemplate.update(INSERT_SQL, e.getId(), e.getTitle(), e.getDesignId(), e.getSubordinateTile(), e.getSubordinateId(), e.getTenantId(), e.getGroupId(), e.getViewType().name(), e.getType()));
    }

    @Override
    public List<ConsanguinityViewEntity> check(String id, Integer type) {
        String sql = "select * from consanguinity_view where type =?";
        List<Map<String, Object>> list;
        if (type == 2) {
            sql += " and groupId=?";
            type = 3;
            //判断是否存在 使用方
        } else if (type == 1) {
            sql += " and designId = ?";
        }
        list = dorisJdbcTemplate.queryForList(sql, type, id);
        List<ConsanguinityViewEntity> consanguinityViewEntities = JSONArray.parseArray(JSONObject.toJSONString(list), ConsanguinityViewEntity.class);
        return consanguinityViewEntities;
    }

    @Override
    public void delete(String id) {
        dorisJdbcTemplate.execute(StrUtil.format(DELETE_SQL, id));
    }

    @Override
    public String syncConsanguinity(String id) {
        StringBuilder message = new StringBuilder();
        //清空租户信息 因为这是一个全局操作
        TenantContextHolder.clear();
        String querySql = "select * from consanguinity_view";
        if (StrUtil.isNotEmpty(id)) {
            querySql = querySql + " where groupId = '" + id + "'";
        }
        List<Map<String, Object>> list = dorisJdbcTemplate.queryForList(querySql);
        if (list.isEmpty()) {
            return message.toString();
        }
        List<ConsanguinityViewEntity> consanguinityViewEntities = JSONArray.parseArray(JSONObject.toJSONString(list), ConsanguinityViewEntity.class);
        //先判断 本体是否删除 如果本地已经被删除就直接 删除整个组的血缘关系
        List<String> groupIds = consanguinityViewEntities.stream().map(ConsanguinityViewEntity::getGroupId).distinct().collect(Collectors.toList());
        List<String> dataFactoryId = jvsDataFactoryService.list(new LambdaQueryWrapper<JvsDataFactory>().select(JvsDataFactory::getId).in(JvsDataFactory::getId, groupIds))
                .stream().map(JvsDataFactory::getId).collect(Collectors.toList());
        message.append("原数据为:").append(JSONObject.toJSONString(dataFactoryId)).append("删除的数据集id:");
        //通过对比两个数组获取需要删除的血缘视图
        consanguinityViewEntities.stream().filter(e -> !dataFactoryId.contains(e.getGroupId()))
                .forEach(e -> {
                    message.append(e.getId()).append(",");
                    this.delete(e.getId());
                });
        String tenantId = TenantContextHolder.getTenantId();
        if (StrUtil.isNotBlank(tenantId)) {
            message.append("租户id").append(tenantId);
        } else {
            message.append("租户id为空");
        }
        //获取主体没有删除的数据 根据不同的类型去查询不同服务的api 来确认是否被删除
        consanguinityViewEntities.stream().filter(e -> dataFactoryId.contains(e.getGroupId()))
                .forEach(e -> {
                    boolean isDelete = false;
                    switch (e.getViewType()) {
                        case chart:
                            JvsChartApi jvsChartApi = SpringContextUtil.getBean(JvsChartApi.class);
                            IsDeleteDto isDeleteDto = new IsDeleteDto();
                            isDeleteDto.setId(e.getDesignId());
                            isDeleteDto.setSubId(e.getSubordinateId());
                            isDeleteDto.setIsJobExec(Boolean.TRUE);
                            try {
                                jvsChartApi.isDelete(isDeleteDto);
                            } catch (Exception exception) {
                                log.info("错误", exception);
                                isDelete = "此图表已经被删除".equals(exception.getMessage());
                            }
                            break;
                        case report:
                            ReportLineageViewApi reportLineageViewApi = SpringContextUtil.getBean(ReportLineageViewApi.class);
                            try {
                                reportLineageViewApi.check(e.getDesignId(), "1");
                            } catch (Exception exception) {
                                log.info("错误", exception);
                                isDelete = "此报表已经被删除".equals(exception.getMessage());
                            }
                            break;
                        case screen:
                            ScreenLineageViewApi screenLineageViewApi = SpringContextUtil.getBean(ScreenLineageViewApi.class);
                            try {
                                log.info("入参为：{},{},{}", e.getDesignId(), e.getSubordinateId(), "1");
                                screenLineageViewApi.check(e.getDesignId(), e.getSubordinateId(), "1");
                            } catch (Exception exception) {
                                log.info("错误", exception);
                                isDelete = "此大屏已经被删除".equals(exception.getMessage());
                            }
                            break;
                        default:
                    }
                    if (isDelete) {
                        this.delete(e.getId());
                    }
                });
        return message.toString();
    }

    @Override
    public void deleteSource(String groupId, Integer type) {
        String sql = StrUtil.format(QUERY_SQL, groupId);
        if (type != null) {
            sql = sql + " and type = " + type;
        }
        List<Map<String, Object>> list = dorisJdbcTemplate.queryForList(sql);
        List<ConsanguinityViewEntity> consanguinityViewEntities = JSONArray.parseArray(JSONObject.toJSONString(list), ConsanguinityViewEntity.class);
        if (!consanguinityViewEntities.isEmpty()) {
            consanguinityViewEntities.forEach(e -> this.delete(e.getId()));
        }

    }

    @Override
    public Neo4jDto list(String groupId) {
        String sql = StrUtil.format(QUERY_SQL, groupId);
        //添加排序
        sql = sql + " order by `createTime`";
        List<Map<String, Object>> listMap = dorisJdbcTemplate.queryForList(sql);
        List<ConsanguinityViewEntity> list = JSONArray.parseArray(JSONObject.toJSONString(listMap), ConsanguinityViewEntity.class);
        //分类数据
        List<ConsanguinityViewTypeEnum> typeList = list.stream().filter(e -> e.getType().equals(3)).map(ConsanguinityViewEntity::getViewType).distinct().collect(Collectors.toList());
        List<Neo4jNodeDto> neo4jNodeDtoList = typeList.stream()
                .map(e -> new Neo4jNodeDto()
                        .setTitle(e.getDesc())
                        .setId(SecureUtil.md5(e.name()))
                        .setLevel(3)
                ).collect(Collectors.toList());
        //分类线条数据
        List<Neo4jEdgesDto> neo4jEdgesList = typeList
                .stream()
                .map(e -> new Neo4jEdgesDto()
                        .setTarget(SecureUtil.md5(e.name()))
                        .setSource(groupId)
                ).collect(Collectors.toList());
        //线条需要排除本身
        List<Neo4jEdgesDto> neo4jEdgesDtos = list.stream().filter(e -> !e.getType().equals(2)).map(e -> {
            Neo4jEdgesDto neo4jEdgesDto = new Neo4jEdgesDto();
            if (e.getType().equals(1)) {
                neo4jEdgesDto.setTarget(e.getGroupId())
                        .setSource(e.getId());
            }
            if (e.getType().equals(3)) {
                neo4jEdgesDto.setSource(SecureUtil.md5(e.getViewType().name()))
                        .setTarget(e.getId());
            }
            return neo4jEdgesDto;
        }).collect(Collectors.toList());
        neo4jEdgesList.addAll(neo4jEdgesDtos);
        //节点数据
        List<Neo4jNodeDto> nodeDtos = list.stream().map(e -> new Neo4jNodeDto()
                        .setId(e.getId())
                        .setLevel(e.getType() == 3 ? e.getType() + 1 : e.getType())
                        .setTitle(e.getTitle())
                        .setType(e.getViewType())
                        .setDesignId(e.getDesignId())
                        .setSubordinateId(e.getSubordinateId())
                        .setSubordinateTile(e.getSubordinateTile())
                        .setTenantId(e.getTenantId()))
                .collect(Collectors.toList());
        neo4jNodeDtoList.addAll(nodeDtos);
        return new Neo4jDto()
                .setEdges(neo4jEdgesList)
                .setNode(neo4jNodeDtoList);
    }
}
