package wiki.csbox.media.service;

import wiki.csbox.media.model.po.MediaProcess;

import java.util.List;

/**
 * @author Krian
 * @version 1.0
 * @description: TODO
 * @date 2023/3/30 0030 9:02
 */
public interface MediaFileProcessService {

    /**
     * 获取待处理任务
     *
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @param count      获取记录数
     * @return List<MediaProcess>
     */
    List<MediaProcess> getMediaProcessList(int shardTotal, int shardIndex, int count);

    /**
     * 开启一个任务
     *
     * @param id 任务ID
     * @return true：开启任务成功；false：开日任务失败
     */
    boolean startTask(long id);
}
