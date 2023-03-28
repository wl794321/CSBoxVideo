package wiki.csbox.media.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import wiki.csbox.csboxbase.model.RestResponse;
import wiki.csbox.media.model.dto.UploadFileParamsDto;
import wiki.csbox.media.service.MediaFileService;

import java.io.File;

import static wiki.csbox.csboxbase.constant.FileInfoConstant.FILE_TYPE_VIDEO;

/**
 * @author Krian
 * @version 1.0
 * @description: TODO 大文件上传
 * @date 2023/3/28 0028 13:07
 */
@Api(value = "大文件上传接口", tags = "大文件上传")
@RestController
@RequestMapping("/media")
public class BigFilesController {

    @Autowired
    private MediaFileService mediaFileService;

    @ApiOperation(value = "文件上传前检查文件")
    @PostMapping("/upload/checkfile")
    public RestResponse<Boolean> checkFile(
            @RequestParam("fileMd5") String fileMd5
    ) throws Exception {
        return mediaFileService.checkFile(fileMd5);
    }

    @ApiOperation(value = "分块文件上传前的检测")
    @PostMapping("/upload/checkchunk")
    public RestResponse<Boolean> checkChunk(@RequestParam("fileMd5") String fileMd5,
                                            @RequestParam("chunk") int chunk) throws Exception {
        return mediaFileService.checkChunk(fileMd5, chunk);
    }

    @ApiOperation(value = "上传分块文件")
    @PostMapping("/upload/uploadchunk")
    public RestResponse uploadChunk(@RequestParam("file") MultipartFile file,
                                    @RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("chunk") int chunk) throws Exception {
        // 创建一个临时文件：
        File tempFile = File.createTempFile("minio", ".temp");
        file.transferTo(tempFile);
        // 获取文件路径：
        String localFilePath = tempFile.getAbsolutePath();

        return mediaFileService.uploadChunk(fileMd5, chunk, localFilePath);

    }

    @ApiOperation(value = "合并文件")
    @PostMapping("/upload/mergechunks")
    public RestResponse mergeChunks(@RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("fileName") String fileName,
                                    @RequestParam("chunkTotal") int chunkTotal) throws Exception {
        Long companyId = 1232141425L;

        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFilename(fileName);
        uploadFileParamsDto.setFileType(FILE_TYPE_VIDEO);
        uploadFileParamsDto.setTags("课程文件");
        return mediaFileService.mergeChunks(companyId, fileMd5, chunkTotal, uploadFileParamsDto);
    }

}
