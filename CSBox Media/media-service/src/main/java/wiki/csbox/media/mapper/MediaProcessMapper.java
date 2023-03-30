package wiki.csbox.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import wiki.csbox.media.model.po.MediaProcess;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author itcast
 */
@Repository
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {

    /**
     * 获取待处理分片列表（使用注解方式实现Mapper接口）
     *
     * @param shardTotal 分片总数
     * @param shardIndex 分片索引
     * @param count      记录条数
     * @return List<MediaProcess>
     */
    @Select("select * from media_process t where t.id % #{shardTotal} = #{shardIndex} and (t.status = 1 or t.status = 3) and t.fail_count < 3 limit #{count}")
    List<MediaProcess> selectListByShardIndex(
            @Param("shardTotal") int shardTotal,
            @Param("shardIndex") int shardIndex,
            @Param("count") int count
    );

    /**
     * 开启一个任务（基于乐观锁）
     *
     * @param id 任务id
     * @return 更新记录数
     */
    @Update("update media_process m set m.status='4' where (m.status='1' or m.status='3') and m.fail_count<3 and m.id=#{id}")
    int startTask(@Param("id") long id);
}
