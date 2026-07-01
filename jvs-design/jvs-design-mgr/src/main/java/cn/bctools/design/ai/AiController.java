package cn.bctools.design.ai;

import cn.bctools.ai.api.JvsAiDatasetRoleApi;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.design.menu.util.JvsMenuVo;
import cn.bctools.design.use.UseComponent;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.web.utils.IpUtil;
import cn.hutool.core.lang.tree.Tree;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Ai  根据当前用户获取判断是否具有这个权限
 *
 * @Author: GuoZi
 */
@Slf4j
@Api(tags = "ai")
@RestController
@AllArgsConstructor
public class AiController implements JvsAiDatasetRoleApi {

    UseComponent useComponent;

    @Override
    public R<Map<String, List<String>>> permission(String authorization) {
        //确定用户是否是模拟用户
        ModeUtils.setMode();
        //根据当前用户获取判断有哪些模型的权限。
        Pair<List<Tree<Object>>, List<JvsMenuVo>> par = useComponent.menu("", ModeUtils.getRealUser().getId(), IpUtil.isMobile(), ModeUtils.getMode(), null);
        //将模型 id进行返回
        List<String> collect = par.getValue().stream().filter(e -> ObjectNull.isNotNull(e.getDataModelId())).map(JvsMenuVo::getDataModelId).distinct().collect(Collectors.toList());
        Map<String, List<String>> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put(SpringContextUtil.getApplicationContextName(),collect);
        return R.ok(objectObjectHashMap);
    }

}
