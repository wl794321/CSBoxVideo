package wiki.csbox.media.api;

import com.sun.org.apache.bcel.internal.generic.NEW;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wiki.csbox.csboxbase.model.PageParams;
import wiki.csbox.csboxbase.model.PageResult;
import wiki.csbox.media.model.dto.QueryMediaParamsDto;
import wiki.csbox.media.model.dto.UploadFileParamsDto;
import wiki.csbox.media.model.dto.UploadFileResultDto;
import wiki.csbox.media.model.po.MediaFiles;
import wiki.csbox.media.service.MediaFileService;

import java.io.File;
import java.io.IOException;

import static wiki.csbox.csboxbase.constant.FileInfoConstant.FILE_TYPE_IMAGE;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理接口
 * @date 2022/9/6 11:29
 */
@Api(value = "媒资文件管理接口", tags = "文件上传")
@RestController
@RequestMapping("/media")
public class MediaFilesController {

    @Autowired
    private MediaFileService mediaFileService;

    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto) {
        Long companyId = 1232141425L;
        return mediaFileService.queryMediaFiles(companyId, pageParams, queryMediaParamsDto);
    }

    @ApiOperation(value = "上传图片接口", tags = "文件上传")
    @PostMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile fileData) throws IOException {
        Long companyId = 1232141425L;
        // 封装数据：
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFilename(fileData.getOriginalFilename())
                .setFileSize(fileData.getSize())
                .setFileType(FILE_TYPE_IMAGE);
        // 创建一个临时文件：
        File tempFile = File.createTempFile("minio", ".temp");
        fileData.transferTo(tempFile);
        // 获取文件路径：
        String localFilePath = tempFile.getAbsolutePath();

        // 调用service上传图片
        return mediaFileService.uploadFile(companyId, uploadFileParamsDto, localFilePath);
    }
}
