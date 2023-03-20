package wiki.csbox.csboxbase.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author Krian
 * @version 1.0
 * @description: TODO 分页查询返回结果数据封装类
 * @date 2023/3/17 0017 14:36
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class PageResult<T> implements Serializable {
    // 数据列表：
    private List<T> items;

    //当前页码
    private long pageNo;

    // 每页记录数：
    private long pageSize;

    // 数据记录总数：
    private long counts;
}
