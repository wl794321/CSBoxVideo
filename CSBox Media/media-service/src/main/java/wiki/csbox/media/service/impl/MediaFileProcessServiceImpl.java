package wiki.csbox.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wiki.csbox.media.mapper.MediaFilesMapper;
import wiki.csbox.media.mapper.MediaProcessHistoryMapper;
import wiki.csbox.media.mapper.MediaProcessMapper;
import wiki.csbox.media.model.po.MediaFiles;
import wiki.csbox.media.model.po.MediaProcess;
import wiki.csbox.media.model.po.MediaProcessHistory;
import wiki.csbox.media.service.MediaFileProcessService;

import java.time.LocalDateTime;
import java.util.List;

import static wiki.csbox.csboxbase.constant.FileInfoConstant.FILE_STATUS_PROCESS_FAIL;
import static wiki.csbox.csboxbase.constant.FileInfoConstant.FILE_STATUS_PROCESS_SUCCESS;

/**
 * @author Krian
 * @version 1.0
 * @description: TODO 视频解析任务
 * @date 2023/3/30 0030 9:03
 */
@Slf4j
@Service
public class MediaFileProcessServiceImpl implements MediaFileProcessService {

    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Override
    public List<MediaProcess> getMediaProcessList(int shardTotal, int shardIndex, int count) {
        return mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
    }

    @Override
    public boolean startTask(long id) {
        return mediaProcessMapper.startTask(id) >= 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        // 待更新任务：
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (mediaProcess == null) return;
        // 任务执行失败：
        if (status.equals(FILE_STATUS_PROCESS_FAIL)) {
            // 更新media_process表的状态：
            mediaProcess.setStatus(FILE_STATUS_PROCESS_FAIL)
                    .setFailCount(mediaProcess.getFailCount() + 1)
                    .setErrormsg(errorMsg);
            // mediaProcessMapper.updateById(mediaProcess);
            // 更高效的更新方式：
            UpdateWrapper<MediaProcess> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", mediaProcess.getId());
            mediaProcessMapper.update(mediaProcess, updateWrapper);
            return;
        }

        // 任务执行成功：
        // 查询待更新的文件记录：
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        // 更新media_file表记录中得URL字段：
        mediaFiles.setUrl(url);
        mediaFilesMapper.updateById(mediaFiles);
        // 更新media_process表的状态：
        mediaProcess.setStatus(FILE_STATUS_PROCESS_SUCCESS).setFinishDate(LocalDateTime.now()).setUrl(url);
        mediaProcessMapper.updateById(mediaProcess);

        // 将media_process表记录插入到media_process_history表：
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);

        // 从media_process删除当前任务：
        mediaProcessMapper.deleteById(mediaProcess);
    }
}
