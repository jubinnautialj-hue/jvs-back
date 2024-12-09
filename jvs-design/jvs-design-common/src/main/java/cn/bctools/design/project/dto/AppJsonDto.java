package cn.bctools.design.project.dto;

import cn.bctools.design.data.fields.enums.DataFieldType;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class AppJsonDto {

    @ApiModelProperty("系统名称")
    String system;
    @ApiModelProperty("菜单")
    List<AppMenusDto> children;

    @Data
    @Accessors(chain = true)
    public static class AppMenusDto {
        @ApiModelProperty("菜单名称")
        String menu;
        @ApiModelProperty("字段")
        List<AppJsonField> fields;
    }

    @Data
    @Accessors(chain = true)
    public static class AppJsonField {
        @ApiModelProperty("字段名")
        String field;
        @ApiModelProperty("中文名")
        String name;
        @ApiModelProperty("字段类型")
        String type;
        @ApiModelProperty("字段类型")
        DataFieldType fieldType;
        @ApiModelProperty("可选项")
        List<String> dicData;
    }


    public static void main(String[] args) {
        String str="{\n" +
                "  \"system\": \"信息管理平台\",\n" +
                "  \"children\": [\n" +
                "    {\n" +
                "          \"menu\": \"1. 材料入库管理\",\n" +
                "          \"fields\": [\n" +
                "            {\"field\": \"materialName\", \"name\": \"物料名称\", \"type\": \"单行文本\"},\n" +
                "            {\"field\": \"quantity\", \"name\": \"数量\", \"type\": \"计数器\"},\n" +
                "            {\"field\": \"supplier\", \"name\": \"供应商\", \"type\": \"单行文本\"},\n" +
                "            {\"field\": \"entryDate\", \"name\": \"入库日期\", \"type\": \"日期\"},\n" +
                "            {\"field\": \"batchNumber\", \"name\": \"批次号\", \"type\": \"单行文本\"},\n" +
                "            {\"field\": \"warehouseLocation\", \"name\": \"仓库位置\", \"type\": \"单行文本\"},\n" +
                "            {\"field\": \"status\", \"name\": \"状态\", \"type\": \"下拉框\"},\n" +
                "            {\"field\": \"remarks\", \"name\": \"备注\", \"type\": \"多行文本\"}\n" +
                "          ]\n" +
                "    },\n" +
                "    {\n" +
                "          \"menu\": \"2. 订单管理\",\n" +
                "          \"fields\": [\n" +
                "            {\"field\": \"orderNumber\", \"name\": \"订单编号\", \"type\": \"流水号\"},\n" +
                "            {\"field\": \"customerName\", \"name\": \"客户名称\", \"type\": \"单行文本\"},\n" +
                "            {\"field\": \"orderDate\", \"name\": \"订单日期\", \"type\": \"日期\"},\n" +
                "            {\"field\": \"totalAmount\", \"name\": \"总金额\", \"type\": \"单行文本\"},\n" +
                "            {\"field\": \"paymentStatus\", \"name\": \"支付状态\", \"type\": \"下拉框\"},\n" +
                "            {\"field\": \"deliveryDate\", \"name\": \"交货日期\", \"type\": \"日期\"},\n" +
                "            {\"field\": \"shippingMethod\", \"name\": \"运输方式\", \"type\": \"下拉框\"},\n" +
                "            {\"field\": \"orderRemarks\", \"name\": \"订单备注\", \"type\": \"多行文本\"}\n" +
                "          ]\n" +
                "    },\n" +
                "    {\n" +
                "          \"menu\": \"3. 库存管理\",\n" +
                "          \"fields\": [\n" +
                "            {\"field\": \"materialID\", \"name\": \"物料ID\", \"type\": \"单行文本\"},\n" +
                "            {\"field\": \"currentStock\", \"name\": \"当前库存\", \"type\": \"计数器\"},\n" +
                "            {\"field\": \"minimumStock\", \"name\": \"最低库存\", \"type\": \"计数器\"},\n" +
                "            {\"field\": \"warehouseID\", \"name\": \"仓库ID\", \"type\": \"单行文本\"},\n" +
                "            {\"field\": \"lastUpdated\", \"name\": \"最后更新时间\", \"type\": \"固定时间\"},\n" +
                "            {\"field\": \"reorderLevel\", \"name\": \"补货水平\", \"type\": \"计数器\"},\n" +
                "            {\"field\": \"stockStatus\", \"name\": \"库存状态\", \"type\": \"下拉框\"},\n" +
                "            {\"field\": \"stockRemarks\", \"name\": \"库存备注\", \"type\": \"多行文本\"}\n" +
                "          ]\n" +
                "    },\n" +
                "    {\n" +
                "          \"menu\": \"4. 财务管理\",\n" +
                "          \"fields\": [\n" +
                "            {\"field\": \"recordID\", \"name\": \"记录ID\", \"type\": \"流水号\"},\n" +
                "            {\"field\": \"transactionDate\", \"name\": \"交易日期\", \"type\": \"日期\"},\n" +
                "            {\"field\": \"amount\", \"name\": \"金额\", \"type\": \"单行文本\"},\n" +
                "            {\"field\": \"transactionType\", \"name\": \"交易类型\", \"type\": \"下拉框\"},\n" +
                "            {\"field\": \"paymentMethod\", \"name\": \"支付方式\", \"type\": \"下拉框\"},\n" +
                "            {\"field\": \"relatedOrder\", \"name\": \"相关订单\", \"type\": \"单行文本\"},\n" +
                "            {\"field\": \"status\", \"name\": \"状态\", \"type\": \"下拉框\"},\n" +
                "            {\"field\": \"transactionRemarks\", \"name\": \"交易备注\", \"type\": \"多行文本\"}\n" +
                "          ]\n" +
                "    },\n" +
                "    {\n" +
                "          \"menu\": \"5. 人员管理\",\n" +
                "          \"fields\": [\n" +
                "            {\"field\": \"employeeID\", \"name\": \"员工ID\", \"type\": \"流水号\"},\n" +
                "            {\"field\": \"employeeName\", \"name\": \"员工姓名\", \"type\": \"单行文本\"},\n" +
                "            {\"field\": \"department\", \"name\": \"部门\", \"type\": \"部门选择\"},\n" +
                "  \n" +
                "            {\"field\": \"position\", \"name\": \"职位\", \"type\": \"岗位选择\"},\n" +
                "            {\"field\": \"hireDate\", \"name\": \"入职日期\", \"type\": \"日期\"},\n" +
                "            {\"field\": \"contactNumber\", \"name\": \"联系电话\", \"type\": \"单行文本\"},\n" +
                "            {\"field\": \"email\", \"name\": \"电子邮箱\", \"type\": \"单行文本\"},\n" +
                "            {\"field\": \"employeeRemarks\", \"name\": \"员工备注\", \"type\": \"多行文本\"}\n" +
                "          ]\n" +
                "    },\n" +
                "    {\n" +
                "          \"menu\": \"6. 项目管理\",\n" +
                "          \"fields\": [\n" +
                "            {\"field\": \"projectID\", \"name\": \"项目ID\", \"type\": \"流水号\"},\n" +
                "            {\"field\": \"projectName\", \"name\": \"项目名称\", \"type\": \"单行文本\"},\n" +
                "            {\"field\": \"startDate\", \"name\": \"开始日期\", \"type\": \"日期\"},\n" +
                "            {\"field\": \"endDate\", \"name\": \"结束日期\", \"type\": \"日期\"},\n" +
                "            {\"field\": \"projectManager\", \"name\": \"项目经理\", \"type\": \"用户选择\"},\n" +
                "            {\"field\": \"budget\", \"name\": \"预算\", \"type\": \"单行文本\"},\n" +
                "            {\"field\": \"progress\", \"name\": \"进度\", \"type\": \"计数器\"},\n" +
                "            {\"field\": \"projectRemarks\", \"name\": \"项目备注\", \"type\": \"多行文本\"}\n" +
                "          ]\n" +
                "    },\n" +
                "    {\n" +
                "          \"menu\": \"7. 供应商管理\",\n" +
                "          \"fields\": [\n" +
                "            {\"field\": \"supplierID\", \"name\": \"供应商ID\", \"type\": \"流水号\"},\n" +
                "            {\"field\": \"supplierName\", \"name\": \"供应商名称\", \"type\": \"单行文本\"},\n" +
                "            {\"field\": \"contactPerson\", \"name\": \"联系人\", \"type\": \"单行文本\"},\n" +
                "            {\"field\": \"contactNumber\", \"name\": \"联系电话\", \"type\": \"单行文本\"},\n" +
                "            {\"field\": \"email\", \"name\": \"电子邮箱\", \"type\": \"单行文本\"},\n" +
                "            {\"field\": \"address\", \"name\": \"地址\", \"type\": \"多行文本\"},\n" +
                "            {\"field\": \"rating\", \"name\": \"评分\", \"type\": \"计数器\"},\n" +
                "            {\"field\": \"supplierRemarks\", \"name\": \"供应商备注\", \"type\": \"多行文本\"}\n" +
                "          ]\n" +
                "    },\n" +
                "    {\n" +
                "          \"menu\": \"8. 报告管理\",\n" +
                "          \"fields\": [\n" +
                "            {\"field\": \"reportID\", \"name\": \"报告ID\", \"type\": \"流水号\"},\n" +
                "            {\"field\": \"reportName\", \"name\": \"报告名称\", \"type\": \"单行文本\"},\n" +
                "            {\"field\": \"reportDate\", \"name\": \"报告日期\", \"type\": \"日期\"},\n" +
                "            {\"field\": \"reportType\", \"name\": \"报告类型\", \"type\": \"下拉框\"},\n" +
                "            {\"field\": \"generatedBy\", \"name\": \"生成者\", \"type\": \"用户选择\"},\n" +
                "            {\"field\": \"status\", \"name\": \"状态\", \"type\": \"下拉框\",   \"dicData\": [\"在职\", \"离职\", \"休假\"]},\n" +
                "            {\"field\": \"fileAttachment\", \"name\": \"文件附件\", \"type\": \"上传文件\"},\n" +
                "            {\"field\": \"reportRemarks\", \"name\": \"报告备注\", \"type\": \"多行文本\"}\n" +
                "          ]\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";
        AppJsonDto appJsonDtos = JSONObject.parseObject(str, AppJsonDto.class);
//        Map<String, DataFieldType> collect = Arrays.stream(DataFieldType.values()).collect(Collectors.toMap(e -> e.getDesc(), Function.identity()));
//        appJsonDtos.forEach(e -> e.getFields().forEach(a -> a.setFieldType(collect.get(a.getType()))));

        System.out.println(appJsonDtos);
    }
}
