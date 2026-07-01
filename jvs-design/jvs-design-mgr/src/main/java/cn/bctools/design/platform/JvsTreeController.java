package cn.bctools.design.platform;

import cn.bctools.common.entity.po.TreePo;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.*;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.design.crud.dto.JvsTreeDto;
import cn.bctools.design.crud.entity.JvsTree;
import cn.bctools.design.crud.entity.JvsTreeSaveDto;
import cn.bctools.design.crud.service.JvsTreeService;
import cn.bctools.log.annotation.Log;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.base.Functions;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : GaoZeXi
 */
@Slf4j
@AllArgsConstructor
@Api(value = "树形字典管理", tags = "树形字典管理")
@RestController
@RequestMapping("/platform/app/tree")
public class JvsTreeController {

    final JvsTreeService sysTreeService;

    @Log
    @ApiOperation(value = "查询所有树形字典", notes = "不分页")
    @GetMapping("/list")
    public R<List<Tree<Object>>> treeList(@RequestParam(required = false, defaultValue = "") String name) {
        List<Tree<Object>> tree = sysTreeService.tree(name);
        return R.ok(tree);
    }

    @Log
    @ApiOperation(value = "获取指定树节点", notes = "返回指定树节点的children数组")
    @GetMapping("/get/{uniqueName}")
    public R<List<Tree<Object>>> getById(@PathVariable("uniqueName") String uniqueName) {
        //根据唯一标识确定
        JvsTree sysTree = sysTreeService.getOne(Wrappers.query(new JvsTree().setUniqueName(uniqueName)));
        if (Objects.isNull(sysTree)) {
            return R.ok(Collections.emptyList());
        }
        String groupId = sysTree.getGroupId();
        List<JvsTree> sysTrees = sysTreeService.list(Wrappers.<JvsTree>lambdaQuery().eq(JvsTree::getGroupId, groupId));
        List<TreePo> treePos = sysTrees.stream().map(e -> BeanCopyUtil.copy(e, TreePo.class).setId(e.getUniqueName()).setExtend(e)).collect(Collectors.toList());
        List<Tree<Object>> tree = TreeUtils.tree(treePos, JvsTree.DICT_ID_ROOT);
        List<Tree<Object>> children = tree.get(0).getChildren();
        return R.ok(children);
    }

    /**
     * 树形字典新增节点
     * <p>
     * 1. 顶级节点的上级id默认为-1
     * 2. 同一层级的字典名称不能重复
     *
     * @param dto 字典数据
     * @return 新增后的字典数据
     */
    @Log
    @ApiOperation(value = "树形字典新增节点")
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public R<JvsTree> save(@RequestBody JvsTreeSaveDto dto) {
        JvsTree tree = BeanCopyUtil.copy(dto, JvsTree.class);
        String name = tree.getName();
        String parentId = tree.getParentId();
        // 字典名称校验
        if (StrUtil.isBlank(name)) {
            return R.failed("字典名称不能为空");
        }
        String idStr = IdGenerator.getIdStr();
        if (StringUtils.isBlank(parentId) || JvsTree.DICT_ID_ROOT.equals(parentId)) {
            // 根节点
            parentId = JvsTree.DICT_ID_ROOT;
            tree.setSort(0);
            tree.setGroupId(idStr);
            tree.setUniqueName(idStr);
            tree.setParentId(JvsTree.DICT_ID_ROOT);
        } else {
            // 子节点
            JvsTree parent = sysTreeService.getOne(Wrappers.query(new JvsTree().setUniqueName(parentId)));
            if (Objects.isNull(parent)) {
                log.error("[树形字典] 新增失败, 上级节点不存在, id: {}", parentId);
                return R.failed("上级节点不存在");
            }
            long count = sysTreeService.count(Wrappers.<JvsTree>lambdaQuery().eq(JvsTree::getGroupId, parent.getGroupId()));
            tree.setSort(Long.valueOf(count).intValue());
            tree.setUniqueName(idStr);
            tree.setGroupId(parent.getGroupId());
        }
        sysTreeService.checkName(name, parentId);
        sysTreeService.save(tree);
        return R.ok(tree);
    }

    /**
     * 修改树形字典
     * <p>
     * 1. 层级关系不允许修改
     * 2. 同一层级的字典名称不能重复
     *
     * @param dto 树形字典
     * @return 修改结果
     */
    @Log
    @ApiOperation(value = "修改树形字典")
    @PutMapping
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> update(@RequestBody JvsTreeSaveDto dto) {
        // 层级关系不允许修改
        dto.setParentId(null);
        JvsTree tree = BeanCopyUtil.copy(dto, JvsTree.class);
        String id = tree.getId();
        String name = tree.getName();
        if (Objects.isNull(id)) {
            log.error("[树形字典] 修改失败, id为空");
            return R.failed("该字典不存在");
        }
        JvsTree oldTree = sysTreeService.getOne(Wrappers.query(new JvsTree().setUniqueName(id)));
        ;
        if (Objects.isNull(oldTree)) {
            log.error("[树形字典] 修改失败, 字典不存在, id: {}", id);
            return R.failed("该字典不存在");
        }
        if (StringUtils.isNotBlank(name) && !name.equals(oldTree.getName())) {
            // 校验字典名称
            sysTreeService.checkName(name, oldTree.getParentId());
        }
        tree.setId(oldTree.getId());
        sysTreeService.updateById(tree);
        return R.ok(true, "修改成功");
    }

    @Log
    @ApiOperation(value = "删除树形字典")
    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> delete(@PathVariable("id") String id) {
        sysTreeService.deleteTree(id);
        return R.ok(true, "删除成功");
    }

    @SneakyThrows
    @Log
    @ApiOperation(value = "导入", notes = "导入部分指定数据,不是所有的数据")
    @PostMapping("/import")
    public R<List<Tree<Object>>> treeImport(@RequestParam("file") MultipartFile file) {
        //解析为excel处理方便用户使用是哪种类型
        List<JvsTreeDto> jvsTrees = ExcelUtils.readAll(file.getInputStream(), JvsTreeDto.class);
        List<JvsTree> treeList = BeanCopyUtil.copys(jvsTrees, JvsTree.class);
        for (JvsTree jvsTree : treeList) {
            BeanValidator.validatorException(jvsTree);
        }
        //只能有一个分组
        List<JvsTree> collect = treeList.stream().filter(e -> "-1".equals(e.getParentId())).collect(Collectors.toList());
        if (collect.size() == 1) {
            //处理不同的分组，判断是否是一个分组
            treeList.forEach(e -> e.setGroupId(collect.get(0).getUniqueName()));
        } else {
            return R.failed("需要有一个父级");
        }
        //初始化所有的id
        Map<String, JvsTree> map = treeList.stream().collect(Collectors.toMap(JvsTree::getUniqueName, Functions.identity()));
        //判断哪些有的去掉这部分
        sysTreeService.list(new LambdaQueryWrapper<JvsTree>().in(JvsTree::getUniqueName, map.keySet()))
                //更新数据，这是原有的数据
                .forEach(e -> sysTreeService.updateById(map.remove(e.getUniqueName()).setId(e.getId())));
        //设置剩下的新增的数据
        sysTreeService.saveBatch(map.values());
        return R.ok();
    }

    @SneakyThrows
    @ApiOperation(value = "下载模板", notes = "下载模板")
    @GetMapping("/download/template")
    public void template(HttpServletResponse response) {
        response.setCharacterEncoding("utf-8");
        response.setHeader(Header.CONTENT_DISPOSITION.toString(), "attachment; filename=".concat(URLEncoder.encode("字典.xlsx", "UTF-8")));
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        ExcelUtils.export(response.getOutputStream(), "字典", "字典", JvsTreeDto.class, new ArrayList<>());
    }

    @SneakyThrows
    @ApiOperation(value = "导出", notes = "可以支持导出指定树级数据,方便用户操作自己部分城市的数据")
    @GetMapping("/export")
    public void treeExport(@RequestParam String uniqueName, HttpServletResponse response) {
        JvsTree one = sysTreeService.getOne(Wrappers.query(new JvsTree().setUniqueName(uniqueName)));
        if (ObjectNull.isNull(one)) {
            throw new BusinessException("找不到数据");
        }
        List<JvsTree> list = sysTreeService.list(Wrappers.query(new JvsTree()));

        List<TreePo> treePos = list.stream().map(e -> BeanCopyUtil.copy(e, TreePo.class).setId(e.getUniqueName()).setExtend(e)).collect(Collectors.toList());
        List<Tree<Object>> tree = TreeUtils.tree(treePos, one.getUniqueName());

        List<JvsTree> treeList = new ArrayList<>();
        treeList.add(new JvsTree().setParentId("-1").setName(one.getName()).setUniqueName(one.getUniqueName()).setSort(one.getSort()));
        openTree(tree, treeList);
        response.setCharacterEncoding("utf-8");
        response.setHeader(Header.CONTENT_DISPOSITION.toString(), "attachment; filename=".concat(URLEncoder.encode(one.getName() + ".xlsx", "UTF-8")));
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        ServletOutputStream outputStream = response.getOutputStream();
        ExcelUtils.export(outputStream, one.getName(), one.getName(), JvsTreeDto.class, BeanCopyUtil.copys(treeList, JvsTreeDto.class));
    }

    private void openTree(List<Tree<Object>> list, List<JvsTree> treeList) {
        if (ObjectNull.isNotNull(list)) {
            for (Tree<Object> child : list) {
                //去掉ID值
                JvsTree copy = BeanCopyUtil.copy(child.get("extend"), JvsTree.class).setId(null);
                treeList.add(copy);
                openTree(child.getChildren(), treeList);
            }
        }
    }

}
