package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/**
 * @author Chandler
 * @version 2021.2
 * @date 2023/1/15 23:19
 *文件的上传和下载
 */
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {
    //动态缓存下载的路径，路径具体配置在配置文件中
    @Value("${reggie.path}")
    private String basePath;
    /**
     * 下载文件
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        //file是临时文件，需要转存到其他位置，否则请求完成结束之后这个文件就消失了
        //MultipartFile是spring-web中自带的用来上传和下载文件的类
        //这个参数变量要和前端规定的name属性的值一样，这里面的名字是file
        log.info(file.toString());
        //获取原始文件名
        String originalFilename = file.getOriginalFilename();//abc.jpg
        //通过对原始文件名的截取，获取到后面的图片格式内容
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        //使用UUID重新随机生成文件名，防止文件名重复造成的文件覆盖
        String filename = UUID.randomUUID().toString() + suffix;//dadda.jpg
        File dir=new File(basePath);
        //判断目录是否存在
        if(!dir.exists()){
            //如果目录不存在，创建目录
            dir.mkdirs();
        }
        try {
            //将临时文件转存到指定位置
            file.transferTo(new File(basePath+filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(filename);
    }

    /**
     * 文件下载
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){

        try {
            //输入流，通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath+name));
            //输出流，通过输出流将图片会写到浏览器，在浏览器上展示图片
            ServletOutputStream outputStream = response.getOutputStream();

            //设置一下响应的内容是什么格式
            response.setContentType("image/jpg");
            //定义一个字节数组，边读边写
            int len=0;
            byte[] bytes=new byte[1024];
            while ((len=fileInputStream.read(bytes))!=-1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }
            //关闭资源
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
