package cn.bctools.function.controller;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.function.component.ExpressionComponent;
import cn.bctools.function.entity.dto.ExecDto;
import cn.bctools.function.entity.dto.TableType;
import cn.bctools.function.entity.po.BaseFunctionPo;
import cn.bctools.function.entity.po.FunctionBusinessPo;
import cn.bctools.function.entity.vo.ElementVo;
import cn.bctools.function.entity.vo.FunctionBusinessTestVo;
import cn.bctools.function.enums.JvsParamType;
import cn.bctools.function.enums.SysParamEnums;
import cn.bctools.function.handler.*;
import cn.bctools.function.handler.impl.SysParamImpl;
import cn.bctools.function.service.FunctionBusinessService;
import cn.bctools.function.service.SysFunctionService;
import cn.bctools.function.utils.ExpressionParam;
import cn.bctools.function.utils.ExpressionUtils;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.redis.JvsMessageListenerAdapter;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import zipkin2.Call;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Jvs表达式
 */
@Slf4j
@RequestMapping("/jvsFunction")
@RestController
@AllArgsConstructor
@Api(tags = "[expression]Jvs表达式")
public class ExpressionController {

    ExpressionHandler handler;
    ExpressionComponent expressionComponent;
    FunctionBusinessService functionBusinessService;
    ExpressionAfterHandler expressionAfterHandler;
    SysFunctionService functionService;
    RedisUtils redisUtils;
    ExpressionBeforeHandler expressionBeforeHandler;

    @Log
    @GetMapping("/functions")
    @ApiOperation("根据变量中心类别名称获取全部函数")
    public R<Map<String, Object>> getAllFunctions(@ApiParam("使用场景") @RequestParam(name = "useCase", required = false) String useCase,
                                                  @ApiParam("节点ID") @RequestParam(name = "nodeId", required = false) String nodeId,
                                                  @ApiParam("使用场景设计ID") @RequestParam(name = "id", required = false) String id,
                                                  @ApiParam("扩展信息 用于不同业务可以传入自定义数据") @RequestParam(name = "extendJson", required = false) String extendJson) {
        // 记录设计id
        SystemThreadLocal.set(IJvsFunction.KEY_DESIGN_ID, id);
        SystemThreadLocal.set(IJvsFunction.KEY_DESIGN_NODE_ID, nodeId);
        SystemThreadLocal.set(IJvsFunction.KEY_DESIGN_EXTEND_JSON, extendJson);
        IJvsExpressionElement.local.set(new HashMap<>());
        return R.ok(handler.getAllExpressionElement(useCase));
    }

    @Log
    @GetMapping("/params")
    @ApiOperation("根据场景获取字段")
    public R<Map> getAllParams(@ApiParam("使用场景") @RequestParam(name = "useCase", required = false) String useCase,
                               @ApiParam("使用场景") @RequestParam(name = "excludeUseCase", required = false) String excludeUseCase,
                               @ApiParam("节点ID") @RequestParam(name = "nodeId", required = false) String nodeId,
                               @ApiParam("使用场景设计ID") @RequestParam(name = "id", required = false) String id) {
        // 记录设计id
        SystemThreadLocal.set(IJvsFunction.KEY_DESIGN_NODE_ID, nodeId);
        SystemThreadLocal.set(IJvsFunction.KEY_DESIGN_ID, id);
        Map<String, Map<String, List<ElementVo>>> allParamElement = handler.getAllParamElement(useCase, excludeUseCase);
        return R.ok(allParamElement);
    }

    @Log
    @DeleteMapping("/exec/{designId}/{id}")
    @ApiOperation("删除公式")
    @Transactional(rollbackFor = Exception.class)
    public R<FunctionBusinessPo> delete(@PathVariable String designId, @PathVariable String id) {
        functionBusinessService.remove(Wrappers.query(new FunctionBusinessPo().setDesignId(designId).setId(id)));
        return R.ok();
    }

    @Log
    @PutMapping("/exec/delete/{designId}")
    @ApiOperation("批量删除公式")
    @Transactional(rollbackFor = Exception.class)
    public R<FunctionBusinessPo> deleteList(@PathVariable String designId, @RequestBody List<String> ids) {
        functionBusinessService.remove(new LambdaQueryWrapper<FunctionBusinessPo>()
                .eq(FunctionBusinessPo::getDesignId, designId)
                .in(FunctionBusinessPo::getId, ids)
        );
        return R.ok();
    }

    @Log
    @GetMapping("/exec/{id}")
    @ApiOperation("获取函数执行具体信息")
    @Transactional(rollbackFor = Exception.class)
    public R<FunctionBusinessPo> exec(@PathVariable String id) {
        FunctionBusinessPo functionBusinessPo = functionBusinessService.getById(id);
        return R.ok(functionBusinessPo);
    }

    /**
     * 新增或修改表达式
     *
     * @param execPo 表达式数据
     * @return 保存后的数据(含id)
     */
    @Log
    @PostMapping("/save/exec")
    @ApiOperation("新增表达式")
    @Transactional(rollbackFor = Exception.class)
    public R<FunctionBusinessPo> exec(@ApiParam("函数执行类") @RequestBody FunctionBusinessPo execPo) {
        expressionComponent.handleFunctionPo(execPo);
        boolean b = functionBusinessService.updateById(execPo);
        if (!b) {
            functionBusinessService.save(execPo);
        }
        // 校验循环依赖
        return R.ok(execPo);
    }

    /**
     * 新增或修改表达式
     *
     * @param execPo 表达式数据
     * @return 保存后的数据(含id)
     */
    @Log
    @PutMapping("/save/exec")
    @ApiOperation("修改表达式")
    @Transactional(rollbackFor = Exception.class)
    public R<FunctionBusinessPo> updateExec(@ApiParam("函数执行类") @RequestBody FunctionBusinessPo execPo) {
        Optional.ofNullable(execPo.getId()).orElseThrow(() -> new BusinessException("id不能为空"));
        expressionComponent.handleFunctionPo(execPo);
        boolean b = functionBusinessService.updateById(execPo);
        if (!b) {
            FunctionBusinessPo byId = functionBusinessService.getById(execPo.getId());
            if (ObjectNull.isNull(byId)) {
                functionBusinessService.save(execPo);
            }
        }
        //清除清楚其它缓存
        // 校验循环依赖
        return R.ok(execPo);
    }

    @Log
    @PutMapping("/test/exec")
    @ApiOperation("测试业务表达式")
    public R testExec(@ApiParam("函数执行类") @RequestBody FunctionBusinessTestVo execPo) {
        //判断参数是否匹配
        List<String> data = parsePostfixExpression(execPo).getData();
        String collect = data.stream().filter(e -> !execPo.getMap().containsKey(e)).collect(Collectors.joining(","));
        if (ObjectNull.isNotNull(collect)) {
            return R.failed("未填写" + collect + "参数");
        }
        execPo.getMapParamType().keySet().forEach(e -> {
            Object o = execPo.getMap().get(e);
            switch (execPo.getMapParamType().get(e)) {
                case number:
                    if (o.toString().contains(".")) {
                        o = Double.valueOf(o.toString());
                    } else {
                        o = Integer.valueOf(o.toString());
                    }
                    break;
                case date:
                    o = DateUtil.parse(o.toString());
                    break;
                case object:
                    o = JSONObject.parseObject(o.toString());
                    break;
                case bool:
                    o = Boolean.valueOf(o.toString());
                    break;
                case array:
                    o = JSONArray.parseArray(o.toString());
                    break;
                default:
                    break;
            }
            execPo.getMap().put(e, o);

        });
        //测试时直接处理
        Object calculate = handler.calculate(execPo.getBody(), execPo.getMap(), "");
        // 校验循环依赖
        return R.ok(calculate);
    }

    /**
     * 解析函数体结构，返回这个函数体中包含的字段，用于测试函数是否可正常执行
     */
    @PutMapping("/test/parse")
    @ApiOperation("测试业务表达式")
    public R<List<String>> parsePostfixExpression(@ApiParam("函数执行类") @RequestBody FunctionBusinessTestVo execPo) {
        List<String> collect = ExpressionUtils.parsePostfixExpression(execPo.getBody())
                .stream()
                .filter(e -> JvsParamType.any.equals(e.getValueType()))
                .map(ExpressionParam::getValue)
                .map(String::valueOf)
                .filter(e -> {
                    if (e.startsWith("SYS" + SysParamImpl.USER_EXTENSION)) {
                        return false;
                    }
                    if (Arrays.stream(SysParamEnums.values()).anyMatch(a -> ("SYS" + a.getId()).equals(e))) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
        return R.ok(collect);
    }


    /**
     * todo 此方法需要做缓存处理
     *
     * @param designId
     * @param useCase
     * @param init
     * @param body
     * @return
     */
    @Log
    @PostMapping("/exec/{designId}/{useCase}")
    @ApiOperation("根据设计id获取函数执行结果")
    public R<Map<String, Object>> getExec(@ApiParam("设计ID") @PathVariable("designId") String designId,
                                          @ApiParam("表达式使用场景") @PathVariable("useCase") String useCase,
                                          @ApiParam("是否对公式进行扩展化处理") @RequestHeader(value = "init", required = false, defaultValue = "false") Boolean init,
                                          @RequestHeader(value = "tableType", required = false) TableType tableType,
                                          @RequestBody(required = false) ExecDto body) {
        //跳过模拟用户操作,让公式可以自己进行一次更新。
        SystemThreadLocal.set("designSkip", init);
        ExecDto copy = BeanCopyUtil.copy(body, ExecDto.class);
        // 记录设计id
        SystemThreadLocal.set(IJvsFunction.KEY_DESIGN_ID, designId);
        Integer index = body.getIndex();
        SystemThreadLocal.set("index", index);
        SystemThreadLocal.set("tableType", tableType);
        if (ObjectNull.isNotNull(index)) {
            //表格的操作类型， 如果是表可的操作， 有行级操作，或新增，或删除
            SystemThreadLocal.set("tableType", TableType.line);
        }
        IJvsExpressionElement.local.set(new HashMap<>());
        body = expressionBeforeHandler.handler(designId, useCase, init, body);
        expressionComponent.getExpression(designId, useCase, body);
        copy.setParams(body.getParams());
        //低代码中如果默认为第一次初始化时，需要进行数据转换优化展示速度
        Map<String, Object> params = expressionAfterHandler.handler(designId, init, copy);
        return R.ok(params);
    }


    @GetMapping("/base/list")
    @ApiOperation("函数分页信息")
    public R<Page<BaseFunctionPo>> page(Page<BaseFunctionPo> page, BaseFunctionPo po) {
        page = functionService.page(page, new LambdaQueryWrapper<BaseFunctionPo>()
                .select(BaseFunctionPo::getName, BaseFunctionPo::getShortName, BaseFunctionPo::getId, BaseFunctionPo::getInfo, BaseFunctionPo::getType)
                .like(StrUtil.isNotBlank(po.getName()), BaseFunctionPo::getName, po.getName())
                .like(StrUtil.isNotBlank(po.getShortName()), BaseFunctionPo::getShortName, po.getShortName())
                .eq(ObjectNull.isNotNull(po.getType()), BaseFunctionPo::getType, po.getType()));
        return R.ok(page);
    }

    @GetMapping("/base/type")
    @ApiOperation("函数分页信息")
    public R<Set<String>> type() {
        Set<String> stringSet = functionService.list(new LambdaQueryWrapper<BaseFunctionPo>().select(BaseFunctionPo::getType))
                .stream().map(BaseFunctionPo::getType).collect(Collectors.toSet());
        return R.ok(stringSet);
    }

    @GetMapping("/base/{name}")
    @ApiOperation("根据获取某一个函数")
    public R by(@PathVariable("name") String name) {
        List<BaseFunctionPo> list = functionService.list();
        BaseFunctionPo baseFunctionPo = list.stream().filter(e -> name.equals(e.getName())).findAny().orElseThrow(() -> new BusinessException("没有这个函数"));
        FunctionBusinessTestVo copy = BeanCopyUtil.copy(baseFunctionPo, FunctionBusinessTestVo.class);
        return R.ok(copy);
    }


    /**
     * 用于添加函数前的测试操作
     */
    @PostMapping("/base/test")
    @ApiOperation("函数测试")
    public R baseTest(@RequestBody FunctionBusinessTestVo businessTestVo) {
        Object o = functionService.testBaseFunction(businessTestVo.getFunctionName(), businessTestVo.getBody(), businessTestVo.getParameters(), businessTestVo.getJvsParamType());
        return R.ok(o);
    }

    /**
     * {
     * "body": "def groovy(... x){   return x[0]+x[1]; };",
     * "functionName": "test",
     * "info": "测试新增函数",
     * "jvsParamType": "text",
     * "type": "测试分组",
     * "parameters": [
     * "asfd","fadsfsa"
     * ]
     * }
     * 新增一个函数
     */
    @Log
    @PostMapping("/base/save")
    @ApiOperation("函数新增")
    public R<Map<String, Object>> baseSave(@RequestBody FunctionBusinessTestVo businessTestVo) {
        if(ObjectNull.isNull(businessTestVo.getParameters())){
            return R.failed("请填写参数");
        }
        functionService.insertBaseFunction(businessTestVo.getFunctionName(), businessTestVo.getShortName(), businessTestVo.getInfo(), businessTestVo.getType(), businessTestVo.getBody(), businessTestVo.getJvsParamType(),
                businessTestVo.getParameters(), businessTestVo.getDynamicParam());
        redisUtils.publish("functionCleanCache", null);
        return R.ok();
    }

    @Log
    @DeleteMapping("/base/delete/{id}")
    @ApiOperation("删除函数")
    public R delete(@PathVariable("id") String id) {
        BaseFunctionPo baseFunctionPo = functionService.getById(id);
        if (ObjectNull.isNotNull(baseFunctionPo)) {
            log.info("用户删除函数 用户:{},函数{} ", UserCurrentUtils.getAccountName(), JSONObject.toJSONString(baseFunctionPo));
            functionService.removeById(id);
            redisUtils.publish("functionCleanCache", null);
        }
        return R.ok();
    }

}
