package cn.bctools.function.service.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.function.entity.po.FunctionBusinessPo;
import cn.bctools.function.mapper.FunctionBusinessMapper;
import cn.bctools.function.service.FunctionBusinessService;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author zhuxiaokang
 */
@Service
public class FunctionBusinessServiceImpl extends ServiceImpl<FunctionBusinessMapper, FunctionBusinessPo> implements FunctionBusinessService {


    @Override
    public void checkType(String id, Object result, String msg) {
        FunctionBusinessPo po = getById(id);
        if (ObjectNull.isNotNull(po.getCheckType())) {
            switch (po.getCheckType()) {
                case cannot_be_empty:
                    if (ObjectNull.isNull(result)) {
                        if (ObjectNull.isNotNull(msg)) {
                            throw new BusinessException(msg);
                        } else {
                            throw new BusinessException("参数(" + po.getDescription() + ")获取为空校验不通过");
                        }
                    }
                case cannot_be_empty_array:
                    if (result != null) {
                        if (JSONUtil.isTypeJSONArray(JSONUtil.toJsonStr(result))) {
                            //表示是空数组
                            if (ObjectNull.isNotNull(msg)) {
                                throw new BusinessException(msg);
                            } else {
                                throw new BusinessException("参数(" + po.getDescription() + ")获取为空校验不通过");
                            }
                        }
                    }
            }
        }
    }

}
