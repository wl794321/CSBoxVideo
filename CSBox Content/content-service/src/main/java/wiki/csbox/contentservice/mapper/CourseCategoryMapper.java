package wiki.csbox.contentservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import wiki.csbox.contentmodel.model.dto.CourseCategoryTreeDto;
import wiki.csbox.contentmodel.model.po.CourseCategory;

import java.util.List;


/**
 * <p>
 * 课程分类 Mapper 接口
 * </p>
 *
 * @author Krian
 */
@Repository
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {

    /**
     * 使用MySQL8递归SQL，查询课程类型分类数据：
     *
     * @param id 树形结构节点Id
     * @return List<CourseCategoryTreeDto> 树形结构信息
     */
    List<CourseCategoryTreeDto> selectTreeNode(String id);

}
