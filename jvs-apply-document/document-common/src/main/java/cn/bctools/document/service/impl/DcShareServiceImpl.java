package cn.bctools.document.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.DcShareEntity;
import cn.bctools.document.entity.enums.DcLibraryReadEnum;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.enums.ShareTimeTypeEnums;
import cn.bctools.document.mapper.DcShareMapper;
import cn.bctools.document.service.DcLibraryService;
import cn.bctools.document.service.DcShareService;
import cn.bctools.document.util.LinkUtil;
import cn.bctools.document.vo.req.ShareCheckReqVo;
import cn.bctools.document.vo.res.ShareCheckResVo;
import cn.bctools.oauth2.utils.AuthorityManagementUtils;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Auto Generator
 */
@Service
@AllArgsConstructor
public class DcShareServiceImpl extends ServiceImpl<DcShareMapper, DcShareEntity> implements DcShareService {
    private final DcLibraryService dcLibraryService;

    @Override
    public DcShareEntity settingLink(DcShareEntity dcShareEntity) {
        if (dcShareEntity.getShare()) {
            DcLibrary dcLibrary = dcLibraryService.getById(dcShareEntity.getId());
            if (Objects.isNull(dcLibrary)) {
                throw new BusinessException("未找到此数据");
            }
            //获取知识库信息
            DcLibrary knowledgeLibrary = dcLibraryService.getById(dcLibrary.getKnowledgeId());
            if (Objects.isNull(knowledgeLibrary)) {
                throw new BusinessException("未找到此知识库信息");
            }
            dcShareEntity.setKnowledgeId(knowledgeLibrary.getId())
                    .setType(dcLibrary.getType())
                    .setNameSuffix(dcLibrary.getNameSuffix());
            createLink(dcShareEntity, knowledgeLibrary.getName(), dcLibrary.getName(), dcLibrary.getType());
            //判断是否存在有效期
            if (!dcShareEntity.getUpdatePassWord()) {
                dcShareEntity.setShareEndTime(null);
                //生成时效性
                if (dcShareEntity.getShareTimeType() != null) {
                    int offsetValue = 0;
                    switch (dcShareEntity.getShareTimeType()) {
                        case THIRTY_DAY:
                            offsetValue = 30;
                            break;
                        case SEVEN_DAYS:
                            offsetValue = 7;
                        default:
                    }
                    if (offsetValue > 0) {
                        DateTime dateTime = DateUtil.offsetDay(DateUtil.date(), offsetValue);
                        dcShareEntity.setShareEndTime(DateUtil.format(dateTime, DatePattern.NORM_DATETIME_PATTERN));
                    }
                }
            }
            this.saveOrUpdate(dcShareEntity);
        } else {
            this.removeById(dcShareEntity.getId());
        }
        return dcShareEntity;
    }

    @Override
    public Boolean checkShare(String id, Boolean isCheckDown) {
        //判断是否为分享
        DcLibrary byId = dcLibraryService.getById(id);
        if (byId == null) {
            throw new BusinessException("未找到此数据");
        }
        DcLibraryReadEnum type = byId.getShareRole();
        boolean isKnowledge = byId.getType().equals(DcLibraryTypeEnum.knowledge);
        if (!isKnowledge) {
            type = dcLibraryService.getById(byId.getKnowledgeId()).getShareRole();
        }
        if (!type.equals(DcLibraryReadEnum.all)) {
            //判断是否存在文件夹分享
            List<String> pathId = byId.getPathId();
            //添加本身 防止本身为分享主体
            pathId.add(byId.getId());
            long count = this.count(new LambdaQueryWrapper<DcShareEntity>().in(DcShareEntity::getDcId, pathId).eq(isCheckDown,DcShareEntity::getDownStatus,Boolean.TRUE));
            if (count == 0) {
                throw new BusinessException("权限不足请联系文档所有者");
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public ShareCheckResVo checkShare(ShareCheckReqVo reqVo) {
        ShareCheckResVo resVo = new ShareCheckResVo();
        resVo.setCheck(Boolean.FALSE);
        DcShareEntity dcShareEntity = this.getOne(new LambdaQueryWrapper<DcShareEntity>().eq(DcShareEntity::getShareKey, reqVo.getKey()));
        if (Objects.isNull(dcShareEntity) || !dcShareEntity.getShare()) {
            resVo.setShareStatus(Boolean.FALSE);
            return resVo;
        }
        //判断是否在有效期内
        if (dcShareEntity.getShareTimeType() != null && !dcShareEntity.getShareTimeType().equals(ShareTimeTypeEnums.PERPETUAL)) {
            boolean before = DateUtil.parse(dcShareEntity.getShareEndTime(), DatePattern.NORM_DATETIME_PATTERN).isBefore(DateUtil.date());
            if (before) {
                resVo.setShareStatus(Boolean.FALSE);
                return resVo;
            }
        }
        //获取分享数据
        DcLibrary dcLibrary = dcLibraryService.getById(dcShareEntity.getDcId());
        //防止数据被删除
        if (!Optional.ofNullable(dcLibrary).isPresent()) {
            resVo.setShareStatus(Boolean.FALSE);
            resVo.setCheck(Boolean.FALSE);
            return resVo;
        }
        // 查询用户头像
        UserDto userDto = AuthorityManagementUtils.getUserById(dcLibrary.getCreateById());
        resVo.setId(dcLibrary.getId());
        resVo.setKnowledgeId(DcLibraryTypeEnum.knowledge.equals(dcLibrary.getType()) ? dcLibrary.getId() : dcLibrary.getKnowledgeId());
        resVo.setTenantId(dcLibrary.getTenantId());
        resVo.setType(dcLibrary.getType());
        resVo.setShareStatus(Boolean.TRUE);
        resVo.setCheck(Boolean.TRUE);
        resVo.setHeadImg(userDto.getHeadImg());

        // 2. 校验密码
        resVo.setNeedPwd(Boolean.FALSE);
        // 存在分享密码，则校验密码
        if (StringUtils.isNotBlank(dcShareEntity.getSharePassword())) {
            resVo.setNeedPwd(Boolean.TRUE);

            if (!dcShareEntity.getSharePassword().equals(reqVo.getPwd())) {
                resVo.setCheck(Boolean.FALSE);
            }
        }
        resVo.setDcLibrary(dcLibrary);
        return resVo;
    }


    /**
     * 创建链接
     *
     * @param knowledgeName 文库名称
     * @param name          文档名称
     * @param dcShareEntity 文档分享设置
     */
    public void createLink(DcShareEntity dcShareEntity, String knowledgeName, String name, DcLibraryTypeEnum type) {
        String key;
        if (dcShareEntity.getUpdatePassWord()) {
            //如果只是修改密码就不用重新生成key 使用原来的key
            DcShareEntity byId = this.getById(dcShareEntity.getId());
            key = byId.getShareKey();
        } else {
            // 生成分享key，每次分享都生成不同的key。一个资源同时只存在一个分享key
            key = DigestUtil.md5Hex(new StringBuilder()
                    .append(dcShareEntity.getDcId())
                    .append(System.currentTimeMillis())
                    .append(RandomUtil.randomInt(1000, 9999))
                    .toString());
        }

        // 分享链接参数：security=是否加密&key=分享key
        String link = buildShareLink(dcShareEntity.getSharePassword(), key, type.getValue(), dcShareEntity.getDownStatus());
        if (StrUtil.isNotBlank(dcShareEntity.getShareLinkOriginal())) {
            link = dcShareEntity.getShareLinkOriginal() + "&" + link;
        }
        //长链接转为短链接
        String linkShortUrl = LinkUtil.linkShortUrl(link);
        linkShortUrl += "《{}》({})";
        linkShortUrl = StrUtil.format(linkShortUrl, name, knowledgeName);
        dcShareEntity.setShareLink(linkShortUrl)
                .setShareLinkOriginal(link)
                .setShareKey(key);
    }

    /**
     * 构造分享链接
     *
     * @param pwd 分享密码
     * @param key 分享key
     * @return
     */
    private String buildShareLink(String pwd, String key, String type, Boolean downStatus) {
        //设置默认值
        downStatus = Optional.ofNullable(downStatus).orElse(Boolean.FALSE);
        return String.format("security=%s&key=%s&type=%s&downStatus=%s", StringUtils.isNotBlank(pwd), key, type, downStatus);
    }

}
