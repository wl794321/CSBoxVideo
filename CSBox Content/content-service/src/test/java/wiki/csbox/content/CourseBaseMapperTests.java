package wiki.csbox.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;
import wiki.csbox.contentmodel.model.dto.QueryCourseParamsDto;
import wiki.csbox.contentmodel.model.po.CourseBase;
import wiki.csbox.contentservice.mapper.CourseBaseMapper;
import wiki.csbox.csboxbase.model.PageResult;

import java.util.List;

/**
 * @author Krian
 * @version 1.0
 * @description: TODO 单元测试Mapper接口
 * @date 2023/3/17 0017 16:50
 */
@SpringBootTest
public class CourseBaseMapperTests {

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    /**
     * 分页查询测试单元
     */
    @Test
    public void testCourseBaseMapper() {
        // 封装查询条件：
        QueryCourseParamsDto queryCourseParamsDto = new QueryCourseParamsDto();
        queryCourseParamsDto.setCourseName("java");

        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        // like模糊查询（根据名称查询）：
        queryWrapper.like(!StringUtils.isEmpty(queryCourseParamsDto.getCourseName()),
                CourseBase::getName,
                queryCourseParamsDto.getCourseName());
        // eq精确匹配拆查询（根据状态查询）：
        queryWrapper.eq(!StringUtils.isEmpty(queryCourseParamsDto.getAuditStatus()),
                CourseBase::getAuditStatus,
                queryCourseParamsDto.getAuditStatus());

        // 创建page分页参数对象：（参数：当前页码，每页记录数）
        Page<CourseBase> page = new Page<>(1, 2);

        Page<CourseBase> courseBasePage = courseBaseMapper.selectPage(page, queryWrapper);

        PageResult<CourseBase> pageResult = new PageResult<>();
        List<CourseBase> records = courseBasePage.getRecords();

        // 封装返回数据：
        pageResult.setPageSize(1).setCounts(records.size()).setData(records);
    }

}
