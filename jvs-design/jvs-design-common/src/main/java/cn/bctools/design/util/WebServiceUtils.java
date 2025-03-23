package cn.bctools.design.util;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.rule.entity.ParameterMap;
import cn.bctools.design.rule.entity.RuleExternalPo;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.http.webservice.SoapClient;
import cn.hutool.http.webservice.SoapProtocol;
import com.jayway.jsonpath.JsonPath;
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
    static final String DOCUMENTATION = "wsdl:documentation";

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
        List<Map<String, Object>> o = (List<Map<String, Object>>) stringObjectMap.get("wsdl:portType");
        List<Map<String, String>> o1 = (List) o.get(0).get("wsdl:operation");
        List<String> collect1 = o1.stream().map(e -> e.get(DOCUMENTATION)).collect(Collectors.toList());

        int i = 0;
        Set<RuleExternalPo> list = new HashSet<>();
        try {
            // 创建WSDLReader实例
            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLReader reader = factory.newWSDLReader();

            // 读取并解析WSDL文件
            Definition definition = reader.readWSDL(wsdlUrl);
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

        } catch (Exception ignored) {
        }
        return list;

    }

    private static void printSchemaElements(Element element) {
        if (element != null) {
            NodeList elementsByTagName = element.getElementsByTagName(DOCUMENTATION);
            for (int v = 0; v < elementsByTagName.getLength(); v++) {
                log.info("Documentation: " + elementsByTagName.item(v).getTextContent());
            }
            NodeList children = element.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element childElement = (Element) node;
                    NodeList docNodes = childElement.getElementsByTagName(DOCUMENTATION);
                    for (int v = 0; v < docNodes.getLength(); v++) {
                        log.info("Documentation: " + docNodes.item(v).getTextContent());
                    }
                    String name = childElement.getAttribute("name");
                    log.info("type: " + name);
                }
            }
        }
    }

    /**
     * 执行方法调用
     *
     * @param wsdlUrl      url地址
     * @param namespaceUrl the namespace uri  命名空间
     * @param methodName   the method name  方法名
     * @param bodyMap      the body map   请求参数信息
     * @return object
     */
    public static Object execute(String wsdlUrl, String namespaceUrl, String methodName, Map<String, Object> bodyMap, Map<String, String> hashMap) {
        SoapClient soapClient = SoapClient.create(wsdlUrl, SoapProtocol.SOAP_1_1, namespaceUrl)
                .setMethod(methodName, namespaceUrl)
                .charset(Charset.defaultCharset())
                .timeout(10000);
        if (ObjectNull.isNotNull(hashMap)) {
            soapClient.addHeaders(hashMap);
        }
        if (ObjectNull.isNotNull(bodyMap)) {
            soapClient.setParams(bodyMap);
        }
        log.info("发起 webservice 请求" + soapClient.getMsgStr(true));
        String send = soapClient.send(true);
        Map resultMap = XmlUtil.xmlToMap(send);
        Object read = Optional.ofNullable(resultMap).map(e ->
        {
            try {
                return JsonPath.read(e, "soap:Body." + String.format("%sResponse.%sResult", methodName, methodName));
            } catch (Exception ex) {
                return null;
            }
        }).orElseGet(() -> {
            return Optional.ofNullable(JsonPath.read(resultMap, "soap:Body.soap:Fault")).orElse("").toString();
        });
        return read;
    }

}
