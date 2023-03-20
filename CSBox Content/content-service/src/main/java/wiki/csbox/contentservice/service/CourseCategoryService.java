package wiki.csbox.contentservice.service;

import wiki.csbox.contentmodel.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * @author Krian
 * @version 1.0
 * @description: TODO 课程分类Service接口
 * @date 2023/3/18 0018 11:56
 */
public interface CourseCategoryService {

    /**
     * 分类树结构查询
     *
     * @param id 节点Id
     * @return List<CourseCategoryTreeDto> 节点树信息Id
     */
    List<CourseCategoryTreeDto> queryTreeNode(String id);
}
