package wiki.csbox.media.service;

import wiki.csbox.csboxbase.model.PageParams;
import wiki.csbox.csboxbase.model.PageResult;
import wiki.csbox.media.model.dto.QueryMediaParamsDto;
import wiki.csbox.media.model.dto.UploadFileParamsDto;
import wiki.csbox.media.model.dto.UploadFileResultDto;
import wiki.csbox.media.model.po.MediaFiles;

/**
 * @author Krian
 * @version 1.0
 * @description 媒资文件管理业务类
 * @date 2022/9/10 8:55
 */
public interface MediaFileService {

    /**
     * 查询文件信息列表
     *
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @return wiki.csbox.base.model.PageResult<wiki.csbox.media.model.po.MediaFiles>
     */
    PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    /**
     * 上传文件
     *
     * @param companyId           企业Id
     * @param uploadFileParamsDto 上传文件信息
     * @param localFilePath       本地文件路径
     * @return 返回文件信息
     */
    UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath);

    /**
     * 向数据库中插入上传文件记录信息
     * （主要是为了解决@Transational注解事务失效问题）
     *
     * @param companyId           企业Id
     * @param fileMD5             文件MD5值
     * @param uploadFileParamsDto 文件信息
     * @param bucket_otherFiles   桶名
     * @param objectName          对象名
     * @return MediaFiles 媒体信息
     */
    MediaFiles insertMediaFilesToDb(Long companyId, String fileMD5, UploadFileParamsDto uploadFileParamsDto, String bucket_otherFiles, String objectName);
}
