package wiki.csbox.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
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
import wiki.csbox.csboxbase.model.RestResponse;
import wiki.csbox.media.mapper.MediaFilesMapper;
import wiki.csbox.media.mapper.MediaProcessMapper;
import wiki.csbox.media.model.dto.QueryMediaParamsDto;
import wiki.csbox.media.model.dto.UploadFileParamsDto;
import wiki.csbox.media.model.dto.UploadFileResultDto;
import wiki.csbox.media.model.po.MediaFiles;
import wiki.csbox.media.model.po.MediaProcess;
import wiki.csbox.media.service.MediaFileService;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static wiki.csbox.csboxbase.constant.FileInfoConstant.*;

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
            addWaitingTask(mediaFiles);
        }
        return mediaFiles;
    }

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        // 查询数据库文件信息是否存在，文件的ID存放的是文件的MD5值：
        MediaFiles fileInfoFromDB = mediaFilesMapper.selectById(fileMd5);
        if (fileInfoFromDB != null) {
            // 文件信息存在，则文件上传过，从对象存储服务中查询文件：
            GetObjectArgs objectArgs = GetObjectArgs.builder()
                    .bucket(fileInfoFromDB.getBucket())
                    .object(fileInfoFromDB.getFilePath())
                    .build();
            // 执行查询，获取一个流对象：
            try {
                FilterInputStream inputStream = minioClient.getObject(objectArgs);
                // 文件已经存在在minio中：
                if (inputStream != null) {
                    return RestResponse.success(true).setMsg("文件已上传成功！");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 文件不存在minio中：
        return RestResponse.success(false);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        // 分块存储的路径是：md5前两位为目录，chunk：存储的分块
        // 获取分块所在目录路径：
        String filePath = getChunkFilePath(fileMd5);
        GetObjectArgs objectArgs = GetObjectArgs.builder()
                .bucket(bucket_videoFiles)
                .object(filePath + chunkIndex)
                .build();
        try {
            FilterInputStream inputStream = minioClient.getObject(objectArgs);
            if (inputStream != null) {
                return RestResponse.success(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResponse.success(false);
    }

    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, byte[] bytes) {
        // 生成分块文件在对象存储上的目录：
        String chunkFilePath = getChunkFilePath(fileMd5);
        String chunkPath = chunkFilePath + chunk;
        try {
            // 上传分块文件到对象存储服务器：
            uploadFileToMinio(bytes, bucket_videoFiles, chunkPath);
            return RestResponse.success(true);
        } catch (Exception e) {
            log.debug("上传分块文件失败：{}", e.getMessage());
            e.printStackTrace();
            return RestResponse.validfail(false, "上传分块失败");
        }
    }

    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {
        // 获取文件上传的文件类型：
        String mineType = getMimeType(null);
        // 获取上传文件对象名（分块路径）：
        String chunkFilePath = getChunkFilePath(fileMd5);
        // 将分块文件上传到对象存储服务器上：
        boolean b = uploadFileToMinio(localChunkFilePath, mineType, bucket_videoFiles, chunkFilePath + chunk);
        if (!b) {
            return RestResponse.validfail(false, "上传文件失败！");
        }
        // 分片上传成功：
        return RestResponse.success(true);
    }

    /*
    @Override
    public RestResponse mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        // 分块文件所在目录：
        String chunkFilePath = getChunkFilePath(fileMd5);
        // 获取到所有的分块文件，调用minio的SDK进行文件合并操作
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> ComposeSource
                        .builder()
                        .bucket(bucket_videoFiles)
                        .object(chunkFilePath + i)
                        .build())
                .collect(Collectors.toList());
        // 获取合并后的对象文件全限定名：
        String filename = uploadFileParamsDto.getFilename();
        String fileExt = filename.substring(filename.lastIndexOf("."));
        String objectName = getFilePathMd5(fileMd5, fileExt);
        // 封装合并信息：
        ComposeObjectArgs objectArgs = ComposeObjectArgs.builder()
                .bucket(bucket_videoFiles)
                // 合并后的文件在对象存储服务器上的地址：
                .object(objectName)
                .sources(sources)
                .build();
        // 执行合并：
        try {
            minioClient.composeObject(objectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("合并文件出错！bucket：{}，object{}，错误信息：{}", bucket_videoFiles, objectName, e.getMessage());
            return RestResponse.success(false, "合并文件异常！");
        }

        // 从minio下载文件：
        File fileFromMinIO = downloadFileFromMinIO(bucket_videoFiles, objectName);
        // 校验合并后的文件是否和源文件一致：
        // 计算合并和文件的MD5值：
        try (FileInputStream inputStream = new FileInputStream(fileFromMinIO)) {
            String md5HexFromMinio = DigestUtils.md5Hex(inputStream);
            // 比较两个MD5值是否相等：
            if (!fileMd5.equals(md5HexFromMinio)) {
                log.error("校验合并文件的MD5值与原文件MD5值不一致！原始文件MD5：{}，合并文件MD5值：{}", fileMd5, md5HexFromMinio);
                return RestResponse.validfail(false, "文件校验失败！");
            }
            // 文件大小：
            uploadFileParamsDto.setFileSize(fileFromMinIO.length());
        } catch (Exception e) {
            e.printStackTrace();
            return RestResponse.validfail(false, "文件校验失败！");
        }

        // 文件信息写入数据库：
        // 使用代理对象，防止事务失效
        MediaFiles mediaFiles = mediaFileServiceProxy.insertMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_videoFiles, objectName);
        if (mediaFiles == null) {
            return RestResponse.validfail(false, "文件信息写入数据库失败！");
        }

        // 清理分块文件
        cleanChunkFiles(chunkFilePath, chunkTotal);
        return RestResponse.success(true);
    }
     */

    @Override
    public RestResponse mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        // 检查分块信息然后下载分块：
        File[] chunkFiles = checkAndDownLoadChunkStatus(fileMd5, chunkTotal);
        // 获取合并后文件拓展名：
        String filename = uploadFileParamsDto.getFilename();
        // 扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        // 获取文件类型：
        String mimeType = getMimeType(extension);

        File tempMergeFile = null;
        try {
            try {
                // 创建一个临时文件作为合并文件
                tempMergeFile = File.createTempFile("'merge'", extension);
            } catch (IOException e) {
                CSBoxVideoException.cast("创建临时合并文件出错");
            }
            // 创建合并文件的流对象
            try (RandomAccessFile raf_write = new RandomAccessFile(tempMergeFile, "rw")) {
                byte[] b = new byte[1024];
                for (File file : chunkFiles) {
                    //读取分块文件的流对象
                    try (RandomAccessFile raf_read = new RandomAccessFile(file, "r");) {
                        int len = -1;
                        while ((len = raf_read.read(b)) != -1) {
                            //向合并文件写数据
                            raf_write.write(b, 0, len);
                        }
                    }
                }
            } catch (IOException e) {
                CSBoxVideoException.cast("合并文件过程出错");
            }

            //校验合并后的文件是否正确
            try {
                FileInputStream mergeFileStream = new FileInputStream(tempMergeFile);
                String mergeMd5Hex = DigestUtils.md5Hex(mergeFileStream);
                if (!fileMd5.equals(mergeMd5Hex)) {
                    log.debug("合并文件校验不通过,文件路径:{},原始文件md5:{}", tempMergeFile.getAbsolutePath(), fileMd5);
                    CSBoxVideoException.cast("合并文件校验不通过");
                }
            } catch (IOException e) {
                log.debug("合并文件校验出错,文件路径:{},原始文件md5:{}", tempMergeFile.getAbsolutePath(), fileMd5);
                CSBoxVideoException.cast("合并文件校验出错");
            }


            // 拿到合并文件在minio的存储路径
            String mergeFilePath = getFilePathMd5(fileMd5, extension);
            // 将合并后的文件上传到文件系统
            uploadFileToMinio(tempMergeFile.getAbsolutePath(), mimeType, bucket_videoFiles, mergeFilePath);

            //将文件信息入库保存
            uploadFileParamsDto.setFileSize(tempMergeFile.length());//合并文件的大小
            insertMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_videoFiles, mergeFilePath);

            return RestResponse.success(true);
        } finally {
            // 删除临时分块文件
            if (chunkFiles != null) {
                for (File chunkFile : chunkFiles) {
                    if (chunkFile.exists()) {
                        chunkFile.delete();
                    }
                }
            }
            // 删除合并的临时文件
            if (tempMergeFile != null) {
                tempMergeFile.delete();
            }
        }
    }

    /**
     * 清除分块文件
     *
     * @param chunkFileFolderPath 分块文件路径
     * @param chunkTotal          分块总数
     */
    private void cleanChunkFiles(String chunkFileFolderPath, int chunkTotal) {
        Iterable<DeleteObject> objects =
                Stream.iterate(0, i -> ++i)
                        .limit(chunkTotal)
                        .map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i))))
                        .collect(Collectors.toList());
        RemoveObjectsArgs objectsArgs = RemoveObjectsArgs.builder()
                .bucket(bucket_videoFiles)
                .objects(objects)
                .build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(objectsArgs);
        // 需要遍历文件才能完成删除：
        results.forEach(item -> {
            try {
                DeleteError deleteError = item.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 根据桶和文件路径从minio下载文件
     *
     * @param file       文件数据
     * @param bucket     桶名
     * @param objectName 对象名
     * @return File
     */
    public File downloadFileFromMinIO(File file, String bucket, String objectName) {

        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(bucket).object(objectName).build();
        try (
                InputStream inputStream = minioClient.getObject(getObjectArgs);
                FileOutputStream outputStream = new FileOutputStream(file);
        ) {
            IOUtils.copy(inputStream, outputStream);
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            CSBoxVideoException.cast("查询分块文件出错");
        }
        return null;
    }

    private File downloadFileFromMinIO(String bucket, String objectName) {
        // 创建一个临时文件：
        File minioFile = null;
        FileOutputStream fileOutputStream = null;
        try {
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            // 创建临时文件：
            minioFile = File.createTempFile("minio", ".merge");
            fileOutputStream = new FileOutputStream(minioFile);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
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
            // 封装文件上传对象信息：
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

    public void uploadFileToMinio(byte[] bytes, String bucket, String objectName) {
        // 设置媒体资源默认类型（默认未知二进制流）：
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        // 根据文件后缀，设置新的文件类型：
        if (objectName.indexOf(".") >= 0) {
            //取objectName中的扩展名
            String extension = objectName.substring(objectName.lastIndexOf("."));
            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
            if (extensionMatch != null) {
                contentType = extensionMatch.getMimeType();
            }
        }

        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    // InputStream stream, long objectSize 对象大小, long partSize 分片大小(-1表示5M,最大不要超过5T，最多10000)
                    .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
                    .contentType(contentType)
                    .build();
            //上传到minio
            minioClient.putObject(putObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            log.debug("上传文件到文件系统出错:{}", e.getMessage());
            CSBoxVideoException.cast("上传文件到文件系统出错");
        }
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

    /**
     * 根据文件的MD5前两位值拼接获取文件在对象存储中的文件路径
     *
     * @param fileMd5 文件的MD5值
     * @return String 文件在对象存储上的路径
     */
    private String getChunkFilePath(String fileMd5) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/chunk/";
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

    /**
     * 添加待处理任务
     *
     * @param mediaFiles 媒体文件信息
     */
    private void addWaitingTask(MediaFiles mediaFiles) {
        // 获取文件的扩展名：
        String filename = mediaFiles.getFilename();
        String extension = filename.substring(filename.lastIndexOf('.'));
        String mimeType = getMimeType(extension);
        // 判断文件是否是AVI格式的视频：
        if (mimeType.equals(FILE_VIDEO_AVI)) {
            // 封装视频处理信息：
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles, mediaProcess);
            // 对视频文件状态设置一个状态（默认未处理）
            mediaProcess.setStatus(FILE_STATUS_USING);
            // 将文件记录写入到任务处理表（media_process）
            int flag = mediaProcessMapper.insert(mediaProcess);
        }
    }

    /**
     * 下载分块
     *
     * @param fileMd5    文件MD5值
     * @param chunkTotal 分块数量
     * @return java.io.File[] 分块文件数组
     */
    private File[] checkAndDownLoadChunkStatus(String fileMd5, int chunkTotal) {
        // 得到分块文件所在目录
        String chunkFileFolderPath = getChunkFilePath(fileMd5);
        // 分块文件数组
        File[] chunkFiles = new File[chunkTotal];
        // 开始下载
        for (int i = 0; i < chunkTotal; i++) {
            // 分块文件的路径
            String chunkFilePath = chunkFileFolderPath + i;
            // 分块文件
            File chunkFile = null;
            try {
                chunkFile = File.createTempFile("chunk", null);
            } catch (IOException e) {
                e.printStackTrace();
                CSBoxVideoException.cast("创建分块临时文件出错" + e.getMessage());
            }
            // 下载分块文件
            downloadFileFromMinIO(chunkFile, bucket_videoFiles, chunkFilePath);
            chunkFiles[i] = chunkFile;

        }
        return chunkFiles;
    }
}
