package com.dux.fastdfs.utils;

import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.UUID;

public class FileIOUtil {

    private static final String TEST_PATH = "E:\\";

    private static final String PATH = "/home/www/temporary/";

    private static final String TEST_ICO = "E:\\Payload\\AIOS.app\\AppIcon20x20@3x.png";

    private static final String ICO_PATH = "/home/www/temporary/AppIcon20x20@3x.png";

    private static final String TEST_BIG_ICO = "E:\\Payload\\AIOS.app\\AppIcon60x60@3x.png";

    private static final String BIG_ICO_PATH = "/home/www/temporary/AppIcon60x60@3x.png";


    public static void transfer(File file, String appPath, String version) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        //小图片上传
        if (file != null && file.getTotalSpace() > 0) {
            try {
                //小图标路径
                fis = new FileInputStream(file);
                File dir = new File(PATH + appPath);
                if(!dir.exists()){
                    dir.mkdirs();
                }

                File versionDir = new File(PATH + appPath + "/" + version);
                if(!versionDir.exists()){
                    versionDir.mkdirs();
                }
                String filePath = PATH + appPath + "/" + version + "/" + file.getName();

                fos = new FileOutputStream(filePath);

                // 定义一个字节数组  存到数组b中
                byte[] b = new byte[1024 * 10];
                // 起始长度为0
                int len = 0;
                while ((len = fis.read(b)) != -1) {
                    fos.write(b, 0, len);
                    fos.flush();  //  文件刷新
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (fis != null) {
                            try {
                                fis.close();
                            } catch (IOException e) {
                                throw new RuntimeException("文件复制失败");
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    public void test1(){
        File file = new File("C:\\Users\\Administrator\\Desktop\\new.plist");
        transfer(file, "ios", "1.1.1");
    }
}
