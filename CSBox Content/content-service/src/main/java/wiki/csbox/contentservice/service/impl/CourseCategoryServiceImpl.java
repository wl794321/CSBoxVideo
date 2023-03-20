package wiki.csbox.contentservice.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wiki.csbox.contentmodel.model.dto.CourseCategoryTreeDto;
import wiki.csbox.contentmodel.model.po.CourseCategory;
import wiki.csbox.contentservice.mapper.CourseCategoryMapper;
import wiki.csbox.contentservice.service.CourseCategoryService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Krian
 * @version 1.0
 * @description: TODO 课程分类信息
 * @date 2023/3/18 0018 11:58
 */
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNode(String id) {
        // 调用Mapper接口查询所有节点信息：
        List<CourseCategoryTreeDto> treeNodes = courseCategoryMapper.selectTreeNode(id);

        // 找到每个节点的子节点，最终封装成List<CourseCategoryTreeDto>
        // 先将list转换成map，key就是节点的id，value就是CourseCategoryTreeDto对象，目的是为了方便从map中获取结点：
        // filter(item -> !id.equals(item.getId()))：排除根节点
        Map<String, CourseCategoryTreeDto> mapTemp = treeNodes.stream().filter(item -> !id.equals(item.getId())).collect(Collectors.toMap(CourseCategory::getId, value -> value, (key1, key2) -> key2));
        // 定义一个list作为最终返回的list：
        ArrayList<CourseCategoryTreeDto> courseCategoryList = new ArrayList<>();
        // 从头遍历List<CourseCategoryTreeDto>，一边遍历一边找子节点放在父节点的childrenTreeNodes属性中：
        treeNodes.stream().filter(item -> !id.equals(item.getId())).forEach(item -> {
            if (item.getParentid().equals(id)) courseCategoryList.add(item);
            CourseCategoryTreeDto courseCategoryTreeDto = mapTemp.get(item.getParentid());
            if (courseCategoryTreeDto != null) {
                // 如果该父节点的childrenTreeNodes属性如果为空，就需要new一个集合，因为要向该集合中放入它的子节点：
                if (courseCategoryTreeDto.getChildrenTreeNodes() == null) {
                    courseCategoryTreeDto.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                }
                // 找到每个节点的子节点，然后放在父节点的childrenTreeNodes属性中：
                courseCategoryTreeDto.getChildrenTreeNodes().add(item);
            }
        });

        return courseCategoryList;
    }
}
