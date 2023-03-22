package wiki.csbox.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wiki.csbox.csboxbase.exception.CSBoxVideoException;
import wiki.csbox.csboxbase.model.PageParams;
import wiki.csbox.csboxbase.model.PageResult;
import wiki.csbox.media.mapper.MediaFilesMapper;
import wiki.csbox.media.mapper.MediaProcessMapper;
import wiki.csbox.media.model.dto.QueryMediaParamsDto;
import wiki.csbox.media.model.dto.UploadFileParamsDto;
import wiki.csbox.media.model.dto.UploadFileResultDto;
import wiki.csbox.media.model.po.MediaFiles;
import wiki.csbox.media.service.MediaFileService;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static wiki.csbox.csboxbase.constant.FileInfoConstant.FILE_AUDIT_STATUS_PASS;
import static wiki.csbox.csboxbase.constant.FileInfoConstant.FILE_STATUS_USING;

/**
 * @author Krian
 * @version 1.0
 * @description TODO 媒体文件服务
 * @date 2022/9/10 8:58
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Autowired
    private MinioClient minioClient;

    // 存储普通文件的bucket
    @Value("${minio.bucket.files}")
    private String bucket_otherFiles;

    // 存储视频文件的bucket
    @Value("${minio.bucket.videofiles}")
    private String bucket_videoFiles;

    // 自己注入自己：（Spring 三级缓存解决循环依赖问题）
    @Autowired
    private MediaFileService mediaFileServiceProxy;

    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {
        // 构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
        // 分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        return new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
    }

    // @Transactional 这个注解会在方法开始之前开启事务，方法执行结束之后提交事务
    // 由于这个方法中需要通过网络请求上传文件（调用了网络服务），由于网路服务时延可能较长，那么事务开启后对数据库的资源占用时间长，极端情况下导致数据库资源不够的情况。
    // @Transactional
    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {
        // 获取文件后缀名：
        String filename = uploadFileParamsDto.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        // 获取文件类型：
        String mimeType = getMimeType(extension);
        // 获取文件Bucket保存路径：
        String defaultFolderPath = getDefaultFolderPath();
        // 获取文件MD5值：
        String fileMD5 = getFileMD5(new File(localFilePath));
        // 获取Object对象存储全限定名：
        String objectName = defaultFolderPath + fileMD5 + extension;
        boolean flag = uploadFileToMinio(localFilePath, mimeType, bucket_otherFiles, objectName);
        if (!flag) {
            CSBoxVideoException.cast("上传文件失败！");
        }

        // 保存文件信息到数据库：
        // 使用自己类的代理对象调用事务方法解决事务失效问题：
        MediaFiles mediaFiles = mediaFileServiceProxy.insertMediaFilesToDb(companyId, fileMD5, uploadFileParamsDto, bucket_otherFiles, objectName);
        if (mediaFiles == null) {
            CSBoxVideoException.cast("文件上传信息保存失败！");
        }
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);

        return uploadFileResultDto;
    }

    /**
     * 将文件信息入库
     *
     * @param companyId           企业Id
     * @param fileId              文件Id
     * @param uploadFileParamsDto 上传文件信息
     * @param bucket              桶名
     * @param objectName          对象名
     * @return wiki.csbox.media.model.po.MediaFiles
     */
    @Transactional
    public MediaFiles insertMediaFilesToDb(Long companyId, String fileId, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {
        // 保存到数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();

            // 封装数据
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileId);
            mediaFiles.setFileId(fileId);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);

            // 获取扩展名
            String extension = null;
            String filename = uploadFileParamsDto.getFilename();
            if (StringUtils.isNotEmpty(filename) && filename.indexOf(".") >= 0) {
                extension = filename.substring(filename.lastIndexOf("."));
            }
            // 媒体类型
            String mimeType = getMimeType(extension);
            // 图片、mp4视频可以设置URL
            if (mimeType.indexOf("image") >= 0 || mimeType.indexOf("mp4") >= 0) {
                mediaFiles.setUrl("/" + bucket + "/" + objectName);
            }

            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setStatus(FILE_STATUS_USING);
            mediaFiles.setAuditStatus(FILE_AUDIT_STATUS_PASS);

            // 插入数据库文件表
            mediaFilesMapper.insert(mediaFiles);

            // 对avi视频添加到待处理任务表
//            if (mimeType.equals("video/x-msvideo")) {
//
//                MediaProcess mediaProcess = new MediaProcess();
//                BeanUtils.copyProperties(mediaFiles, mediaProcess);
//                //设置一个状态
//                mediaProcess.setStatus(FILE_STATUS_USING);//未处理
//                mediaProcessMapper.insert(mediaProcess);
//            }

        }
        return mediaFiles;
    }


    /**
     * 根据扩展名获取文件类型
     *
     * @param extension 文件扩展名
     * @return 文件类型
     */
    private String getMimeType(String extension) {
        if (extension == null) extension = "";
        // 根据扩展名获取mineType（文件类型）：
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    /**
     * 上传文件
     *
     * @param localFilePath 本地文件路径
     * @param mimeType      文件媒体类型
     * @param bucket        桶名
     * @param objectName    存储对象名
     * @return boolean 上传文件是否成功
     */
    public boolean uploadFileToMinio(String localFilePath, String mimeType, String bucket, String objectName) {
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder().
                    bucket(bucket)
                    .filename(localFilePath)
                    .object(objectName)
                    .contentType(mimeType)
                    .build();
            // 上传文件：
            minioClient.uploadObject(uploadObjectArgs);
            log.info("上传文件成功，bucket：{}，objectName：{}", bucket, objectName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件错误，bucket：{}，objectName：{}，错误信息：{}", bucket, objectName, e.getMessage());
        }
        return false;
    }

    /**
     * 根据日志获取默认的Bucket目录名
     *
     * @return Bucket目录名
     */
    private String getDefaultFolderPath() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(new Date()).replace("-", "/") + "/";
    }

    /**
     * 获取文件的MD5值
     *
     * @param file 文件
     * @return String 文件的MD5值
     */
    private String getFileMD5(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            return DigestUtils.md5Hex(fileInputStream);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取文件MD5值异常：{}", e.getMessage());
        }
        return null;
    }
}
