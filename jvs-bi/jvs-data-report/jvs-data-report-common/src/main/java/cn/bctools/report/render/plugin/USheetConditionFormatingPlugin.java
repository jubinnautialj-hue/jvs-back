package cn.bctools.report.render.plugin;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.report.model.univer.UCell;
import cn.bctools.report.model.univer.conf.URange;
import cn.bctools.report.render.USheetPlugin;
import cn.bctools.report.utils.ListUtils;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class USheetConditionFormatingPlugin implements USheetPlugin {

    @Override
    public String getName() {
        return "SHEET_CONDITIONAL_FORMATTING_PLUGIN";
    }

    @Override
    public Object calculation(String config, List<UCell> cells) {
        List<Conf> list = JSONUtil.toList(config, Conf.class);
        List<Conf> newList = new ArrayList<>();
        list.forEach(e -> {
            List<URange> ranges = e.getRanges();
            List<URange> newRanges = ranges.parallelStream().map(range -> {
                List<UCell> collect = cells.parallelStream()
                        .filter(v -> ListUtils.isIn(CollectionUtil.toList(range.getStartRow(), range.getEndRow()), v.getCustom().getOR())
                                &&
                                ListUtils.isIn(CollectionUtil.toList(range.getStartColumn(), range.getEndColumn()), v.getCustom().getOC())
                        ).collect(Collectors.toList());
                if (CollectionUtil.isNotEmpty(collect)) {
                    Map<Integer, List<Integer>> collect1 = collect.parallelStream().collect(Collectors.groupingBy(UCell::getC, Collectors.mapping(UCell::getR, Collectors.toList())));

                    return collect1.entrySet().stream().map(v -> {
                        Integer startRow = v.getValue().stream().min(Integer::compare).get();
                        Integer endRow = v.getValue().stream().max(Integer::compare).get();

                        URange copy = BeanCopyUtil.copy(range, URange.class);
                        copy.setStartRow(startRow).setEndRow(endRow).setStartColumn(v.getKey()).setEndColumn(v.getKey());
                        return copy;
                    }).collect(Collectors.toList());
                }
                return null;
            }).filter(Objects::nonNull).flatMap(Collection::stream).collect(Collectors.toList());
//            e.setRanges(newRanges);
            for (URange newRange : newRanges) {
                Conf copy = BeanCopyUtil.copy(e, Conf.class);
                copy.setCfId(IdGenerator.getIdStr(32));
                copy.setRanges(Collections.singletonList(newRange));
                newList.add(copy);
            }
        });
        return newList;
    }

    @Data
    @Accessors(chain = true)
    public static class Conf{
        private String cfId;

        private List<URange> ranges;

        private Object rule;

        private boolean stopIfTrue;
    }
}
