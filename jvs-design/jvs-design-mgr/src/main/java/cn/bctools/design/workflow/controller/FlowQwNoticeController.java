package cn.bctools.design.workflow.controller;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.design.config.QwNoticeConfig;
import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.service.FlowQwNoticeService;
import cn.bctools.design.workflow.service.FlowTaskApprovalRecordService;
import cn.bctools.design.workflow.service.FlowTaskCarbonCopyService;
import cn.bctools.design.workflow.service.FlowTaskService;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.web.utils.HttpRequestUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @author zhuxiaokang
 */
@Slf4j
@Api(tags = "[workflow]工作流企业微信消息")
@AllArgsConstructor
@RestController
@RequestMapping("/base/workflow/qw/notice")
public class FlowQwNoticeController {

    FlowQwNoticeService flowQwNoticeService;
    @Log
    @ApiOperation("测试创建消息通知")
    @GetMapping("/create")
    @Transactional(rollbackFor = Exception.class)
    public R<String> create() throws Exception{

        Map flowParam = new HashMap();
        //结果格式:  {"bizTaskAndTaskId":{"1253038444628619298":"1827966645317992450_1253038444628619298"}}}
        String s = flowQwNoticeService.create(flowParam);
        //入库保存结果,与请求参数关联.

        return R.ok(s);
    }

    @Log
    @ApiOperation("测试关闭消息通知")
    @GetMapping("/create")
    @Transactional(rollbackFor = Exception.class)
    public R<String> close() throws Exception{

        List<String> params = new ArrayList<String>();

        // 请求接口的示例参数 - 参数值为创建结果集中的 bizTaskAndTaskId
        List<String> bb=new ArrayList<>();
        bb.add("1827966645317992450_1253038444628619298");

       String res= flowQwNoticeService.close(params);


        return R.ok(res);
    }


}
