package cn.bctools.data.factory.html.node;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.entity.JvsDataFactoryOut;
import cn.bctools.data.factory.html.FData;
import cn.bctools.data.factory.html.node.params.OutPutParams;
import cn.bctools.data.factory.html.run.Frun;
import cn.bctools.data.factory.service.JvsDataFactoryOutService;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 数据输出
 *
 * @author guojing
 */
@Service
@AllArgsConstructor
public class OutPutNode implements Frun<OutPutParams> {


    JvsDataFactoryOutService dataFactoryOutService;

    @Override
    public FData run(Boolean formal, Map<String, FData> linkBody, OutPutParams outPutParams) {
        String next = linkBody.keySet().iterator().next();
        FData fData = linkBody.get(next);
        String outNodeDocumentName = "ods_" + outPutParams.getDataId();
        if (!formal) {
            outNodeDocumentName = outPutParams.getTableName();
        }
        //获取数据进行过滤
        //同步数据到输出节点  注意输出节点的集合应该是规定的方便用于删除
        String documentName = fData.getDocumentName();
        StringBuffer sql = new StringBuffer();
        OutPutParams.OutPutObj outPutObj = outPutParams.getSourceData().getOutPutObj();
        Boolean isAdd = Optional.ofNullable(outPutObj.getIsAdd()).orElse(Boolean.FALSE);
        if (formal && !isAdd) {
            DorisJdbcTemplate dorisJdbcTemplate = SpringContextUtil.getBean(DorisJdbcTemplate.class);
            dorisJdbcTemplate.dropForce(outNodeDocumentName);
        }
        //执行逻辑
        //只保留需要展示的字段
        List<DataSourceField> title = fData.getTitle().parallelStream().filter(DataSourceField::getIsShow).collect(Collectors.toList());
        List<Object> objects = createSql(title, title, sql, documentName, outNodeDocumentName, Boolean.TRUE, Boolean.TRUE, outPutParams);
        this.save(sql.toString(), outNodeDocumentName, title, Boolean.TRUE, null, Boolean.FALSE, new ArrayList<>(),objects.toArray());
        //判断是否是正式执行
        if (formal) {
            List<JSONObject> list = JSONObject.parseArray(JSONObject.toJSONString(title), JSONObject.class);
            //直接入库
            JvsDataFactoryOut jvsDataFactoryOut = dataFactoryOutService.getOne(new LambdaQueryWrapper<JvsDataFactoryOut>().eq(JvsDataFactoryOut::getDataId, outPutParams.getDataId()));
            boolean aNull = ObjectUtil.isNull(jvsDataFactoryOut);
            if (aNull) {
                jvsDataFactoryOut = new JvsDataFactoryOut()
                        .setDataId(outPutParams.getDataId())
                        .setName(outPutParams.getName())
                        .setDocumentName(outNodeDocumentName)
                        .setFields(list);
                dataFactoryOutService.save(jvsDataFactoryOut);
            } else {
                jvsDataFactoryOut
                        .setDataId(outPutParams.getDataId())
                        .setName(outPutParams.getName())
                        .setFields(list);
                dataFactoryOutService.updateById(jvsDataFactoryOut);
            }
        }
        return fData.setDocumentName(outNodeDocumentName);
    }


}
