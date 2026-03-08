package file.service.impl;

import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import common.exception.BusinessException;
import common.utils.Result;
import common.utils.ULIDUtils;
import file.config.OSSConfig;
import file.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class FileServiceImpl implements FileService {
    @Autowired
    OSSConfig ossConfig;

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private static final long MAX_SIZE = 1024 * 1024 * 5;

    @Override
    public Result<?> uploadImg(MultipartFile file) {
        // 校验文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        // 校验文件大小
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("文件大小不能超过5MB");
        }
        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException("仅支持jpg、jpeg、png、gif、webp格式的图片");
        }
        // 4. 获取原始文件名和扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 5. 生成唯一文件名（ULID + 扩展名）
        String key = "images/" + ULIDUtils.generateULID() + extension;

        try {
            // 6. 获取上传凭证
            Auth auth = ossConfig.auth();
            String uploadToken = auth.uploadToken(ossConfig.getBucketName());

            // 7. 上传到七牛云
            Configuration configuration = new Configuration(Region.region2());
            UploadManager uploadManager = new com.qiniu.storage.UploadManager(configuration);
            byte[] bytes = file.getBytes();
            Response response = uploadManager.put(bytes, key, uploadToken);

            if (response.isOK()) {
                // 8. 返回文件访问URL
                String fileUrl = ossConfig.getDomain() + "/" + key;
                log.info("文件上传成功: {}", fileUrl);
                return Result.success("上传成功", fileUrl);
            } else {
                log.error("七牛云上传失败: {}", response.error);
                throw new BusinessException("文件上传失败");
            }
        } catch (IOException e) {
            log.error("文件读取失败", e);
            throw new BusinessException("文件读取失败");
        }
    }
}
