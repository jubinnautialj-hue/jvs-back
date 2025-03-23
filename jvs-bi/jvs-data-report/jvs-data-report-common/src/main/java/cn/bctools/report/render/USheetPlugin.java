package cn.bctools.report.render;

import cn.bctools.report.model.univer.UCell;
import cn.bctools.report.model.univer.conf.USheetPluginDTO;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import java.util.List;
import java.util.Map;

public interface USheetPlugin {

    String getName();

    default void compute(List<USheetPluginDTO> resources, String sheetId, List<UCell> cells){
        String currentPluginName = getName();
        resources.forEach(e -> {
            if(currentPluginName.equals(e.getName())){
                String data = e.getData();
                if(StrUtil.isBlank(data)){
                    return;
                }
                Map<String,Object> sheetConf = JSONUtil.parseObj(data);
                Object o = sheetConf.get(sheetId);
                if(o!=null){
                    Object compute = calculation(o.toString(), cells);
                    sheetConf.put(sheetId,compute);
                }
                e.setData(JSONUtil.toJsonStr(sheetConf));
            }
        });

    }

    Object calculation(String config,List<UCell> cells);

}
