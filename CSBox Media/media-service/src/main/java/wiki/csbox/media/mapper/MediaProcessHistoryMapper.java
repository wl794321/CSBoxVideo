package wiki.csbox.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import wiki.csbox.media.model.po.MediaProcessHistory;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itcast
 */
@Repository
public interface MediaProcessHistoryMapper extends BaseMapper<MediaProcessHistory> {

}
