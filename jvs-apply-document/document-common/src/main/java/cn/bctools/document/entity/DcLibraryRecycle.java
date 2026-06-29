package cn.bctools.document.entity;

import cn.bctools.database.entity.po.BasalPo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author auto
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("回收站")
@EqualsAndHashCode(callSuper = false)
@TableName(value = "dc_recycle", autoResultMap = true)
public class DcLibraryRecycle extends BasalPo implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("文件id")
    private String dcId;
    @ApiModelProperty("知识库id")
    private String knowledgeId;
    @ApiModelProperty("刪除时的所有文件id 包含本身")
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private List<String> dcIdList;
}
