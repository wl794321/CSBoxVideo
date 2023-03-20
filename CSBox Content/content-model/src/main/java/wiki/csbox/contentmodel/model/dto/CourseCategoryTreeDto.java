package wiki.csbox.contentmodel.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import wiki.csbox.contentmodel.model.po.CourseCategory;

import java.io.Serializable;
import java.util.List;

/**
 * @author Krian
 * @version 1.0
 * @description: TODO 视频类型树形结构模型类
 * @date 2023/3/18 0018 10:58
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {
    // 子节点数据信息：
    private List<CourseCategoryTreeDto> childrenTreeNodes;
}
