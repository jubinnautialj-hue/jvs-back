package cn.bctools.document.controller;


import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.document.component.UserComponent;
import cn.bctools.document.constant.Constant;
import cn.bctools.document.dto.SubVersionsDto;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.DcVersion;
import cn.bctools.document.service.DcLibraryService;
import cn.bctools.document.service.DcVersionService;
import cn.bctools.document.util.FileOssUtils;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文档版本
 *
 * @Author: GuoZi
 */
@Slf4j
@Api(tags = "文档版本")
@RestController
@RequestMapping(value = "/dcLibrary/version")
@AllArgsConstructor
public class DcVersionController {

    private final DcVersionService dcVersionService;
    private final MongoTemplate mongoTemplate;
    private final DcLibraryService dcLibraryService;
    private final OssTemplate ossTemplate;
    private final UserComponent userComponent;

    @Log
    @GetMapping("/list/{id}")
    @ApiOperation("查询")
    public R<Map<String, List<DcVersion>>> list(@PathVariable String id) {
        List<DcVersion> list = dcVersionService.list(new LambdaQueryWrapper<DcVersion>().orderByDesc(DcVersion::getUpdateTime).eq(DcVersion::getDcId, id));
        //查询用户信息
        if (list.isEmpty()) {
            return R.ok(new HashMap<>());
        }
        List<String> userIds = list.stream().map(DcVersion::getCreateById).filter(StrUtil::isNotBlank).collect(Collectors.toList());
        Map<String, UserDto> userMap = userComponent.getUserMap(userIds);
        list.stream().peek(e -> e.setUserDto(userMap.getOrDefault(e.getCreateById(), new UserDto()))).collect(Collectors.toList());
        Map<String, List<DcVersion>> map = list.parallelStream()
                .peek(e -> e.setGroupTime(DateUtil.format(e.getUpdateTime(), DatePattern.NORM_DATE_PATTERN)))
                .collect(Collectors.groupingBy(DcVersion::getGroupTime, Collectors.toList()));
        return R.ok(map);
    }

    @Log
    @DeleteMapping("/{id}")
    @ApiOperation("删除")
    public R<Boolean> delete(@PathVariable String id) {
        dcVersionService.removeById(id);
        return R.ok(Boolean.TRUE);
    }

    @Log
    @GetMapping("/get/version/{id}")
    @ApiOperation("通过id获取版本封装的内容")
    public R<SubVersionsDto> getVersion(@PathVariable String id) {
        SubVersionsDto subVersionsDto = new SubVersionsDto();
        DcVersion byId = dcVersionService.getById(id);
        String objectUrl = ossTemplate.fileLink(byId.getFilePath(), byId.getBucketName());
        byte[] bytes = HttpUtil.downloadBytes(objectUrl);
        subVersionsDto.setContent(bytes == null ? null : ObjectUtil.deserialize(bytes));
        return R.ok(subVersionsDto);
    }


    @Log
    @PostMapping("/sub/versions")
    @ApiOperation("提交版本号")
    public R<Boolean> subVersions(@RequestBody SubVersionsDto subVersionsDto) {
        DcLibrary dcLibrary = dcLibraryService.getById(subVersionsDto.getDcId());
        //获取知识库名称
        DcLibrary knownDcLibrary = dcLibraryService.getById(dcLibrary.getKnowledgeId());
        String originalName = FileOssUtils.getMultipartFileName(dcLibrary);
        //上传文件
        BaseFile baseFile = ossTemplate.putContent(originalName, subVersionsDto.getContent(), knownDcLibrary.getName(), dcLibrary.getName());
        String versionNumber = subVersionsDto.getVersionNumber();
        if (StrUtil.isEmpty(subVersionsDto.getVersionNumber())) {
            versionNumber = DateUtil.now();
        }
        DcVersion dcVersion = new DcVersion()
                .setVersionNumber(versionNumber)
                .setDcId(subVersionsDto.getDcId())
                .setCreateById(UserCurrentUtils.getUserId())
                .setBucketName(baseFile.getBucketName())
                .setFilePath(baseFile.getFileName())
                .setVoluntarilyIs(Boolean.FALSE);
        dcVersionService.save(dcVersion);
        return R.ok(Boolean.TRUE);
    }

    @Log
    @PostMapping("/update")
    @ApiOperation("修改版本名称")
    public R<Boolean> delete(@RequestBody DcVersion dcVersion) {
        dcVersionService.update(new UpdateWrapper<DcVersion>().lambda().eq(DcVersion::getId, dcVersion.getId()).set(DcVersion::getVersionNumber, dcVersion.getVersionNumber()));
        return R.ok(Boolean.TRUE);
    }

    @Log
    @GetMapping("/rollback/{id}")
    @ApiOperation("版本回退")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> rollback(@PathVariable String id) {
        DcVersion dcVersion = dcVersionService.getById(id);
        if (dcVersion == null) {
            throw new BusinessException("未找到此版本");
        }
        //替换文本之前先把原有的数据进行版本封装 防止数据丢失
        JSONObject json = mongoTemplate.findById(dcVersion.getDcId(), JSONObject.class, Constant.KNOWLEDGE_VERSION_COLLECTION_NAME);
        DcVersion version = new DcVersion().setDcId(dcVersion.getDcId()).setVersionNumber(DateUtil.now()).setMongodbContent(json);
        dcVersionService.save(version);
        return R.ok(Boolean.TRUE);
    }

}
