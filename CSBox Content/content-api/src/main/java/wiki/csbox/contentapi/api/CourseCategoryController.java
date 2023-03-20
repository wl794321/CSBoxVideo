package wiki.csbox.contentapi.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import wiki.csbox.contentmodel.model.dto.CourseCategoryTreeDto;
import wiki.csbox.contentservice.service.CourseCategoryService;

import java.util.List;

import static wiki.csbox.csboxbase.constant.CourseBaseInfoConstant.COURSE_CATEGORY_ROOT_NODE_ID;

/**
 * @author Krian
 * @version 1.0
 * @description: TODO 课程分类信息接口
 * @date 2023/3/18 0018 11:03
 */
@Api(value = "课程信息分类接口", tags = "课程分类信息")
@CrossOrigin
@RestController
public class CourseCategoryController {

    @Autowired
    private CourseCategoryService courseCategoryService;

    @ApiOperation(value = "获取课程分类信息", tags = "课程分类信息")
    @GetMapping("/content/course-category/tree-nodes")
    public List<CourseCategoryTreeDto> queryTreeNodes() {
        return courseCategoryService.queryTreeNode(COURSE_CATEGORY_ROOT_NODE_ID);
    }

}
