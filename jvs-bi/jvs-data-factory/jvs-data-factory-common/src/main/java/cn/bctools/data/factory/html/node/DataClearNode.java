package cn.bctools.data.factory.html.node;

import cn.bctools.data.factory.html.FData;
import cn.bctools.data.factory.html.node.params.DataClearParams;
import cn.bctools.data.factory.html.run.Frun;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author guojing
 */
@Service
public class DataClearNode implements Frun<DataClearParams> {
    @Override
    public FData run(Boolean formal, Map<String, FData> linkBody, DataClearParams dataClearParams) {
        return new FData();
    }
}
