package cn.bctools.document.service;

import cn.bctools.document.entity.DcTagBinding;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author Auto Generator
 */
public interface DcTagBindingService extends IService<DcTagBinding> {

    String getTagNameByDcId(String dcId);
}
