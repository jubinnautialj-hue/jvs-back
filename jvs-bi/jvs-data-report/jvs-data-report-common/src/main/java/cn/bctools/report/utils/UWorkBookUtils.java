package cn.bctools.report.utils;

import cn.bctools.database.util.IdGenerator;
import cn.bctools.report.model.univer.UWorkbook;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringPool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UWorkBookUtils {

    public static final String STYLE_PREFIX = "jvs_";

    /**
     * 替换工作簿 工作表中的id
     * @param workbook
     * @param newId
     * @return
     */
    public static UWorkbook replaceId(UWorkbook workbook, String newId){
        Map<String,String> sheetIdMapping = new HashMap<>();
        List<String> sheetOrder = workbook.getSheetOrder();

        workbook.setId(getNewId(workbook.getId(),newId));

        List<String> newSheetOrder = sheetOrder.stream().map(sheetId -> {
            String newSheetId = getNewId(sheetId, newId);
            sheetIdMapping.put(sheetId, newSheetId);
            return newSheetId;
        }).collect(Collectors.toList());
        workbook.setSheetOrder(newSheetOrder);

        String workbookStr = JSONUtil.toJsonStr(workbook);

        for (String oldSheetId : sheetOrder) {
            workbookStr = StrUtil.replace(workbookStr, oldSheetId, sheetIdMapping.get(oldSheetId));
        }

        return JSONUtil.toBean(workbookStr, UWorkbook.class);
    }

    private static String getNewId(String workBookId,String newId){
        List<String> split = StrUtil.split(workBookId, StringPool.DASH);
        split.set(0, newId);
        return String.join(StringPool.DASH, split);
    }

    public static String newStyleKey(){
        return STYLE_PREFIX + IdGenerator.getIdStr(32);
    }

    public static Boolean isCustomStyle(Object styleKey){
        if(styleKey==null){
            return false;
        }
        return styleKey.toString().startsWith(STYLE_PREFIX);
    }
}
