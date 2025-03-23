package cn.bctools.document.controller;

import cn.bctools.common.utils.R;
import cn.bctools.document.entity.DcShareEntity;
import cn.bctools.document.entity.enums.DcLibraryLogOperationTypeEnum;
import cn.bctools.document.log.DocumentLog;
import cn.bctools.document.service.DcShareService;
import cn.bctools.log.annotation.Log;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * @Author: ZhuXiaoKang
 * @Description: 知识库分享
 */

@Slf4j
@Api(tags = "分享")
@RestController
@RequestMapping(value = "/dcLibrary/share")
@AllArgsConstructor
public class ShareController {

    private final DcShareService dcShareService;

    @Log
    @ApiOperation(value = "分享设置")
    @PostMapping("/setting")
    @DocumentLog(operationType = DcLibraryLogOperationTypeEnum.SHARE)
    public R<DcShareEntity> shareSetting(@RequestBody DcShareEntity dcShareEntity) {
        dcShareService.settingLink(dcShareEntity);
        return R.ok(dcShareEntity);
    }


    @Log
    @ApiOperation(value = "通过文档id查看某个文档的分享信息")
    @GetMapping("/get/{id}")
    public R<DcShareEntity> shareSetting(@ApiParam("文档id") @PathVariable String id) {
        //判断是否存在此数据
        DcShareEntity dcShareEntity = dcShareService.getOne(new LambdaQueryWrapper<DcShareEntity>().eq(DcShareEntity::getDcId, id));
        dcShareEntity = Optional.ofNullable(dcShareEntity).orElseGet(() -> new DcShareEntity().setShare(Boolean.FALSE));
        return R.ok(dcShareEntity);
    }


}
