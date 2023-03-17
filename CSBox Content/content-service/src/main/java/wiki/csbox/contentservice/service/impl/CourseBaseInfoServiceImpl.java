package wiki.csbox.contentservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sun.org.apache.xpath.internal.operations.String;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wiki.csbox.contentmodel.model.dto.QueryCourseParamsDto;
import wiki.csbox.contentmodel.model.po.CourseBase;
import wiki.csbox.contentservice.mapper.CourseBaseMapper;
import wiki.csbox.contentservice.service.CourseBaseInfoService;
import wiki.csbox.csboxbase.model.PageParams;
import wiki.csbox.csboxbase.model.PageResult;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @description: TODO
 * @date 2023/3/17 0017 18:00
 */
@Slf4j
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        // 设置查询条件：
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        // 根据课程名称进行模糊查询：
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),
                CourseBase::getName
                , queryCourseParamsDto.getCourseName());
        // 根据课程审核状态查询：
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),
                CourseBase::getAuditStatus,
                queryCourseParamsDto.getAuditStatus());
        // 根据课程发布状态查询：
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus())
                , CourseBase::getStatus
                , queryCourseParamsDto.getPublishStatus());

        // 设置分页条件：
        Page<CourseBase> page = new Page<>(pageParams.getPageNumber(), pageParams.getPageSize());
        // 执行查询：
        Page<CourseBase> courseBasePage = courseBaseMapper.selectPage(page, queryWrapper);

        // 封装返回数据：
        // 获取所有记录：
        List<CourseBase> records = courseBasePage.getRecords();
        // 获取总记录数：
        long total = courseBasePage.getTotal();
        return new PageResult<CourseBase>(records, pageParams.getPageNumber(), pageParams.getPageSize(), total);
    }
}
