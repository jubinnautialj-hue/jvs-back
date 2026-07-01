package cn.bctools.rule.entity.enums.type;

/**
 * @author zxk
 * 逻辑处理jpg\png\pdf\docx  等文件格式时返回给前端时需要前端给对应的反馈信息，如打开文件预览，或直接下载文件。
 * 文件渲染格式
 */
public enum OutputType {
    /**
     * 预览
     */
    preview("预览"),
    /**
     * 下载
     */
    download("下载");

    public String msg;

    OutputType(String msg) {
        this.msg = msg;
    }
}
