package com.example.controller;

import com.example.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {


    @Value("${myPath.BasePath}")
    private String basePath;

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) throws IOException {

        String originalFilename = file.getOriginalFilename();

        File dir =  new File(basePath);
        if (!dir.exists()){
            dir.mkdirs();
        }
        String fileName = UUID.randomUUID() +originalFilename.substring(originalFilename.lastIndexOf("."));
        file.transferTo(new File(basePath+fileName));
        log.info("保存成功路径为：{}",basePath+fileName);
        return R.success(fileName);
    }

    @GetMapping("/download")
    public void  download(String name, HttpServletResponse response) throws IOException{
        log.info("获取路径为：{}",basePath+name);
        FileInputStream fileInputStream = new FileInputStream(basePath+name);
        ServletOutputStream outputStream = response.getOutputStream();

        response.setContentType("image/jpeg");

        int len = 0;
        byte[] bytes =new byte[1024];
        while ((len = fileInputStream.read(bytes))!=-1){
            outputStream.write(bytes,0,len);
            outputStream.flush();
        }

        outputStream.close();
        fileInputStream.close();

    }

}
