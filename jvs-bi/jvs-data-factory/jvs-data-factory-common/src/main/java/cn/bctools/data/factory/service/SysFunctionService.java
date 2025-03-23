package cn.bctools.data.factory.service;

import cn.bctools.data.factory.entity.SysFunction;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 函数
 *
 * @author zqs
 */
public interface SysFunctionService extends IService<SysFunction> {
    /**
     *  初始化新增函数的md5
     */
    List<SysFunction> createMd5();

}
