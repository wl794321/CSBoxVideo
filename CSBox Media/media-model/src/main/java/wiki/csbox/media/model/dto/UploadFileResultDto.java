package wiki.csbox.media.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import wiki.csbox.media.model.po.MediaFiles;

/**
 * @author Krian
 * @version 1.0
 * @description: TODO 文件信息响应类
 * @date 2023/3/21 0021 14:14
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class UploadFileResultDto extends MediaFiles {

}
