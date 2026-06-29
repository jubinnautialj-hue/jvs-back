package cn.bctools.document.service;

import cn.bctools.document.entity.DcIdentifying;
import cn.bctools.document.entity.enums.IdentifyingKeyEnum;
import cn.bctools.document.entity.enums.IdentifyingTypeEnum;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author Auto Generator
 */
public interface DcIdentifyingService extends IService<DcIdentifying> {


    /**
     * 根据类型获取权限标识 这里统一排除所有者
     * 如果类型不传 不区分类型
     *
     * @param identifyingType 权限类型
     * @param possessorIs     是否包含所有者才能使用的权限标识
     * @return 权限标识
     */
    List<DcIdentifying> basisTypeToPossessor(IdentifyingTypeEnum identifyingType, Boolean possessorIs);

    /**
     * 根据类型获取数据
     *
     * @param identifyingKeyEnum 类型
     * @return 权限标识
     */
    List<DcIdentifying> typeGetList(IdentifyingKeyEnum... identifyingKeyEnum);
}
