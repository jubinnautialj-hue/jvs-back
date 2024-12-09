package cn.bctools.rule.tools.vin;


import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author jvs
 */
@Service
@AllArgsConstructor
@Rule(value = "VIN号生成",
        group = RuleGroup.工具插件,
        explain = "vin编号指的是汽车的车辆识别代码，车辆识别代码主要包含了车辆的生产厂家、年代、车型、车身型式等信息",
        test = true,
        order = 33,
        returnType = ClassType.文本,
        testShowEnum = TestShowEnum.TEXT
)
public class VinServiceImpl implements BaseCustomFunctionInterface<VinDto> {
    public static void main(String[] args) {
        Object execute = new VinServiceImpl().execute(new VinDto().setYearLetter("G").setFactoryCode("R").setSerialNumber("20245").setVinTopEight("wwertyuI").setFactoryCode("Y"), null);
        System.out.println(execute);
    }

    @Override
    public Object execute(VinDto vinDto, Map<String, Object> params) {
        return generateAndReplaceVinChecksum(vinDto.getVinTopEight() + vinDto.getYearLetter() + vinDto.getFactoryCode() + vinDto.getAssemblyLineCode() + vinDto.getSerialNumber()).toUpperCase();
    }

    public static String generateAndReplaceVinChecksum(String vin) {
        StringBuilder sb = new StringBuilder(vin);
        sb.insert(8, '1');
        char checksum = generateVinChecksum(sb.toString().toUpperCase());
        sb.setCharAt(8, checksum);
        return sb.toString();
    }

    public static char generateVinChecksum(String vin) {
        int[] weights = {8, 7, 6, 5, 4, 3, 2, 10, 0, 9, 8, 7, 6, 5, 4, 3, 2};
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            char c = vin.charAt(i);
            int value = mapCharacterToValue(c);
            sum += value * weights[i];
        }
        int remainder = sum % 11;
        if (remainder == 10) {
            // 如果余数为10，校验码为字符'X'
            return 'X';
        } else {
            return (char) ('0' + remainder);
        }
    }

    private static int mapCharacterToValue(char c) {
        switch (c) {
            case '0':
                return 0;
            case '1':
                return 1;
            case '2':
                return 2;
            case '3':
                return 3;
            case '4':
                return 4;
            case '5':
                return 5;
            case '6':
                return 6;
            case '7':
                return 7;
            case '8':
                return 8;
            case '9':
                return 9;
            case 'A':
            case 'J':
                return 1;
            case 'B':
            case 'K':
                return 2;
            case 'C':
            case 'L':
                return 3;
            case 'D':
            case 'M':
                return 4;
            case 'E':
            case 'N':
                return 5;
            case 'F':
            case 'P':
                return 6;
            case 'G':
                return 7;
            case 'H':
                return 8;
            case 'R':
                return 9;
            case 'S':
                return 2;
            case 'T':
                return 3;
            case 'U':
                return 4;
            case 'V':
                return 5;
            case 'W':
                return 6;
            case 'X':
                return 7;
            case 'Y':
                return 8;
            case 'Z':
                return 9;
            default:
                throw new IllegalArgumentException("vin Cannot include IOQ ");
        }
    }

}
