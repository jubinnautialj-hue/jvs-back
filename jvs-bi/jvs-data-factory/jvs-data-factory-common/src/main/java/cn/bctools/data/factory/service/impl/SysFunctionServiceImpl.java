package cn.bctools.data.factory.service.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.data.factory.entity.SysFunction;
import cn.bctools.data.factory.mapper.SysFunctionMapper;
import cn.bctools.data.factory.service.SysFunctionService;
import cn.bctools.data.factory.util.DorisJarUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 函数
 *
 * @author zqs
 */
@Slf4j
@Service
@AllArgsConstructor
public class SysFunctionServiceImpl extends ServiceImpl<SysFunctionMapper, SysFunction> implements SysFunctionService {


    @Override
    public List<SysFunction> createMd5() {
        List<SysFunction> list = this.list(new LambdaQueryWrapper<SysFunction>().eq(SysFunction::getIsDorisFunction, Boolean.FALSE).and(e -> e.isNull(SysFunction::getJarMd5).or().eq(SysFunction::getJarMd5, "")));
        list.forEach(e -> {
            String md5 = DorisJarUtil.jarUrlToMd5(e.getJarUrl());
            if (StrUtil.isBlank(md5)) {
                throw new BusinessException("通过url获取md5唯一值错误,请核对url是否正确");
            }
            e.setJarMd5(md5);
            this.updateById(e);
        });
        return list;
    }
}
