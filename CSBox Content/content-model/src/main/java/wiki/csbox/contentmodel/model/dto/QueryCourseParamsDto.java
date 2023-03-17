package wiki.csbox.contentmodel.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Krian
 * @version 1.0
 * @description: TODO 查询课程信息数据类
 * @date 2023/3/17 0017 14:32
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class QueryCourseParamsDto {
    // 课程审核状态：
    private String auditStatus;

    // 课程名称：
    private String courseName;

    // 课程发布状态：
    private String publishStatus;
}
