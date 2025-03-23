package cn.bctools.remote.service.impl;

import cn.bctools.remote.mapper.JvsDataRemoteServiceMapper;
import cn.bctools.remote.po.InParameterJsonPo;
import cn.bctools.remote.service.JvsDataRemoteServerService;
import cn.bctools.remote.po.JvsDataRemoteServer;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * api服务 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-20
 */
@Service
@AllArgsConstructor
public class JvsDataRemoteServiceServiceImpl extends ServiceImpl<JvsDataRemoteServiceMapper, JvsDataRemoteServer> implements JvsDataRemoteServerService {

    @Override
    public String checkSettings(JvsDataRemoteServer server) {
        String msg;
        List<InParameterJsonPo> inParameterDtoList = new ArrayList<>();
        String inParameter = server.getInParameter();

        if(StrUtil.isNotBlank(inParameter)){
            inParameterDtoList.addAll(JSONUtil.toList(inParameter, InParameterJsonPo.class));
        }
        String sourceInParameter = server.getSourceInParameter();
        if(StrUtil.isNotBlank(sourceInParameter)){
            inParameterDtoList.addAll(JSONUtil.toList(sourceInParameter,InParameterJsonPo.class));
        }
        msg = this.checkDouble("接口入参",inParameterDtoList);
        if(StrUtil.isBlank(msg)){
            String outParameter = server.getOutParameter();
            List<InParameterJsonPo> outParameterList = JSONUtil.toList(outParameter, InParameterJsonPo.class);
            msg = this.checkDouble("接口出参",outParameterList);
        }
        if (StrUtil.isBlank(msg)){
            String outParameter = server.getOutParameter();
            List<InParameterJsonPo> outParameterList = JSONUtil.toList(outParameter, InParameterJsonPo.class);
            msg = this.checkAsKeyDouble(outParameterList);
        }
        return msg;
    }

    /**
     * 检查是否重复 true重复 false不重复
     * @param parameters
     * @return
     */
    private String checkDouble(String prefix,List<InParameterJsonPo> parameters){
        Map<String, List<InParameterJsonPo>> collect = parameters.stream().collect(Collectors.groupingBy(InParameterJsonPo::getKey));
        for (String s : collect.keySet()) {
            List<InParameterJsonPo> inParameterJsonPos = collect.get(s);
            int size = inParameterJsonPos.size();
            if(size>1){
                InParameterJsonPo first = CollectionUtil.getFirst(inParameterJsonPos);
                return StrUtil.format("{}:字段: {}<{}> 重复",prefix,first.getName(),first.getKey());
            }
        }
        return null;
    }

    /**
     * 检查别名是否存在重复
     * @param parameters
     * @return
     */
    private String checkAsKeyDouble(List<InParameterJsonPo> parameters){
        Map<String, List<InParameterJsonPo>> collect = parameters.stream().filter(e->StrUtil.isNotBlank(e.getAsKey())).collect(Collectors.groupingBy(InParameterJsonPo::getAsKey));
        for (String s : collect.keySet()) {
            List<InParameterJsonPo> inParameterJsonPos = collect.get(s);
            int size = inParameterJsonPos.size();
            if(size>1){
                InParameterJsonPo first = CollectionUtil.getFirst(inParameterJsonPos);
                return StrUtil.format("接口出参:字段: {}<{}> 别名重复",first.getName(),first.getKey());
            }
        }
        return null;
    }
}
