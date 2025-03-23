package cn.bctools.data.factory.controller;


import cn.bctools.common.utils.R;
import cn.bctools.data.factory.entity.JvsDataAuth;
import cn.bctools.data.factory.service.JvsDataAuthService;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author xiaohui
 */
@Api(tags = "函数")
@RestController
@AllArgsConstructor
@RequestMapping("/jvs/data/auth")
@Slf4j
public class JvsDataAuthController {
    private final JvsDataAuthService jvsDataAuthService;


    @ApiOperation("分页数据")
    @GetMapping("/page")
    public R<Page<JvsDataAuth>> page(Page<JvsDataAuth> page, JvsDataAuth jvsDataAuth) {
        LambdaQueryWrapper<JvsDataAuth> queryWrapper = new LambdaQueryWrapper<JvsDataAuth>()
                .eq(ObjUtil.isNotNull(jvsDataAuth.getDataFactoryId()),JvsDataAuth::getDataFactoryId,jvsDataAuth.getDataFactoryId())
                .like(ObjUtil.isNotNull(jvsDataAuth.getAuthName()),JvsDataAuth::getAuthName,jvsDataAuth.getAuthName())
                .eq(ObjUtil.isNotNull(jvsDataAuth.getAuthType()), JvsDataAuth::getAuthType, jvsDataAuth.getAuthType());
        queryWrapper.orderByDesc(JvsDataAuth::getCreateTime);
        page = jvsDataAuthService.page(page, queryWrapper);
        return R.ok(page);
    }


    @ApiOperation("修改或新增")
    @PostMapping("/update")
    public R<JvsDataAuth> update(@RequestBody JvsDataAuth jvsDataAuth) {
        jvsDataAuthService.saveOrUpdate(jvsDataAuth);
        return R.ok(jvsDataAuth);
    }

    @ApiOperation("删除数据权限")
    @DeleteMapping("/delete/{id}")
    public R<Boolean> delete(@PathVariable String id) {
        boolean remove = jvsDataAuthService.removeById(id);
        return R.ok(remove);
    }

}
