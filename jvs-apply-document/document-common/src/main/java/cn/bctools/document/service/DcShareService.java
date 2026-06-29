package cn.bctools.document.service;

import cn.bctools.document.entity.DcShareEntity;
import cn.bctools.document.vo.req.ShareCheckReqVo;
import cn.bctools.document.vo.res.ShareCheckResVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author Auto Generator
 */
public interface DcShareService extends IService<DcShareEntity> {
    /**
     * 分享设置
     *
     * @param dcShareEntity 分享的设置
     * @return 设置完成后的数据
     */
    DcShareEntity settingLink(DcShareEntity dcShareEntity);

    /**
     * 校验分享
     *
     * @param reqVo 入参
     * @return 校验结果
     */
    ShareCheckResVo checkShare(ShareCheckReqVo reqVo);

    /**
     * 分享校验
     *
     * @param id 文档id
     * @param isCheckDown 是否需要校验 下载权限
     */
    Boolean checkShare(String id,Boolean isCheckDown);

}
