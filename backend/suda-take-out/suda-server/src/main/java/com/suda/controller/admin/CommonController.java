package com.suda.controller.admin;


import com.suda.result.Result;
import com.suda.utils.MinioUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

import static com.suda.constant.MessageConstant.UPLOAD_FAILED;

@RestController
@RequestMapping("/admin/common")
@Tag(name = "通用接口")
@Slf4j
public class CommonController {


    @Autowired
    private MinioUtil minioUtil;

    /**
     * 文件上传接口
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @Operation(summary = "文件上传")
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传：{}",file);

        try {
            String originalFilename = file.getOriginalFilename();
            //截取原始文件名的后缀
            String extention=originalFilename.substring(originalFilename.lastIndexOf("."));
            String objectName = UUID.randomUUID().toString() + extention;
            //使用UUID生成文件名，确保文件名唯一性
            String url=minioUtil.upload(file.getBytes(),objectName);
            return Result.success(url);
        } catch (IOException e) {
            log.error("文件上传失败",e);
        }
        return Result.error(UPLOAD_FAILED);
    }
}
