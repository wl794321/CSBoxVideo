package wiki.csbox.contentservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wiki.csbox.contentmodel.model.dto.AddCourseDto;
import wiki.csbox.contentmodel.model.dto.CourseBaseInfoDto;
import wiki.csbox.contentmodel.model.dto.EditCourseDto;
import wiki.csbox.contentmodel.model.dto.QueryCourseParamsDto;
import wiki.csbox.contentmodel.model.po.CourseBase;
import wiki.csbox.contentmodel.model.po.CourseCategory;
import wiki.csbox.contentmodel.model.po.CourseMarket;
import wiki.csbox.contentservice.mapper.CourseBaseMapper;
import wiki.csbox.contentservice.mapper.CourseCategoryMapper;
import wiki.csbox.contentservice.mapper.CourseMarketMapper;
import wiki.csbox.contentservice.service.CourseBaseInfoService;
import wiki.csbox.csboxbase.exception.CSBoxVideoException;
import wiki.csbox.csboxbase.model.PageParams;
import wiki.csbox.csboxbase.model.PageResult;

import java.time.LocalDateTime;
import java.util.List;

import static wiki.csbox.csboxbase.constant.CourseBaseInfoConstant.*;
import static wiki.csbox.csboxbase.constant.CommonError.OBJECT_NULL;

/**
 * @author Krian
 * @version 1.0
 * @description: TODO 课程基础信息业务类
 * @date 2023/3/17 0017 18:00
 */
@Slf4j
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

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
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 执行查询：
        Page<CourseBase> courseBasePage = courseBaseMapper.selectPage(page, queryWrapper);

        // 封装返回数据：
        // 获取所有记录：
        List<CourseBase> records = courseBasePage.getRecords();
        // 获取总记录数：
        long total = courseBasePage.getTotal();
        return new PageResult<>(records, pageParams.getPageNo(), pageParams.getPageSize(), total);
    }

    @Transactional  // 涉及到对数据库进行增删改的业务逻辑，service层方法需要使用@Transactional开启事务
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto) {
        // 参数校验：
        if (StringUtils.isBlank(addCourseDto.getName())) {
            // 抛出异常：
            // throw new RuntimeException("课程名称为空");
            CSBoxVideoException.cast(OBJECT_NULL.getErrMessage());
        }

        if (StringUtils.isBlank(addCourseDto.getMt())) {
            throw new RuntimeException("课程分类为空");
        }

        if (StringUtils.isBlank(addCourseDto.getSt())) {
            throw new RuntimeException("课程分类为空");
        }

        if (StringUtils.isBlank(addCourseDto.getGrade())) {
            throw new RuntimeException("课程等级为空");
        }

        if (StringUtils.isBlank(addCourseDto.getTeachmode())) {
            throw new RuntimeException("教育模式为空");
        }

        if (StringUtils.isBlank(addCourseDto.getUsers())) {
            throw new RuntimeException("适应人群为空");
        }

        if (StringUtils.isBlank(addCourseDto.getCharge())) {
            throw new RuntimeException("收费规则为空");
        }

        // 向课程基本信息表：course_base写入数据：
        CourseBase courseBaseNew = new CourseBase();
        // 从原始对象中获取属性值，拷贝到新的对象中，只要属性名一致就能实现拷贝：（新对象中的属性值会被覆盖）
        BeanUtils.copyProperties(addCourseDto, courseBaseNew);
        // 设置CompanyId：
        courseBaseNew.setCompanyId(companyId)
                .setCreateDate(LocalDateTime.now())
                // 设置审核状态默认为未提交：
                .setAuditStatus(AUDIT_STATUS_NOT_COMMITTED)
                // 设置发布状态为未发布：
                .setStatus(PUBLISHING_STATUS_NOT_PUBLISHED);
        // 执行插入数据库：
        int insertFlag = courseBaseMapper.insert(courseBaseNew);
        if (insertFlag <= 0) throw new RuntimeException("新增课程信息失败！");

        // 向课程营销信息表：course_market写入数据：
        CourseMarket courseMarketNew = new CourseMarket();
        BeanUtils.copyProperties(addCourseDto, courseMarketNew);
        // 获取课程主键信息Id：
        Long id = courseBaseNew.getId();
        courseMarketNew.setId(id);
        // 保存营销信息：
        saveCourseMarket(courseMarketNew);

        // 从数据中查询课程详细信息
        return getCourseBaseInfo(id);
    }

    /**
     * 向数据库插入课程营销消息数据（记录不存在则插入数据，记录存在则更新数据）
     *
     * @param courseMarket 课程经营信息
     * @return 数据库操作影响数据行数
     */
    private int saveCourseMarket(CourseMarket courseMarket) {
        String charge = courseMarket.getCharge();
        // 参数校验：
        if (StringUtils.isEmpty(charge)) {
            throw new RuntimeException("收费规则没有选择");
        }
        if (charge.equals(CHARGING_CATEGORY_CHARGED)) {
            if (courseMarket.getPrice() == null || courseMarket.getPrice().floatValue() <= 0) {
                throw new RuntimeException("课程为收费价格不能为空且必须大于0");
            }
        }
        // 从数据获取营销信息：
        CourseMarket courseMarketFromDB = courseMarketMapper.selectById(courseMarket.getId());
        if (courseMarketFromDB == null) {
            // 执行新增数据逻辑
            return courseMarketMapper.insert(courseMarket);
        } else {
            // 执行修改数据逻辑
            BeanUtils.copyProperties(courseMarket, courseMarketFromDB);
            courseMarketFromDB.setId(courseMarket.getId());
            return courseMarketMapper.updateById(courseMarketFromDB);
        }
    }

    /**
     * 查询课程信息
     *
     * @param courseId 课程信息Id
     * @return CourseBaseInfoDto 课程详细信息（课程基本信息 + 课程营销信息）
     */
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId) {
        // 从课程基本信息查询：
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            return null;
        }
        // 从课程营销信息查询：
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        // 组装CourseBaseInfoDto类信息：
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);

        // 根据分类Id，查询分类名称：
        CourseCategory mtCategory = courseCategoryMapper.selectById(courseBaseInfoDto.getMt());
        CourseCategory stCategory = courseCategoryMapper.selectById(courseBaseInfoDto.getSt());
        courseBaseInfoDto.setMtName(mtCategory.getName()).setStName(stCategory.getName());

        return courseBaseInfoDto;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto) {
        Long courseId = editCourseDto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            CSBoxVideoException.cast("课程信息不存在！");
        }
        // 数据合法性校验，当前机构只能修改当前机构的课程信息：
        if (!companyId.equals(courseBase.getCompanyId())) {
            CSBoxVideoException.cast("机构ID非法，不允许修改其他机构课程信息！");
        }
        // 跟新课程基本信息：
        BeanUtils.copyProperties(editCourseDto, courseBase);
        courseBase.setChangeDate(LocalDateTime.now());
        int b = courseBaseMapper.updateById(courseBase);
        if (b <= 0) {
            CSBoxVideoException.cast("更新课程信息失败!");
        }
        // 更新课程营销信息：
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(editCourseDto, courseMarket);
        int i = courseMarketMapper.updateById(courseMarket);
        if (i <= 0) {
            CSBoxVideoException.cast("更新课程信息失败!");
        }
        return getCourseBaseInfo(courseId);
    }
}
