package cn.bctools.function.service;

import cn.bctools.function.entity.po.FunctionBusinessPo;
import com.baomidou.mybatisplus.extension.service.IService;


/**
 * @author zhuxiaokang
 */
public interface FunctionBusinessService extends IService<FunctionBusinessPo> {

    /**
     * 校验返回参数如果为空时给用户提示
     *
     * @param id     公式的 id值
     * @param result 要校验的参数值
     * @param msg    具体的消息，如果传递为空则返回默认的消息提示
     */
    void checkType(String id, Object result, String msg);

}
