package wiki.csbox.contentservice.service;

import org.springframework.stereotype.Service;
import wiki.csbox.contentmodel.model.dto.QueryCourseParamsDto;
import wiki.csbox.contentmodel.model.po.CourseBase;
import wiki.csbox.csboxbase.model.PageParams;
import wiki.csbox.csboxbase.model.PageResult;

import javax.validation.Valid;

/**
 * @author Krian
 * @version 1.0
 * @description: TODO 课程信息管理接口
 * @date 2023/3/17 0017 17:55
 */
public interface CourseBaseInfoService {

    /**
     * 分页查询接口
     *
     * @param pageParams 分页参数
     * @param queryCourseParamsDto 查询条件
     * @return PageResult<CourseBase> 分页查询结果
     */
    PageResult<CourseBase> queryCourseBaseList(@Valid PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);
}
