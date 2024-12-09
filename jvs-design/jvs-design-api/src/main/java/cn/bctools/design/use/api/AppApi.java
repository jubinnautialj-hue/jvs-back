package cn.bctools.design.use.api;

import cn.bctools.common.utils.R;
import cn.bctools.design.use.api.dto.DataModelDto;
import cn.bctools.design.use.api.dto.ModeDto;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * The interface App api.
 *
 * @author Administrator
 */
@Component
@FeignClient(value = "jvs-design-mgr", contextId = "useapp")
public interface AppApi {

    /**
     * The constant PREFIX.
     */
    String PREFIX = "/api/jvsdesign/menu";
    /**
     * 菜单类型: 应用
     */
    String TYPE_JVS_APP = "jvsapp";
    /**
     * key: 设计类型
     */
    String KEY_DESIGN_TYPE = "design";
    /**
     * key: 是否为应用管理员
     */
    String KEY_IS_APP_ADMIN = "designRole";

//    /**
//     * 获取所有菜单数据(树形结构)
//     *
//     * @param isAdmin 是否为管理员
//     * @param admin
//     * @return 菜单数据
//     */
//    @ApiOperation("获取所有")
//    @GetMapping(PREFIX)
//    R<List> getMenuTree(@RequestParam(value = "isAdmin") Boolean isAdmin, @RequestParam("mobile") boolean mobile);

    /**
     * 菜单查询
     *
     * @param isAdmin 是否为管理员
     * @param name    查询条件-菜单名称(模糊搜索)
     * @param mobile
     * @return 搜索结果
     */
//    @ApiOperation("获取应用APP菜单-模糊搜索")
//    @GetMapping(PREFIX + "/search")
//    R<List> getMenu(@RequestParam(value = "isAdmin") Boolean isAdmin, @RequestParam(required = false, value = "name") String name, @RequestParam(required = false, value = "mobile") boolean mobile);

    /**
     * 根据模式查询 获取应用
     *
     * @param msg the msg
     * @return 返回模型信息 r
     */
    @GetMapping(PREFIX + "/base/app")
    R<List<DataModelDto>> apps(@RequestParam("mode") String msg);

    /**
     * 获取模式
     *
     * @return r
     */
    @GetMapping(PREFIX + "/base/mode")
    R<List<ModeDto>> mode();

    /**
     * 低代码租户数据存储大小
     *
     * @return r
     */
    @ApiOperation("低代码租户数据存储大小")
    @GetMapping(PREFIX + "/data/size")
    R<Long> dataSize();

}
