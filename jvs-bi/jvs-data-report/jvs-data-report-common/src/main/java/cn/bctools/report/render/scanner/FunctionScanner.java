package cn.bctools.report.render.scanner;

import cn.bctools.report.enums.EValueType;
import cn.bctools.report.model.univer.UCell;
import cn.bctools.report.render.ERenderingType;
import cn.bctools.report.render.RenderingGroup;
import cn.bctools.report.render.ScannerOrder;
import cn.bctools.report.utils.USheetUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 公式扫描器
 * @author wl
 */
@Component
@Order(ScannerOrder.FUNCTION)
public class FunctionScanner implements BaseScanner{

    @Override
    public List<RenderingGroup> scanner(List<UCell> oneDimensional, Map<Integer, Map<Integer, UCell>> cellData) {
        return oneDimensional.stream()
                .filter(e -> USheetUtils.equalsValueType(e, EValueType.公式))
                .peek(UCell::flag)
                .map(e -> new RenderingGroup()
                        .setBaseC(e.getC())
                        .setBaseR(e.getR())
                        .setBaseValueType(e.getCustom().getValueType())
                        .setRenderingType(ERenderingType.FUNCTION)
                        .setCells(Collections.singletonList(e))
                ).collect(Collectors.toList());
    }
}
