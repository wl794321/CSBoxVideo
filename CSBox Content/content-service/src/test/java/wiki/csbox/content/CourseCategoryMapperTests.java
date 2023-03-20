package wiki.csbox.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import wiki.csbox.contentmodel.model.dto.CourseCategoryTreeDto;
import wiki.csbox.contentservice.mapper.CourseCategoryMapper;

import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @description: TODO 测试类
 * @date 2023/3/18 0018 11:46
 */
@SpringBootTest
public class CourseCategoryMapperTests {

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    /**
     * 递归查询树形结构单元测试
     */
    @Test
    public void treeNodeCourseCategoryMapper() {
        List<CourseCategoryTreeDto> courseCategoryTreeNodes = courseCategoryMapper.selectTreeNode("1");
        System.out.printf(String.valueOf(courseCategoryTreeNodes));
    }

}
