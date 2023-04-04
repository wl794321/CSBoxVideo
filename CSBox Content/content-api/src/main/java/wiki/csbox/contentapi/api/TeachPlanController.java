package wiki.csbox.contentapi.api;

import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wiki.csbox.contentmodel.model.dto.TeachPlanDto;

import java.util.List;

/**
 * @author Krian
 * @version 1.0
 * @description: TODO 课程计划接口
 * @date 2023/4/4 0004 21:43
 */
@Api(value = "课程计划接口", tags = "课程计划信息")
@RestController
@RequestMapping("/teachplan")
public class TeachPlanController {

    @ApiOperation(value = "查询课程计划树", tags = "课程计划信息")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "courseId", name = "课程ID", required = true, dataType = "Long", paramType = "path")
    })
    @GetMapping("/{courseId}/tree-nodes")
    public List<TeachPlanDto> getPlanNodes(@PathVariable Long courseId) {
        return null;
    }
}
