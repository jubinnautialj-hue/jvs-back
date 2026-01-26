package cn.bctools.design.util;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.rule.entity.ParameterMap;
import cn.bctools.design.rule.entity.RuleExternalPo;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.http.webservice.SoapClient;
import cn.hutool.http.webservice.SoapProtocol;
import cn.hutool.json.JSONUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import springfox.documentation.service.ParameterType;

import javax.wsdl.*;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jvs
 */
@Slf4j
@UtilityClass
public class WebServiceUtils {

    /**
     * 获取所有的方法入参和出参
     *
     * @param wsdlUrl the wsdl url
     * @return 这个接口下所有的方法 list
     */
    public static Set<RuleExternalPo> parseWsdl(String wsdlUrl) {
        /**
         * 解析 url地址
         */
        Map<String, Object> stringObjectMap = XmlUtil.xmlToMap(HttpUtil.downloadString(wsdlUrl, Charset.defaultCharset()));

        List<Map<String, String>> o1 = new ArrayList<>();
        if(Optional.of(JSONUtil.isTypeJSONObject( stringObjectMap.get("wsdl:portType").toString())).orElse(false)){
            Map<String, Object> o = (Map<String, Object>) stringObjectMap.get("wsdl:portType");
            Object o2 = o.get("wsdl:operation");
            if(o2 instanceof Map){
                o1.add((Map)o2);
            }
            if(o2 instanceof Collection){
                o1.addAll((List) o2);
            }
        }else if(Optional.of(JSONUtil.isTypeJSONArray( stringObjectMap.get("wsdl:portType").toString())).orElse(false)){
            List<Map<String, Object>> o = (List<Map<String, Object>>) stringObjectMap.get("wsdl:portType");
            Map<String, Object> objectMap = o.get(0);
            Object o2 = objectMap.get("wsdl:operation");
            if(o2 instanceof Map){
                o1.add((Map)o2);
            }
            if(o2 instanceof Collection){
                o1.addAll((List) o2);
            }
        }

        List<String> collect1 = o1.stream().map(e -> e.get("wsdl:documentation")).collect(Collectors.toList());

        int i = 0;
        Set<RuleExternalPo> list = new HashSet<>();
        try {
            // 创建WSDLReader实例
            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLReader reader = factory.newWSDLReader();

            // 读取并解析WSDL文件
            Definition definition = reader.readWSDL(wsdlUrl);
            Map<?, ?> portTypesa = definition.getAllPortTypes();
            // 打印所有接口名称
            for (Object key : portTypesa.keySet()) {
                PortType portType = (PortType) portTypesa.get(key);
                log.info("Interface Name: " + portType.getQName().getLocalPart());
            }

            // 打印types部分
            Types types = definition.getTypes();
            if (types != null) {
                for (Object ext : types.getExtensibilityElements()) {
                    if (ext instanceof Schema) {
                        Schema schema = (Schema) ext;
                        Element element = schema.getElement();
                        printSchemaElements(element);
                    }
                }
            } else {
                log.info("No types section found in the WSDL.");
            }
            // 获取并打印接口信息
            Map<QName, PortType> portTypes = definition.getAllPortTypes();
            PortType portType = portTypes.values().stream().limit(1).collect(Collectors.toList()).get(0);
            log.info("PortType: " + portType.getQName());
            List<Operation> operations = portType.getOperations();
            for (Operation operation : operations) {
                log.info("  Operation: " + operation.getName());
                RuleExternalPo ruleExternalPo = new RuleExternalPo();
                ruleExternalPo.setUrl(wsdlUrl);
                list.add(ruleExternalPo);
                ruleExternalPo.setName(operation.getName()).setExplainInfo(collect1.get(i++)).setStatus(false).setMethod(Method.POST).setServerUrl(operation.getInput().getMessage().getQName().getNamespaceURI()).setType(
                        "webservice").setIcon(
                        "icon-jilianxuanze1").setUrl(operation.getName());

                // 获取输入参数
                Input input = operation.getInput();
                if (input != null) {
                    Message inputMessage = input.getMessage();
                    if (inputMessage != null) {
                        log.info("    Input Message: " + inputMessage.getQName());
                        List<ParameterMap> collect = (List<ParameterMap>) inputMessage.getParts().keySet()
                                .stream()
                                .map(a -> new ParameterMap().setParamType(ParameterType.BODY).setKey(StrUtil.toString(a)).setInputType("string"))
                                .collect(Collectors.toList());
                        ruleExternalPo.setFieldList(collect);
                    }
                }

                // 获取输出参数
                Output output = operation.getOutput();
                if (output != null) {
                    Message outputMessage = output.getMessage();
                    if (outputMessage != null) {
                        List<ParameterMap> collect = (List<ParameterMap>) outputMessage.getParts()
                                .keySet()
                                .stream()
                                .map(a -> new ParameterMap().setKey(StrUtil.toString(a)).setInputType("string"))
                                .collect(Collectors.toList());
                        ruleExternalPo.setResponseList(collect);
                    }
                }
            }

        } catch (Exception e) {
        }
        return list;

    }

    private static void printSchemaElements(Element element) {
        if (element != null) {
            NodeList elementsByTagName = element.getElementsByTagName("wsdl:documentation");
            for (int v = 0; v < elementsByTagName.getLength(); v++) {
                log.info("Documentation: " + elementsByTagName.item(v).getTextContent());
            }
            NodeList children = element.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element childElement = (Element) node;
                    NodeList docNodes = childElement.getElementsByTagName("wsdl:documentation");
                    for (int v = 0; v < docNodes.getLength(); v++) {
                        log.info("Documentation: " + docNodes.item(v).getTextContent());
                    }
                    String name = childElement.getAttribute("name");
                    log.info("type: " + name);
                    // 如果需要进一步处理嵌套元素，可以递归调用
//                    printSchemaElements(childElement);
                }
            }
        }
    }

    /**
     * 执行方法调用
     *
     * @param wsdlUrl      url地址
     * @param namespaceURI the namespace uri  命名空间
     * @param methodName   the method name  方法名
     * @param bodyMap      the body map   请求参数信息
     * @return object
     */
    public static Object execute(String wsdlUrl, String namespaceURI, String methodName, Map<String, Object> bodyMap, Map<String, String> hashMap) {
        SoapClient soapClient = SoapClient.create(wsdlUrl, SoapProtocol.SOAP_1_1, namespaceURI)
                .setMethod(methodName, namespaceURI)
                .charset(Charset.defaultCharset())
                .timeout(10000);
        if (ObjectNull.isNotNull(hashMap)) {
            soapClient.addHeaders(hashMap);
        }
        if (ObjectNull.isNotNull(bodyMap)) {
            soapClient.setParams(bodyMap);
        }
        log.info("发起 webservice 请求" + soapClient.getMsgStr(true));
        String send = null;
        try {
            send = soapClient.send(true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("webservice请求发送异常");
        }
        Map<String, Object> resultMap = XmlUtil.xmlToMap(send);
        log.info("webservice 响应：{}",resultMap);

        if(resultMap==null){
            throw new BusinessException("响应结果解析失败");
        }

        Object value = CollectionUtil.getFirst(resultMap.entrySet()).getValue();

        Map<String, Object> resultBody = BeanUtil.beanToMap(value);
        String responseKey = String.format("%sResponse", methodName);
        if(resultBody.containsKey(responseKey)){
            Object o = resultBody.get(responseKey);
            Map<String, Object> response = BeanUtil.beanToMap(o);
            String resultKey = String.format("%sResult", methodName);
            if(response.containsKey(resultKey)) {
                return response.get(resultKey);
            }
        }
        return value;
    }


}
