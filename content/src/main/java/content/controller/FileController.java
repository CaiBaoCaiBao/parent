package content.controller;

import common.client.FileClient;
import common.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/content-file")
public class FileController {
    @Autowired
    FileClient fileClient;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/upload-img")
    @Description(value = "上传图片")
    public Result<?> uploadImg(@RequestParam("file") MultipartFile file) {
        return fileClient.uploadImg(file);
    }
}
