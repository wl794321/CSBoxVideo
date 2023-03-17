package wiki.csbox.content.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wiki.csbox.content.service.CoursePublishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * 课程发布 前端控制器
 * </p>
 *
 * @author Krian
 */
@Slf4j
@RestController
@RequestMapping("coursePublish")
public class CoursePublishController {

    @Autowired
    private CoursePublishService  coursePublishService;
}
