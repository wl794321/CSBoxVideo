package wiki.csbox.media.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import wiki.csbox.media.mapper.MediaProcessMapper;
import wiki.csbox.media.model.po.MediaProcess;
import wiki.csbox.media.service.MediaFileProcessService;

import java.util.List;

/**
 * @author Krian
 * @version 1.0
 * @description: TODO 视频解析任务
 * @date 2023/3/30 0030 9:03
 */
public class MediaFileProcessServiceImpl implements MediaFileProcessService {

    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Override
    public List<MediaProcess> getMediaProcessList(int shardTotal, int shardIndex, int count) {
        return mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
    }

    @Override
    public boolean startTask(long id) {
        return mediaProcessMapper.startTask(id) >= 0;
    }
}
