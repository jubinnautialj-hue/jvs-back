package cn.bctools.report.model.univer;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author wl
 */
@Data
@Accessors(chain = true)
@ApiModel("univer 单元格对象")
public class UCell implements Serializable,Cloneable,Comparable<UCell> {

    private static final long serialVersionUID = -3088593392784210840L;

    @ApiModelProperty("单元格的原始值")
    private Object v;

    @ApiModelProperty(value = "单元格的样式 id 或者样式对象",notes = "UStyle")
    private Object s;

    @ApiModelProperty(value = "单元格的类型",notes = "1 表示字符串，2 表示数字，3 表示布尔值，4 表示强制文本",dataType = "UCellType")
    private Integer t;

    @ApiModelProperty("富文本，同时也是一个univer docs")
    private Object p;

    @ApiModelProperty("公式")
    private String f;

    @ApiModelProperty("公式 ID")
    private String si;

    @ApiModelProperty(value = "自定义字段",notes = "自定义字段。其中可以放入任何符合 JSON 格式的数据")
    private UCellExpand custom = new UCellExpand();

    @Override
    public UCell clone() {
        try {
            UCell clone = (UCell) super.clone();
            UCellExpand expand = clone.getCustom().clone();
            clone.setCustom(expand);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("克隆失败", e);
        }
    }

    public UCell clear(){
        this.custom = null;
        return this;
    }

    public void setR(int r){
        this.custom.setR(r);
    }

    public Integer getR(){
        if(this.custom==null){
            return null;
        }
        return this.custom.getR();
    }

    public void setC(int c){
        this.custom.setC(c);
    }

    public Integer getC(){
        if(this.custom==null){
            return null;
        }
        return this.custom.getC();
    }

    public Integer getOC(){
        if(this.custom==null){
            return null;
        }
        return this.custom.getOC();
    }


    public Integer getOR(){
        if(this.custom==null){
            return null;
        }
        return this.custom.getOR();
    }

    /**
     * 打标记
     */
    public void flag(){
        this.custom.setFlagged(Boolean.TRUE);
    }


    @Override
    public int compareTo(@NotNull UCell o) {
        if(Objects.equals(this.getR(), o.getR()) && Objects.equals(this.getC(), o.getC())){
            return 0;
        }
        if(Objects.equals(this.getR(), o.getR()) && this.getC()<o.getC()){
            return -1;
        }
        if(this.getR()<o.getR()){
            return -1;
        }
        return 1;
    }

    public int compareToOrigin(@NotNull UCell o) {
        if(Objects.equals(this.getCustom().getOR(), o.getCustom().getOR()) && Objects.equals(this.getCustom().getOC(), o.getCustom().getOC())){
            return 0;
        }
        if(Objects.equals(this.getCustom().getOR(), o.getCustom().getOR()) && this.getCustom().getOC()<o.getCustom().getOC()){
            return -1;
        }
        if(this.getCustom().getOR()<o.getCustom().getOR()){
            return -1;
        }
        return 1;
    }
}
