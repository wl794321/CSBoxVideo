package wiki.csbox.contentservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import wiki.csbox.contentmodel.model.po.CourseTeacher;

/**
 * <p>
 * 课程-教师关系表 Mapper 接口
 * </p>
 *
 * @author Krian
 */
@Repository
public interface CourseTeacherMapper extends BaseMapper<CourseTeacher> {

}
