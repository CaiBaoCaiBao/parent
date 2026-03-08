package file.service;

import common.utils.Result;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    Result<?> uploadImg(MultipartFile file);
}
