//package cn.bctools.design.data.component;
//
//import cn.bctools.common.constant.SysConstant;
//import cn.bctools.common.entity.TenantSpace;
//import cn.bctools.common.utils.ObjectNull;
//import cn.bctools.common.utils.TenantContextHolder;
//import cn.bctools.design.data.entity.DataModelPo;
//import cn.bctools.design.data.service.DataModelService;
//import cn.bctools.redis.utils.RedisUtils;
//import cn.hutool.cron.CronUtil;
//import cn.hutool.cron.task.Task;
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.stereotype.Component;
//
//import java.util.Map;
//import java.util.stream.Collectors;
//
///**
// * 用于计算平台数据大小
// 暂时屏蔽此功能，不在计算数据大小,建议使用 bi进行统计后，直接读取数据
// */
//@Slf4j
//@Lazy(value = false)
//@Component
//public class DataComponentSize {
//
//    RedisUtils redisUtils;
//    DataModelService dataModelService;
//
//    public DataComponentSize(RedisUtils redisUtils, DataModelService dataModelService) {
//        this.redisUtils = redisUtils;
//        this.dataModelService = dataModelService;
////        extracted();
//        //动态的添加定时任务每5秒执行一次
//        CronUtil.schedule("* * */6 * * *", (Task) () -> {
//            try {
//                TenantContextHolder.clear();
////                extracted();
//            } catch (Exception e) {
//
//            }
//        });
//        //支持秒级
//        CronUtil.setMatchSecond(true);
//        //开启定时任务
////        CronUtil.start(true);
//    }
//
//    private void extracted() {
//        Map<String, Long> map = dataModelService.list(new LambdaQueryWrapper<DataModelPo>().select(DataModelPo::getSize, DataModelPo::getTenantId))
//                .stream()
//                .collect(Collectors.groupingBy(DataModelPo::getTenantId, Collectors.summingLong(DataModelPo::getSize)));
//        map.keySet().forEach(e -> {
//            //计算大小
//            TenantSpace o = (TenantSpace) redisUtils.get(SysConstant.SYSTEM_STORAGE_INFO + e);
//            if (ObjectNull.isNull(o)) {
//                o = new TenantSpace().setFileSumSize(Long.valueOf(0)).setDataSumSize(Long.valueOf(0));
//            }
//            o.setDataSumSize(map.get(e));
//            redisUtils.set(SysConstant.SYSTEM_STORAGE_INFO + e, o);
//        });
//    }
//
//}
