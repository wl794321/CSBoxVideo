package wiki.csbox.media.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Krian
 * @version 1.0
 * @description: TODO 文件信息类
 * @date 2023/3/21 0021 14:22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UploadFileParamsDto {

    /**
     * 文件名称
     */
    private String filename;

    /**
     * 文件content-type
     */
    private String contentType;

    /**
     * 文件类型（文档，音频，视频）
     */
    private String fileType;
    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 标签
     */
    private String tags;

    /**
     * 上传人
     */
    private String username;

    /**
     * 备注
     */
    private String remark;
}