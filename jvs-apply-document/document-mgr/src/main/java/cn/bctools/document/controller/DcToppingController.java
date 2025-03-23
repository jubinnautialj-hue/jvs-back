package cn.bctools.document.controller;


import cn.bctools.common.utils.R;
import cn.bctools.document.entity.DcTopping;
import cn.bctools.document.service.DcToppingService;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "置顶数据")
@RestController
@AllArgsConstructor
@RequestMapping("/dc/topping")
public class DcToppingController {
    private final DcToppingService dcToppingService;

    @Log
    @ApiOperation("添加置顶数据")
    @PostMapping("/add")
    public R<DcTopping> add(@RequestBody DcTopping dcTopping) {
        dcTopping.setUserId(UserCurrentUtils.getUserId());
        dcToppingService.save(dcTopping);
        return R.ok(dcTopping);
    }

    @Log
    @ApiOperation("取消置顶")
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody DcTopping dcTopping) {
        boolean remove = dcToppingService.remove(new LambdaQueryWrapper<DcTopping>()
                .eq(DcTopping::getUserId, UserCurrentUtils.getUserId())
                .eq(DcTopping::getDcId, dcTopping.getDcId()));
        return R.ok(remove);
    }

}
