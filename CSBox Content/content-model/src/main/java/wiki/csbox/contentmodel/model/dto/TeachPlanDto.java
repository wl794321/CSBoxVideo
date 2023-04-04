package wiki.csbox.contentmodel.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import wiki.csbox.contentmodel.model.po.Teachplan;
import wiki.csbox.contentmodel.model.po.TeachplanMedia;

import java.util.List;

/**
 * @author Krian
 * @version 1.0
 * @description: TODO  课程计划信息
 * @date 2023/4/4 0004 21:40
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TeachPlanDto extends Teachplan {

    // 媒资管理信息：
    private TeachplanMedia teachplanMedia;

    // 小章节列表：
    private List<TeachPlanDto > teachPlanDtoNodes;
}
