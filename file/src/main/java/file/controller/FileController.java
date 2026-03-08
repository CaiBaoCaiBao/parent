package file.controller;

import common.utils.Result;
import file.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
public class FileController {
    @Autowired
    FileService fileService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/trip-api/upload-img")
    @Description(value = "上传图片")
    public Result<?> uploadImg(@RequestParam("file") MultipartFile file){
        return fileService.uploadImg(file);
    }
}
