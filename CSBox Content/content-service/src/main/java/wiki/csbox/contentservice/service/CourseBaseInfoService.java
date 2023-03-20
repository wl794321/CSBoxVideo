package wiki.csbox.contentservice.service;

import wiki.csbox.contentmodel.model.dto.AddCourseDto;
import wiki.csbox.contentmodel.model.dto.CourseBaseInfoDto;
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
     * 分页查询课程信息
     *
     * @param pageParams           分页参数
     * @param queryCourseParamsDto 查询条件
     * @return PageResult<CourseBase> 分页查询结果
     */
    PageResult<CourseBase> queryCourseBaseList(@Valid PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    /**
     * 新增课程信息
     *
     * @param companyId    机构id
     * @param addCourseDto 新增课程信息
     * @return CourseBaseInfoDto 回传前端课程信息
     */
    CourseBaseInfoDto createCourseBase(Long companyId, @Valid AddCourseDto addCourseDto);
}
