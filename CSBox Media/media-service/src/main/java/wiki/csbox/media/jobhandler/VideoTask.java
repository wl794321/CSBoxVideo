package wiki.csbox.media.jobhandler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import wiki.csbox.csboxbase.utils.Mp4VideoUtil;
import wiki.csbox.media.model.po.MediaProcess;
import wiki.csbox.media.service.MediaFileProcessService;
import wiki.csbox.media.service.MediaFileService;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static wiki.csbox.csboxbase.constant.FileInfoConstant.FILE_STATUS_PROCESS_FAIL;
import static wiki.csbox.csboxbase.constant.FileInfoConstant.FILE_STATUS_PROCESS_SUCCESS;

/**
 * @author Krian
 * @version 1.0
 * @description: TODO XXL_JOB 视频处理任务
 * @date 2023/4/4 0004 17:25
 */
@Slf4j
@Component
public class VideoTask {

    // ffmpeg 安装路径：
    @Value("${videoprocess.ffmpegpath}")
    private String ffmpeg_path;

    @Autowired
    private MediaFileProcessService mediaFileProcessService;

    @Autowired
    private MediaFileService mediaFileService;

    /**
     * 分片处理
     *
     * @throws Exception 异常
     */
    @XxlJob("videoJobHandler")
    public void videoHandler() throws Exception {

        // 设置分片参数：
        int shardIndex = XxlJobHelper.getShardIndex();  // 执行器编号，索引从0开始
        int shardTotal = XxlJobHelper.getShardTotal();  // 执行器总数

        // 确定CPU核心数量：
        int processors = Runtime.getRuntime().availableProcessors();
        // 查询数据库表，确定待处理的视频任务：
        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardTotal, shardIndex, processors);
        // 任务数量：
        int size = mediaProcessList.size();
        if (size <= 0) {
            log.info("获取视频处理任务数量：{}", size);
            return;
        }

        // 根据任务数量创建线程池中的线程数量：
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        // 使用计数器，防止方法结束立即返回，而线程还在运行任务：
        CountDownLatch countDownLatch = new CountDownLatch(size);

        mediaProcessList.forEach(mediaProcess -> {

            // 调用线程执行任务：
            executorService.execute(() -> {
                try {
                    // 获取任务ID：
                    Long taskId = mediaProcess.getId();
                    // 获取文件ID（文件的MD5值）：
                    String fileId = mediaProcess.getFileId();
                    // 获取文件的桶：
                    String bucket = mediaProcess.getBucket();
                    // 获取文件对象名：
                    String objectName = mediaProcess.getFilePath();
                    // 根据任务ID，开启线程执行任务：
                    boolean flag = mediaFileProcessService.startTask(taskId);
                    if (!flag) {
                        log.error("抢占任务失败，任务ID：{}", taskId);
                        return;
                    }
                    // 下载Minio的视频文件到本地：
                    File file = mediaFileService.downloadFileFromMinIO(bucket, objectName);
                    if (file == null) {
                        log.error("处理视频文件下载失败！任务id：{}，bucket：{}，objectName：{}", taskId, bucket, objectName);
                        // 保存任务处理失败结果：
                        mediaFileProcessService.saveProcessFinishStatus(taskId, FILE_STATUS_PROCESS_FAIL, fileId, null, "处理视频文件下载失败！");
                        return;
                    }
                    // 源avi视频的路径
                    String video_path = file.getAbsolutePath();
                    // 转换后mp4文件的名称
                    String mp4_name = fileId + ".mp4";
                    // 转换后mp4文件的路径
                    // 先创建一个临时文件，作为转换后的文件：
                    File mp4File = null;
                    try {
                        mp4File = File.createTempFile("minio", "mp4");
                    } catch (IOException e) {
                        log.error("创建临时文件异常：{}", e.getMessage());
                        mediaFileProcessService.saveProcessFinishStatus(taskId, FILE_STATUS_PROCESS_FAIL, fileId, null, "创建临时文件异常！");
                        e.printStackTrace();
                    }
                    String mp4_path = mp4File.getAbsolutePath();
                    // 创建工具类对象
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path, video_path, mp4_name, mp4_path);
                    // 开始视频转换，成功将返回success（视频转码操作）
                    String result = videoUtil.generateMp4();
                    // 文件转码处理失败：
                    if (!result.equals("success")) {
                        log.error("视频转码失败，{}，bucket：{}，objectName：{}", result, bucket, objectName);
                        mediaFileProcessService.saveProcessFinishStatus(taskId, FILE_STATUS_PROCESS_FAIL, fileId, null, "视频转码失败！");
                        return;
                    }
                    // 上传Minio：
                    boolean b = mediaFileService.uploadFileToMinio(mp4_path, "video/mp4", bucket, objectName);
                    if (!b) {
                        log.error("转码后视频上传Minio失败，taskId：{}", taskId);
                        mediaFileProcessService.saveProcessFinishStatus(taskId, FILE_STATUS_PROCESS_FAIL, fileId, null, "上传视频失败！");
                        return;
                    }
                    // mp4文件url：
                    String url = getFilePathMd5(fileId, ".mp4");

                    // 执行成功之后，保存任务处理结果：
                    mediaFileProcessService.saveProcessFinishStatus(taskId, FILE_STATUS_PROCESS_SUCCESS, fileId, url, null);

                } finally {
                    // 计数器减一操作，直到减到0：
                    countDownLatch.countDown();
                }
            });
        });

        // 线程阻塞，等待线程执行完成：
        // 指定最大限度等待时间，解除阻塞，防止线程因为异常被永久阻塞，无法完成后续调度任务
        countDownLatch.await(30, TimeUnit.MINUTES);
    }

    /**
     * 获取合并后文件对象存储文件路径
     *
     * @param fileMd5 文件的MD5值
     * @param fileExt 文件的后缀名
     * @return String对象存储文件路径
     */
    private String getFilePathMd5(String fileMd5, String fileExt) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }
}
