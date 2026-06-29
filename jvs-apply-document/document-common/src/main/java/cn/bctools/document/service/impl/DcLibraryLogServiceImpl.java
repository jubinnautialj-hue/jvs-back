package cn.bctools.document.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.document.auth.dto.LibraryAuthDto;
import cn.bctools.document.auth.util.AuthSystemTool;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.DcLibraryLog;
import cn.bctools.document.entity.enums.DcLibraryLogOperationTypeEnum;
import cn.bctools.document.mapper.DcLibraryLogMapper;
import cn.bctools.document.service.DcLibraryLogService;
import cn.bctools.document.service.DcLibraryService;
import cn.bctools.oauth2.utils.AuthorityManagementUtils;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.hutool.core.lang.Dict;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @author Auto Generator
 */
@AllArgsConstructor
@Service
@Slf4j
public class DcLibraryLogServiceImpl extends ServiceImpl<DcLibraryLogMapper, DcLibraryLog> implements DcLibraryLogService {
    private final DcLibraryService dcLibraryService;
    private final DcLibraryLogMapper dcLibraryLogMapper;

    @Override
    public List<DcLibrary> frequentlyKnowledge() {
        // 查询自己拥有的知识库id
        LibraryAuthDto libraryAuth = AuthSystemTool.getLibraryAuth();
        List<String> allIds = libraryAuth.getAllIds();
        //通过日志查询
        List<String> list = dcLibraryLogMapper.frequentlyKnowledge(UserCurrentUtils.getUserId(), allIds);
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        log.info("获取的文库id为:{}", JSONObject.toJSONString(list));
        //查询文库
        Map<String, DcLibrary> map = dcLibraryService.listByIds(list).stream().collect(Collectors.toMap(DcLibrary::getId, Function.identity()));
        //排序
        LinkedList<DcLibrary> linkedList = new LinkedList<>();
        list.forEach(e -> linkedList.add(map.get(e)));
        log.info("返回的常用文库为:{}", JSONObject.toJSONString(linkedList));
        return linkedList;
    }

    @Override
    public List<DcLibraryLog> frequently(DcLibraryLog dcLibraryLog) {
        List<DcLibraryLog> list = dcLibraryLogMapper.frequently(UserCurrentUtils.getUserId(), dcLibraryLog.getOperationType(), dcLibraryLog.getType(), dcLibraryLog.getKnowledgeId());
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        //check一下文件是否存在
        Map<String, DcLibrary> map = dcLibraryService.list(new QueryWrapper<DcLibrary>().lambda()
                        .in(DcLibrary::getId, list.stream().map(DcLibraryLog::getDcLibraryId).collect(Collectors.toList())))
                .stream().collect(Collectors.toMap(DcLibrary::getId, Function.identity()));
        list = list.stream()
                .filter(e -> map.containsKey(e.getDcLibraryId()))
                .peek(e->e.setDcLibrary(map.get(e.getDcLibraryId())))
                .collect(Collectors.toList());
        return list;
    }

    @Override
    public Dict groupByCount(String id) {
        QueryWrapper<DcLibraryLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("operation_type as operationType,count(type) as count");
        Dict set = Dict.create();
        if (!"-1".equals(id)) {
            queryWrapper.eq("knowledge_id", id).or().eq("dc_library_id", id);
            DcLibrary dcLibrary = dcLibraryService.getById(id);
            //获取创建人信息
            UserDto userDto = AuthorityManagementUtils.getUserById(dcLibrary.getCreateById());
            set.set("userDto", userDto);
        }
        queryWrapper.and(e -> e.eq("operation_type", DcLibraryLogOperationTypeEnum.DOWNLOAD).or().eq("operation_type", DcLibraryLogOperationTypeEnum.SEE))
                .groupBy("operation_type");
        List<Map<String, Object>> list = this.listMaps(queryWrapper);
        list.forEach(e -> {
            String operationType = (String) e.get("operationType");
            String name = Objects.requireNonNull(DcLibraryLogOperationTypeEnum.get(operationType)).name();
            set.put(name, e.get("count"));
        });
        return set;
    }
}
