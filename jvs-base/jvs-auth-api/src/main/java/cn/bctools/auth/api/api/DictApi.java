package cn.bctools.auth.api.api;

import cn.bctools.auth.api.dto.SysDictDto;
import cn.bctools.auth.api.dto.SysDictItemDto;
import cn.bctools.common.utils.R;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * The interface Dict api.
 *
 * @author guojing
 */
@FeignClient(value = "jvs-auth-mgr", contextId = "dict")
public interface DictApi {

    /**
     * The constant PREFIX.
     */
    String PREFIX = "/api/dict";

    /**
     * 通过ID查询字典信息
     *
     * @param id the id
     * @return by id
     */
    @ApiOperation("通过ID查询字典信息")
    @GetMapping(PREFIX + "/{id}")
    R getById(@PathVariable("id") String id);

    /**
     * 分页查询字典信息
     *
     * @param current the current
     * @param size    the size
     * @param sysDict the sys dict
     * @return dict page
     */
    @ApiOperation("分页查询字典信息")
    @GetMapping(PREFIX + "/page/{current}/{size}")
    R getDictPage(@PathVariable("current") Integer current, @PathVariable("size") Integer size, @RequestParam("sysDict") SysDictDto sysDict);

    /**
     * 通过字典类型查找字典
     *
     * @param type the type
     * @return dict by type
     */
    @ApiOperation("通过字典类型查找字典")
    @GetMapping(PREFIX + "/type/{type}")
    R<List<SysDictItemDto>> getDictByType(@PathVariable("type") String type);

    /**
     * 添加字典
     *
     * @param sysDict the sys dict
     * @return r
     */
    @ApiOperation("添加字典")
    @PostMapping(PREFIX)
    R save(@Valid @RequestBody SysDictDto sysDict);

    /**
     * 删除字典
     *
     * @param id the id
     * @return r
     */
    @ApiOperation("删除字典")
    @DeleteMapping(PREFIX + "/{id}")
    R removeById(@PathVariable("id") String id);

    /**
     * 修改字典
     *
     * @param sysDict the sys dict
     * @return r
     */
    @ApiOperation("修改字典")
    @PutMapping(PREFIX)
    R updateById(@Valid @RequestBody SysDictDto sysDict);

    /**
     * 通过字典ID查询字典信息
     *
     * @param dictId the dict id
     * @return dict item by id
     */
    @ApiOperation("通过id查询字典项")
    @GetMapping(PREFIX + "/item/{dictId}")
    R<List<SysDictItemDto>> getDictItemById(@PathVariable("dictId") String dictId);

    /**
     * 通过字典ID查询字典信息
     *
     * @param dictItemIds the dict item ids
     * @return dict item by ids
     */
    @ApiOperation("通过id查询字典项")
    @PostMapping(PREFIX + "/query/item/ids")
    R<List<SysDictItemDto>> getDictItemByIds(@RequestBody List<String> dictItemIds);

    /**
     * 新增或修改字典项
     *
     * @param sysDictItem the sys dict item
     * @param id          the id
     * @return r
     */
    @ApiOperation("新增或修改字典项")
    @PostMapping(PREFIX + "/item/{id}")
    R save(@RequestBody List<SysDictItemDto> sysDictItem, @PathVariable("id") String id);

    /**
     * 多种字典类型的字典列表
     *
     * @param types the types
     * @return multiple types of dict
     */
    @ApiOperation("多种字典类型的字典列表")
    @GetMapping(PREFIX + "/types/dicts")
    R<Map<String, List<SysDictDto>>> getMultipleTypesOfDict(@RequestBody List<String> types);

    /**
     * 所有字典
     *
     * @param description 根据解释获取所有字典
     * @return r
     */
    @GetMapping(PREFIX + "/list/dicts")
    @ApiOperation("所有字典")
    R<List<SysDictDto>> list(@RequestParam(value = "description", required = false) String description);

    /**
     * 所有字典项
     *
     * @param uniqId 唯一标识获取字典
     * @return r
     */
    @GetMapping(PREFIX + "/list/items")
    @ApiOperation("所有字典项")
    R<List<SysDictItemDto>> listItems(@RequestParam(value = "uniqId") String uniqId);

}
