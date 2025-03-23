package cn.bctools.remote.controller;

import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import cn.bctools.remote.component.DataFactoryComponent;
import cn.bctools.remote.dto.MoveDto;
import cn.bctools.remote.dto.ServerParameterDto;
import cn.bctools.remote.enums.RemoteOperationEnum;
import cn.bctools.remote.log.service.RemoteLogService;
import cn.bctools.remote.po.JvsDataRemoteServer;
import cn.bctools.remote.service.JvsDataRemoteServerService;
import cn.bctools.remote.utils.RemoteAuthUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiaohui
 */
@RestController
@AllArgsConstructor
@Api(tags = "数据服务")
@RequestMapping("/remote")
public class RemoteController {

    private final JvsDataRemoteServerService jvsDataRemoteServerService;
    private final DataFactoryComponent dataFactoryComponent;
    private final RemoteLogService remoteLogService;
    private final RemoteAuthUtil<RemoteOperationEnum, JvsDataRemoteServer> remoteAuthUtil;

    @ApiOperation("新增服务")
    @PostMapping("/save")
    public R<JvsDataRemoteServer> save(@Validated @RequestBody JvsDataRemoteServer dto) {
        String msg = jvsDataRemoteServerService.checkSettings(dto);
        if (StrUtil.isNotBlank(msg)) {
            return R.failed(msg);
        }
        String example = dataFactoryComponent.generateOutExample(dto.getOutParameter());
        dto.setExampleResp(example);
        jvsDataRemoteServerService.save(dto);
        return R.ok(dto);
    }

    @ApiOperation("编辑服务信息")
    @PutMapping("/edit/{id}")
    public R<JvsDataRemoteServer> edit(@ApiParam("服务id") @PathVariable("id") String id, @Validated @RequestBody JvsDataRemoteServer dto) {
        JvsDataRemoteServer byId = jvsDataRemoteServerService.getById(id);
        if (ObjectUtil.isNull(byId)) {
            return R.failed("服务已被删除");
        }
        String msg = jvsDataRemoteServerService.checkSettings(dto);
        if (StrUtil.isNotBlank(msg)) {
            return R.failed(msg);
        }
        dto.setId(id);
        String example = dataFactoryComponent.generateOutExample(dto.getOutParameter());
        dto.setExampleResp(example);
        jvsDataRemoteServerService.updateById(dto);
        return R.ok(dto);
    }

    @ApiOperation("删除服务信息")
    @DeleteMapping("/del/{id}")
    public R<Boolean> del(@ApiParam("服务id") @PathVariable("id") String id) {
        boolean b = jvsDataRemoteServerService.removeById(id);
        remoteLogService.remove(id);
        return R.ok(b);
    }

    @Log
    @ApiOperation("数据服务详情")
    @GetMapping("/detail/{id}")
    public R<JvsDataRemoteServer> detail(@ApiParam("数据服务id") @PathVariable("id") String id) {
        JvsDataRemoteServer remoteServer = jvsDataRemoteServerService.getById(id);
        return R.ok(remoteAuthUtil.auth(remoteServer, null, Arrays.asList(RemoteOperationEnum.values())));
    }

    @Log
    @ApiOperation("获取数据服务参数信息")
    @GetMapping("/get/parameter/{id}")
    public R<Dict> parameter(@ApiParam("数据服务id") @PathVariable("id") String id) {
        JvsDataRemoteServer remoteServer = jvsDataRemoteServerService.getById(id);
        if (remoteServer == null) {
            return R.ok();
        }
        Dict dict = new Dict();
        String inParameter = remoteServer.getInParameter();
        String outParameter = remoteServer.getOutParameter();
        List<ServerParameterDto> in = JSONUtil.parseArray(inParameter).stream().map(JSONUtil::toJsonStr).map(e -> JSONUtil.toBean(e, ServerParameterDto.class)).collect(Collectors.toList());
        List<ServerParameterDto> out = JSONUtil.parseArray(outParameter).stream().map(JSONUtil::toJsonStr).map(e -> JSONUtil.toBean(e, ServerParameterDto.class)).collect(Collectors.toList());
        dict.put("in", in);
        dict.put("out", out);
        return R.ok(dict);
    }

    @Log
    @ApiOperation("移动不管是调整顺序还是移动到其它目录")
    @PutMapping("/move")
    @Transactional(rollbackFor = Exception.class)
    public R<List<JvsDataRemoteServer>> move(@RequestBody MoveDto MoveDto) {
        //获取数据
        JvsDataRemoteServer byId = jvsDataRemoteServerService.getById(MoveDto.getId());
        //获取当前目录下面的所有文档
        List<JvsDataRemoteServer> list = jvsDataRemoteServerService.list(new LambdaQueryWrapper<JvsDataRemoteServer>().eq(JvsDataRemoteServer::getType, MoveDto.getParentId())
                .ne(JvsDataRemoteServer::getId, MoveDto.getId())
                .orderByAsc(JvsDataRemoteServer::getSort));
        int indexOf = 0;
        if (StrUtil.isNotBlank(MoveDto.getFrontId())) {
            indexOf = list.stream().map(JvsDataRemoteServer::getId).collect(Collectors.toList()).indexOf(MoveDto.getFrontId()) + 1;
        }
        //插入数据
        byId.setType(MoveDto.getParentId());
        list.add(indexOf, byId);
        //重新排序
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setSort(i);
        }
        jvsDataRemoteServerService.updateBatchById(list);
        return R.ok(list);
    }
}
