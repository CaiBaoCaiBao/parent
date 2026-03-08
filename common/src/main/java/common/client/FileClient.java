package common.client;

import common.config.FeignConfig;
import common.constants.ClientInfo;
import common.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Description;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(
        contextId= ClientInfo.FileService.SERVICE_CONTEXT_ID,
        name= ClientInfo.FileService.SERVICE_NAME,
        configuration = FeignConfig.class
)
public interface FileClient {
    @PostMapping(
            value="/file/trip-api/upload-img",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Description(value = "上传图片")
    Result<?> uploadImg(@RequestPart("file") MultipartFile file);
}
