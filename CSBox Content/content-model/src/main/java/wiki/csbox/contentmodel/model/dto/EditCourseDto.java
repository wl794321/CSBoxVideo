package wiki.csbox.contentmodel.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Krian
 * @version 1.0
 * @description: TODO 课程修改DTO
 * @date 2023/4/4 0004 20:55
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class EditCourseDto extends AddCourseDto {

    @ApiModelProperty(value = "课程ID", required = true)
    private Long id;
}
