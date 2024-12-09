package cn.bctools.rule.dingding.attendance;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * The type Ding oapi attendance list request dto.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class DingOapiAttendanceListRequestDto {

    /**
     * The Users.
     */
    @ParameterValue(info = "用户对象", type = InputType.userList, explain = "获取多个用户,需要参数为用户id值")
    public List<String> users;

    /**
     * 查询考勤打卡记录的起始工作日
     */
    @ParameterValue(info = "查询考勤打卡记录的起始工作日。", explain = "查询考勤打卡记录的起始工作日", type = InputType.dateTime)
    public Date workDateFrom;

    /**
     * 查询考勤打卡记录的结束工作日
     */
    @ParameterValue(info = "查询考勤打卡记录的结束工作日。", explain = "查询考勤打卡记录的结束工作日", type = InputType.dateTime)
    public Date workDateTo;


}
