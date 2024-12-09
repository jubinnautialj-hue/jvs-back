//package cn.bctools.rule.ess.impl.processsigning;
//
//import cn.hutool.core.io.FileUtil;
//
//import java.io.File;
//import java.nio.charset.Charset;
//import java.util.Arrays;
//import java.util.List;
//import java.util.stream.Collectors;
//
//public class tt {
//    public static void main(String[] args) {
//        String s = "##Create signing process related interfaces\n" + "\n" + "Create a signing process using PDF files\n" + "\n" + "Template Initiate Contract - Create Signing Process\n" + "\n" + "Template Initiate Contract - Create Electronic Document\n" + "\n" + "Template Initiate Contract - Initiate Signing Process\n" + "\n" + "Create a contract group signing process through multiple templates\n" + "\n" + "Create a contract group signing process through multiple files\n" + "\n" + "Create a one code multi scan process to sign a QR code\n" + "\n" + "Obtain the signing link to jump to Tencent's electronic signature mini program\n" + "\n" + "Obtain a batch signing link to jump to Tencent's electronic signature mini program\n" + "\n" + "Obtain individual user H5 signing link\n" + "\n" + "Obtain H5 bulk signing link\n" + "\n" + "##Query signing process related interfaces\n" + "\n" + "Query process basic information\n" + "\n" + "Query the detailed information of the contract process\n" + "\n" + "Query Process Fill in Control Content\n" + "\n" + "##Control signing process related interfaces\n" + "\n" + "Submit approval results for the signing process\n" + "\n" + "Supplementary signing process signatory information\n" + "\n" + "Contract reminder\n" + "\n" + "Initiate termination of agreement\n" + "\n" + "Revoking a single contract process\n" + "\n" + "Batch revocation signing process\n" + "\n" + "Cancel the one code multiple scan process and sign the QR code\n" + "\n" + "##Embedable page related interfaces\n" + "\n" + "Set the theme configuration of our enterprise's embedded page\n" + "\n" + "Create a web page for initiating the process\n" + "\n" + "Obtain the web page for enterprise contract signing\n" + "\n" + "Get other embeddable web pages\n" + "\n" + "##Interface related to electronic seal management\n" + "\n" + "Querying Enterprise Electronic Seals\n" + "\n" + "Create authorization for enterprise employee seals\n" + "\n" + "Revoking Seal Authorization for Enterprise Employees\n" + "\n" + "Create a personal seal for importing prescription forms\n" + "\n" + "Create an enterprise electronic seal\n" + "\n" + "##Certificate management related interfaces\n" + "\n" + "Process document verification\n" + "\n" + "Submit the task of applying for certification report\n" + "\n" + "Obtain certification report task execution results\n" + "\n" + "Interface for querying personal certificates\n" + "\n" + "##Institutional and Organizational Related Interfaces\n" + "\n" + "Obtain a list of enterprise department information\n" + "\n" + "Create Enterprise Department\n" + "\n" + "Update enterprise department information\n" + "\n" + "Delete Enterprise Department\n" + "\n" + "Modifying Enterprise Callback Configuration\n" + "\n" + "Query the list of group enterprises\n" + "\n" + "Query Enterprise Extension Service Authorization Information\n" + "\n" + "Create Enterprise Extension Service Authorization\n" + "\n" + "Delete Enterprise Extension Service Authorization\n" + "\n" + "Create enterprise information change link\n" + "\n" + "##Institutional and employee related interfaces\n" + "\n" + "Query Enterprise Employee Information List\n" + "\n" + "Creating Enterprise Employees\n" + "\n" + "Update enterprise employee information\n" + "\n" + "Removing Enterprise Employees\n" + "\n" + "Binding employee Userid to customer system Openid\n" + "\n" + "Unbind employee UserId from customer system OpenId\n" + "\n" + "##Role management related interfaces\n" + "\n" + "Query Enterprise Role List\n" + "Bind employee roles\n" + "\n" + "Unbind employee roles\n" + "\n" + "Create Enterprise Role\n" + "\n" + "Update Enterprise Role\n" + "\n" + "##Personal account management related interfaces\n" + "\n" + "Obtain the activation status of automatic signing by individual users\n" + "\n" + "Obtain the activation link automatically signed by individual users\n" + "\n" + "Turn off automatic signature function for individual users\n" + "\n" + "Revoking the activation link automatically signed by individual users\n" + "\n" + "Query whether an individual user has a real name through AuthCode\n" + "\n" + "Obtain image of personal user authentication certificate\n" + "\n" + "Get the link to the automatic seal signing mini program\n" + "\n" + "##Billing related interfaces\n" + "\n" + "Query the usage of enterprise billing";
//
//
//        String s1 = "## 创建签署流程相关接口\n" + "用PDF文件创建签署流程\n" + "模板发起合同-创建签署流程\n" + "模板发起合同-创建电子文档\n" + "模板发起合同-发起签署流程\n" + "通过多模板创建合同组签署流程\n" + "通过多文件创建合同组签署流程\n" + "创建一码多扫流程签署二维码\n" + "获取跳转至腾讯电子签小程序的签署链接\n" + "获取跳转至腾讯电子签小程序的批量签署链接\n" + "获取个人用户H5签署链接\n" + "获取H5批量签署链接\n" + "## 查询签署流程相关接口\n" + "查询流程基础信息\n" + "查询合同流程的详情信息\n" + "查询流程填写控件内容\n" + "## 控制签署流程相关接口\n" + "提交签署流程审批结果\n" + "补充签署流程签署人信息\n" + "合同催办\n" + "发起解除协议\n" + "撤销单个合同流程\n" + "批量撤销签署流程\n" + "取消一码多扫流程签署二维码\n" + "## 可嵌入页面相关接口\n" + "设置本企业嵌入式页面主题配置\n" + "创建发起流程web页面\n" + "获取企业签署合同web页面\n" + "获取其他可嵌入web页面\n" + "## 电子印章管理相关接口\n" + "查询企业电子印章\n" + "创建企业员工印章授权\n" + "撤销企业员工的印章授权\n" + "创建导入处方单个人印章\n" + "创建企业电子印章\n" + "## 证书管理相关接口\n" + "流程文件验签\n" + "提交申请出证报告任务\n" + "获取出证报告任务执行结果\n" + "查询个人证书接口\n" + "## 机构与组织相关接口\n" + "获取企业部门信息列表\n" + "创建企业部门\n" + "更新企业部门信息\n" + "删除企业部门\n" + "修改企业回调配置\n" + "查询集团企业列表\n" + "查询企业扩展服务授权信息\n" + "创建企业扩展服务授权\n" + "删除企业扩展服务授权\n" + "创建企业信息变更链接\n" + "## 机构与员工相关接口\n" + "查询企业员工信息列表\n" + "创建企业员工\n" + "更新企业员工信息\n" + "移除企业员工\n" + "员工Userid与客户系统Openid绑定\n" + "解除员工UserId与客户系统OpenId的绑定\n" + "## 角色管理相关接口\n" + "查询企业角色列表\n" + "绑定员工角色\n" + "解绑员工角色\n" + "创建企业角色\n" + "更新企业角色\n" + "## 个人账号管理相关接口\n" + "获取个人用户自动签的开通状态\n" + "获取个人用户自动签的开通链接\n" + "关闭个人用户自动签功能\n" + "撤销个人用户自动签的开通链接\n" + "通过AuthCode查询个人用户是否实名\n" + "获取个人用户认证证书图片\n" + "获取设置自动签印章小程序链接\n" + "## 计费相关接口\n" + "查询企业计费使用情况";
//
//
//        List<String> list = (Arrays.stream(s.split("\n")).filter(e -> e.length() > 2).map(e -> {
//            if (e.startsWith("#")) {
//                return e;
//            }
//            return Arrays.stream(e.split(" ")).filter(a -> a.length() > 1).map(a -> Character.toUpperCase(a.charAt(0)) + a.substring(1)).collect(Collectors.joining()) + "ServiceImpl";
//        }).collect(Collectors.toList()));
//
//        String paht = "/Users/guojing/IdeaProjects/jvs-design/jvs-function/jvs-function-api-tencent-ess-plug/src/main/java/cn/bctools/rule/ess/impl/processsigning/";
//        File file = new File("/Users/guojing/IdeaProjects/jvs-design/jvs-function/jvs-function-api-tencent-ess-plug/src/main/java/cn/bctools/rule/ess/impl/processsigning/Name.java");
//        String path = "/Users/guojing/IdeaProjects/jvs-design/jvs-function/jvs-function-api-tencent-ess-plug/src/main/java/cn/bctools/rule/ess/impl/";
////        String s2 = FileUtil.readString(file, Charset.defaultCharset());
////        String[] split = s1.split("\n");
////        for (int i = 0; i < split.length; i++) {
////            if(split[i].contains("##")){
////                continue;
////            }
////            log.info(split[i] + list.get(i));
////            String s3 = s2.replaceAll("name1", split[i]);
////            s3 = s3.replaceAll("Name", list.get(i));
////            FileUtil.writeString(s3, new File(paht + list.get(i) + ".java"), Charset.defaultCharset());
////        }
//        list.stream().filter(e -> e.startsWith("#"))
//                .forEach(e -> {
//                    log.info(e);
//                });
//    }
//}
