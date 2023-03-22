package wiki.csbox;

import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author Krian
 * @version 1.0
 * @description: TODO Minio 分布式文件系统
 * @date 2023/3/21 0021 12:30
 */
public class TestMinio {

    // 创建Minio客户端：
    MinioClient minioClient = MinioClient.builder()
            .endpoint("http://www.csbox.wiki:9000")
            .credentials("ROOTNAME", "CHANGEME123")
            .build();

    // 测试文件上传：
    @Test
    public void testUpload() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.uploadObject(
                // 构造文件上传参数信息：
                UploadObjectArgs.builder()
                        .bucket("test")
                        .object("test.zip")
                        .filename("E:\\BaiduNetdiskDownload\\学成在线项目—资料\\day05 媒资管理 Nacos Gateway MinIO\\资料\\nacos配置\\nacos_config_export.zip")
                        .build()
        );
    }
}
