package cn.bctools.design.data.fields.impl.advance;

import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.form.html.BluetoothBeaconHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;


/**
 * 用于移动端小程序获取蓝牙信标的数据
 *
 * @author zhuxiaokang
 */
@Slf4j
@Component
@DesignField(value = "蓝牙信标", type = DataFieldType.bluetoothBeacon)
public class BluetoothBeaconFieldHandler implements IDataFieldHandler<BluetoothBeaconHtml> {

    @Override
    public Object checkDataFieldType(BluetoothBeaconHtml bluetoothBeaconHtml, Object o) throws Exception {
        //校验是否是数组
        if (!(o instanceof List)) {
            throw new DataFormatException("类型不正确");
        }
        return o;
    }

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"bluetoothBeacon\",\n" +
                "    \"label\": \"" + name + "\",\n" +
                "    \"span\": 24,\n" +
                "    \"display\": true,\n" +
                "    \"status\": \"\",\n" +
                "    \"tips\": {\n" +
                "        \"text\": \"\",\n" +
                "        \"position\": \"right\"\n" +
                "    },\n" +
                "    \"multiple\": true,\n" +
                "    \"allowinput\": false,\n" +
                "    \"showFrom\": [\n" +
                "        \"label\",\n" +
                "        \"span\",\n" +
                "        \"prop\",\n" +
                "        \"sqlType\",\n" +
                "        \"disabled\"\n" +
                "    ],\n" +
                "    \"rules\": [\n" +
                "\n" +
                "    ],\n" +
                "    \"sqlType\": \"array\",\n" +
                "    \"name\": \"" + DataFieldType.bluetoothBeacon.getDesc() + "\",\n" +
                "    \"disabled\": false\n" +
                "}";
        return JSONObject.parseObject(str);
    }
}
