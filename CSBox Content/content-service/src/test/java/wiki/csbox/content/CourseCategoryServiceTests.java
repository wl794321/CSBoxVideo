package wiki.csbox.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import wiki.csbox.contentmodel.model.dto.CourseCategoryTreeDto;
import wiki.csbox.contentservice.service.CourseCategoryService;

import java.util.List;

/**
 * @author Krian
 * @version 1.0
 * @description: TODO CourseCategoryService 测试类
 * @date 2023/3/20 0020 9:11
 */
@SpringBootTest
public class CourseCategoryServiceTests {

    @Autowired
    private CourseCategoryService courseCategoryService;

    @Test
    public void testQueryTreeNode() {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryService.queryTreeNode("1");
        System.out.println(courseCategoryTreeDtos);
    }

}
