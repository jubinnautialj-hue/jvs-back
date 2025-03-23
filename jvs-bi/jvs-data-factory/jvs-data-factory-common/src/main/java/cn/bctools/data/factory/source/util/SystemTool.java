package cn.bctools.data.factory.source.util;

import lombok.extern.slf4j.Slf4j;

/**
 * @author admin
 */
@Slf4j
public class SystemTool {


    public static void removeAll() {
        SystemThreadLocal.removeAll();
    }

}
