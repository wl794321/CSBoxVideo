package wiki.csbox.contentapi.api;

import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import wiki.csbox.contentmodel.model.dto.QueryCourseParamsDto;
import wiki.csbox.contentmodel.model.po.CourseBase;
import wiki.csbox.contentservice.service.CourseBaseInfoService;
import wiki.csbox.csboxbase.model.PageParams;
import wiki.csbox.csboxbase.model.PageResult;

import javax.validation.Valid;

/**
 * @author Krian
 * @version 1.0
 * @description: TODO 课程相关接口
 * @date 2023/3/17 0017 14:52
 */
@Api(value = "课程信息操作接口", tags = "课程信息")
@RestController
public class
CourseBaseInfoController {

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @ApiOperation(value = "获取课程信息列表", tags = "课程信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNumber", value = "当前记录页码"),
            @ApiImplicitParam(name = "pageSize", value = "每页显示记录数")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "获取列表成功！")
    })
    @PostMapping("/content/course/list")
    public PageResult<CourseBase> list(@Valid PageParams pageParams,
                                       @RequestBody QueryCourseParamsDto queryCourseParamsDto) {
        return courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParamsDto);
    }
}
