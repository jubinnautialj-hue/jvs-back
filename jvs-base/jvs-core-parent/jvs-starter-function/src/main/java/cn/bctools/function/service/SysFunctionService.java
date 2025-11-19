package cn.bctools.function.service;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.function.entity.po.BaseFunctionPo;
import cn.bctools.function.entity.vo.FunctionBusinessTestVo;
import cn.bctools.function.entity.vo.Parameter;
import cn.bctools.function.enums.JvsParamType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * The interface Sys function service.
 *
 * @author jvs
 */
public interface SysFunctionService extends IService<BaseFunctionPo> {

    /**
     * 新增基础函数函数,
     * 同时检查函数是否可以正常执行
     *
     * @param functionName 函数名
     * @param shortName    搜索简短名称
     * @param info         解释
     * @param type         分组
     * @param body         函数体
     * @param jvsParamType 返回类型
     * @param parameters   根据顺序的参数
     * @param dynamicParam
     */
    void insertBaseFunction(String functionName, String shortName, String info, String type, String body, JvsParamType jvsParamType, List<Parameter> parameters, Boolean dynamicParam);

    /**
     * 用于测试要新增的函数是否正确
     *
     * @param functionName 函数名称 不传的话为 groovy
     * @param body         函数体 示例： def groovy(... x){   return x[0]+x[1]; };
     * @param parameters   所有参数
     * @param jvsParamType 返回类型，返回时会强制校验类型是否正确
     * @return object
     * @throws BusinessException the business exception
     */
    Object testBaseFunction(String functionName, String body, List<Parameter> parameters, JvsParamType jvsParamType) throws BusinessException;

}
