package cn.bctools.auth.controller;

import cn.bctools.auth.entity.IndexApplet;
import cn.bctools.auth.service.IndexAppletService;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author guojing
 */
@Api(tags = "入口页信息展示")
@RestController
@RequestMapping("/index/applet")
@AllArgsConstructor
public class IndexAppletController {

    IndexAppletService indexAppletService;

    @ApiOperation(value = "获取入口页")
    @GetMapping
    public R<List> index() {
        IndexApplet one = indexAppletService.getOne(Wrappers.query(new IndexApplet().setUserId(UserCurrentUtils.getUserId())));
        if (ObjectNull.isNull(one)) {
            return R.ok();
            //如果为空,则直接将近8个菜单做为快速入口进行返回
        }
        return R.ok(one.getAppletJson());
    }

    @ApiOperation(value = "设置当前用户入口页")
    @PostMapping
    public R<List> index(@RequestBody List<Map<String, Object>> list) {
        IndexApplet one = indexAppletService.getOne(Wrappers.query(new IndexApplet().setUserId(UserCurrentUtils.getUserId())));
        if (ObjectNull.isNull(one)) {
            one = new IndexApplet().setUserId(UserCurrentUtils.getUserId()).setAppletJson(list);
            indexAppletService.save(one);
        } else {
            one.setAppletJson(list);
            indexAppletService.updateById(one);
        }
        return R.ok(one.getAppletJson());
    }

}
