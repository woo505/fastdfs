package com.dux.fastdfs.controller;

import com.alibaba.fastjson.JSONObject;
import com.dux.fastdfs.config.ApkInfo;
import com.dux.fastdfs.config.User;
import com.dux.fastdfs.entity.AppEntity;
import com.dux.fastdfs.service.AppServiceImpl;
import com.dux.fastdfs.sqlmapper.AppDownloadMapper;
import com.dux.fastdfs.utils.*;
import com.github.tobato.fastdfs.domain.MataData;
import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.mysql.cj.log.LogFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.activation.MimetypesFileTypeMap;
import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

import static com.dux.fastdfs.utils.IPAUtil.getIpaInfoMap;

@RestController
@Slf4j
public class FileController {

    Logger logger = LoggerFactory.getLogger(getClass());

    private static final String TEST_PATH = "E:\\";

    private static final String PATH = "/home/www/temporary/";

    private static final String PLIST_PREFIX = "https://file.bhshsk.top/";

    private static final String TEST_ICO = "E:\\Payload\\AIOS.app\\AppIcon20x20@3x.png";

    private static final String ICO_PATH = "/home/www/temporary/Payload/AIOS.app/AppIcon20x20@3x.png";

    private static final String TEST_BIG_ICO = "E:\\Payload\\AIOS.app\\AppIcon60x60@3x.png";

    private static final String BIG_ICO_PATH = "/home/www/temporary/Payload/AIOS.app/AppIcon60x60@3x.png";

    private static final String PREFIX = "https://file.bhshsk.top/";

    private static final String Android_PATH = "https://file.bhshsk.top/app-release.apk";

    private static final String TEST_Android_ICO = "E:\\res\\mipmap-xxxhdpi-v4\\ic_launcher.png";

    private static final String Android_ICO = "https://file.bhshsk.top/ic_launcher.png";

    private static final String IOS_ICO = "https://file.bhshsk.top/AppIcon60x60@3x.png";

    private static final String IOS_ICO_PREFIX = "/home/www/temporary/Payload/";

    @Resource
    private AppServiceImpl appServiceImpl;


    @GetMapping("/test1")
    public JSONObject test1(HttpSession httpSession){
        JSONObject jo = new JSONObject();
        httpSession.setAttribute("test", "test");
        jo.put("code", 200);
        return jo;
    }

    @GetMapping("test2")
    public JSONObject test2(HttpSession session){
        JSONObject jsonObject = new JSONObject();
        System.out.println(session.getAttribute("test"));
        jsonObject.put("code", 200);
        return jsonObject;
    }

    /**
     * 文件上传
     * 测试成功
     *
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping("/file/upload")
    public JSONObject upload(HttpSession session, @RequestParam(required = true, value = "file") MultipartFile file, @RequestParam(required = true, value = "phoneType") String phoneType, @RequestParam(required = true, value = "appType") String appType) {


        JSONObject jsonObject = new JSONObject();
        if(session.getAttribute("user") == null){
            jsonObject.put("code", 500);
            jsonObject.put("message", "没有登录");
            return jsonObject;
        }
        // 设置文件信息
        String cfBundleIdentifier = null;

        String cfBundleDisplayName = null;

        String cfBundleVersion = null;

        String iosIcoName = null;

        String iosBigIcoName = null;

        String appPath = null;

        String versionName = null;

        //如果是ipa的话，解压
        if(phoneType.equals("2")){
            if (file.getOriginalFilename().contains("ipa")) {
                try {
                    File file1 = MultipartFileToFile.multipartFileToFile(file);
                    //获取Ipa属性
                    Map<String, String> ipaInfoMap = getIpaInfoMap(file1);
                    if (ipaInfoMap != null && ipaInfoMap.size() > 0) {
                        for (Map.Entry<String, String> entry : ipaInfoMap.entrySet()) {
                            String key = entry.getKey();
                            if ("CFBundleIdentifier".equals(key)) {
                                cfBundleIdentifier = entry.getValue();
                            } else if ("CFBundleName".equals(key)) {
                                cfBundleDisplayName = entry.getValue();
                            } else if ("CFBundleShortVersionString".equals(key)) {
                                cfBundleVersion = entry.getValue();
                            } else if("IconPath".equals(key)){
                                iosIcoName = entry.getValue();
                            } else if("bigIconPath".equals(key)){
                                iosBigIcoName = entry.getValue();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("文件解压失败");
                    throw new RuntimeException(e);
                }

                appPath = IPAUtil.getAppPath();
                String iconName = IPAUtil.getIconName(iosIcoName, appPath);
                File ico = new File(IOS_ICO_PREFIX + appPath + "/" + iconName);
                if (ico != null && ico.getTotalSpace() > 0) {
                    FileIOUtil.transfer(ico, "ios", cfBundleVersion);
                }
                //获取大图片
                String bigIconName = IPAUtil.getIconName(iosBigIcoName, appPath);
                File bigIco = new File((IOS_ICO_PREFIX + appPath + "/" + bigIconName));
                if (bigIco != null && bigIco.getTotalSpace() > 0) {
                    //大图片上传
                    FileIOUtil.transfer(bigIco, "ios", cfBundleVersion);
                }


                //生成newplist文件
                String newPlistPath = PATH + "ios/" + cfBundleVersion + "/"+ "manifest.plist";
                IPAUtil.createPlist(cfBundleIdentifier, newPlistPath, cfBundleVersion, cfBundleDisplayName, PREFIX + "ios" + "/" + cfBundleVersion + "/" + iconName, PREFIX + "ios" + "/" + cfBundleVersion + "/" + bigIconName, PREFIX + "ios" + "/" + cfBundleVersion + "/" + file.getOriginalFilename());
                String newPlistUrl = PLIST_PREFIX  + "ios/" + cfBundleVersion + "/" + "manifest.plist";

                //删除解压文件
                IPAUtil.deleteDir(PATH + "Payload");

                //ipa信息入库
                AppEntity appEntity = new AppEntity();
                //如果是IOS
                appEntity.setPhoneType(2L);
                appEntity.setDownloadUrl(newPlistUrl);
                appEntity.setVersion(cfBundleVersion);
                appEntity.setAppType(appType);
                appEntity.setAppName(cfBundleDisplayName);
                appEntity.setIcoUrl(PLIST_PREFIX + "ios/" + cfBundleVersion + "/" + bigIconName);
                long fileSize = file.getSize();
                long fileSizeToM = fileSize / 1024 /1024;
                appEntity.setAppSize(fileSizeToM + "M");
                appServiceImpl.saveApp(appEntity);
            }

        }

        if(phoneType.equals("1")){

            try {
                File ipaFile = MultipartFileToFile.multipartFileToFile(file);
                FileIOUtil.transfer(ipaFile);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("文件转换失败");
            }

            System.out.println("<-------------- apk 文件处理 --------->");
            String andriodIco = null;
            AppEntity appEntity = new AppEntity();
            appEntity.setPhoneType(1L);
            String androidFileName = null;
            if (file.getOriginalFilename().contains("apk")) {
                //上传apk
                String demo = PATH + file.getOriginalFilename();
                File file1;
                try {
                    file1 = MultipartFileToFile.multipartFileToFile(file);
                } catch (Exception e) {
                    e.printStackTrace();
                    jsonObject.put("code", 500);
                    jsonObject.put("message", "文件上传失败");
                    throw new RuntimeException(e);
                }
                jsonObject.put("apkPath", demo);
                ApkInfo apkInfo = null;
                System.out.println("<-------------- apk 文件解压开始 --------->");
                IPAUtil.decompression(file1, PATH);
                try {
                    System.out.println("<-------------- apk 文件解压开始 --------->");
                    apkInfo = new ApkUtil().getApkInfo(demo);
                    System.out.println("<-------------- apk 文件获取信息 --------->");
                    Map<String, String> applicationIcons = apkInfo.getApplicationIcons();
                    //获取安卓图片
                    String androidIcoPath = applicationIcons.get("application-icon-640");
                    andriodIco = PATH + "andriodIco/" + androidIcoPath;
                    File androidIco = new File((andriodIco));
                    versionName = apkInfo.getVersionName();
                    if (androidIco != null && androidIco.getTotalSpace() > 0) {
                        //安卓图片上传
                        System.out.println("<-------------- apk 文件开始上传 --------->");
                        FileIOUtil.transfer(androidIco, "andriodIco", versionName);
                        System.out.println("<-------------- apk 文件结束上传 --------->");
                    }
                    androidFileName = androidIco.getName();

                    cfBundleDisplayName = apkInfo.getApplicationLable();
                } catch (Exception e) {
                    e.printStackTrace();
                    jsonObject.put("code", 500);
                    jsonObject.put("message", "文件上传失败");
                    throw new RuntimeException(e);
                }
            }
            //删除解压文件
            IPAUtil.deleteDir(PATH + "Payload");
            appEntity.setDownloadUrl(PREFIX + "andriodIco/" + versionName + "/" + file.getOriginalFilename());
            appEntity.setVersion(versionName);
            appEntity.setIcoUrl(PLIST_PREFIX + "andriodIco/"+ versionName + "/" + androidFileName);
            appEntity.setAppType(appType);
            long fileSize = file.getSize();
            long fileSizeToM = fileSize / 1024 /1024;
            appEntity.setAppSize(fileSizeToM + "M");
            appEntity.setAppName(cfBundleDisplayName);
            appServiceImpl.saveApp(appEntity);
            System.out.println("<-------------- apk 信息入库 --------->");
        }

        try {
            File ipaFile = MultipartFileToFile.multipartFileToFile(file);
            if("1".equals(phoneType)){
                FileIOUtil.transfer(ipaFile, "andriodIco", versionName);
            }else if("2".equals(phoneType)){
                FileIOUtil.transfer(ipaFile, "ios", cfBundleVersion);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("文件转换失败");
        }

        jsonObject.put("code", 200);
        jsonObject.put("message", "文件上传成功");
        return jsonObject;
    }

    @PostMapping(value = "/getApp")
    public JSONObject getApp(@RequestParam(required = true, value = "appType") String appType) {
        JSONObject jsonObject = new JSONObject();
        List<AppEntity> appList = new ArrayList<>();
        AppEntity android = appServiceImpl.getApp(1L, appType);
        AppEntity ios = appServiceImpl.getApp(2L, appType);
        appList.add(android);
        appList.add(ios);
        jsonObject.put("code", 200);
        jsonObject.put("android", android);
        jsonObject.put("ios", ios);
        return jsonObject;
    }

    @PostMapping(value = "/getAllApp")
    public JSONObject getAllApp(HttpSession httpSession){

        JSONObject jsonObject = new JSONObject();
        if(httpSession.getAttribute("user") == null){
            jsonObject.put("code", 500);
            jsonObject.put("message", "没有登录");
            return jsonObject;
        }

        List<AppEntity> list = new ArrayList<>();

        AppEntity aiosAndroid = appServiceImpl.getAppByAppTypeAndPhoneType("1", 1L);
        if(aiosAndroid != null){
            list.add(aiosAndroid);
        }else{
            AppEntity aiosAndroidEntity = new AppEntity();
            aiosAndroidEntity.setAppType("1");
            aiosAndroidEntity.setPhoneType(1L);
            aiosAndroidEntity.setAppName("aios");
            list.add(aiosAndroidEntity);
        }

        AppEntity aiosIOS = appServiceImpl.getAppByAppTypeAndPhoneType("1", 2L);

        if(aiosIOS != null){
            list.add(aiosIOS);
        }else{
            AppEntity aiosIosEntity = new AppEntity();
            aiosIosEntity.setAppType("1");
            aiosIosEntity.setPhoneType(2L);
            aiosIosEntity.setAppName("aios");
            list.add(aiosIosEntity);
        }

        AppEntity aicAndroid = appServiceImpl.getAppByAppTypeAndPhoneType("2", 1L);
        if(aicAndroid != null){
            list.add(aicAndroid);
        }else{
            AppEntity aiosAndroidEntity = new AppEntity();
            aiosAndroidEntity.setAppType("2");
            aiosAndroidEntity.setPhoneType(1L);
            aiosAndroidEntity.setAppName("aic");
            list.add(aiosAndroidEntity);
        }

        AppEntity aicIOS = appServiceImpl.getAppByAppTypeAndPhoneType("2", 2L);
        if(aicIOS != null){
            list.add(aicIOS);
        }else{
            AppEntity aiosIOSEntity = new AppEntity();
            aiosIOSEntity.setAppType("2");
            aiosIOSEntity.setPhoneType(2L);
            aiosIOSEntity.setAppName("aic");
            list.add(aiosIOSEntity);
        }
        jsonObject.put("code", 200);
        jsonObject.put("appList", list);
        return jsonObject;
    }

    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public JSONObject login(HttpSession session, @RequestParam(required = true, value = "account") String account, @RequestParam(required = true, value = "password") String password){
        User user = new User();
        user.setUserName(account);
        user.setPassword(password);


        JSONObject jsonObject = new JSONObject();
        if(account.equals("amazingmq")&&password.equals("qqwwee")){

            session.setAttribute("user", user);
            //2.创建User对象保存账号、密码
            //4.登陆成功跳转指定页面
            jsonObject.put("code", 200);
            jsonObject.put("message", "登录成功");
            return jsonObject;
        }else{
            jsonObject.put("code", 500);
            jsonObject.put("message", "登录失败");
        }
        return jsonObject;
    }

    @RequestMapping(value = "/logout",method = RequestMethod.POST)
    public JSONObject logout(HttpSession httpSession){
        JSONObject jsonObject = new JSONObject();
        httpSession.removeAttribute("user");
        jsonObject.put("code", 200);
        jsonObject.put("message", "登出成功");
        return jsonObject;
    }

}
