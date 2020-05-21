package com.dux.fastdfs.controller;

import com.alibaba.fastjson.JSONObject;
import com.dux.fastdfs.config.ApkInfo;
import com.dux.fastdfs.config.User;
import com.dux.fastdfs.entity.AppEntity;
import com.dux.fastdfs.service.AppServiceImpl;
import com.dux.fastdfs.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.dux.fastdfs.utils.IPAUtil.getIpaInfoMap;

@RestController
@Slf4j
public class FileController {

    Logger logger = LoggerFactory.getLogger(getClass());

    private static final String TEST_PATH = "E:\\";

    private static final String PATH = "/home/www/temporary/";

    private static final String PLIST_PREFIX = "https://file.aios-asc.com/";

    private static final String TEST_ICO = "E:\\Payload\\AIOS.app\\AppIcon20x20@3x.png";

    private static final String ICO_PATH = "/home/www/temporary/Payload/AIOS.app/AppIcon20x20@3x.png";

    private static final String TEST_BIG_ICO = "E:\\Payload\\AIOS.app\\AppIcon60x60@3x.png";

    private static final String BIG_ICO_PATH = "/home/www/temporary/Payload/AIOS.app/AppIcon60x60@3x.png";

    private static final String PREFIX = "https://file.aios-asc.com/";

    private static final String Android_PATH = "https://file.aios-asc.com/app-release.apk";

    private static final String TEST_Android_ICO = "E:\\res\\mipmap-xxxhdpi-v4\\ic_launcher.png";

    private static final String Android_ICO = "https://file.aios-asc.com/ic_launcher.png";

    private static final String IOS_ICO = "https://file.aios-asc.com/AppIcon60x60@3x.png";

    private static final String IOS_ICO_PREFIX = "/home/www/temporary/Payload/";

    public static final int DEFAULT_METHOD_EXPIRE_SECOND_DAY = 5;

    @Resource
    private AppServiceImpl appServiceImpl;

    @Resource
    private RedisTemplate redisTemplate;


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
    public JSONObject upload(@RequestHeader(value = "appToken", required = true) String appToken,
                             @RequestParam(required = true, value = "file") MultipartFile file,
                             @RequestParam(required = true, value = "phoneType") String phoneType,
                             @RequestParam(required = true, value = "appType") String appType) {


        JSONObject jsonObject = new JSONObject();

        if(StringUtils.isNotBlank(appType)){
            if(!appType.equals("3") || !appType.equals("4")){
                jsonObject.put("code", 500);
                jsonObject.put("message", "上传错误");
                return jsonObject;
            }
        }

        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object sessionId = valueOperations.get(appToken);

        if(org.springframework.util.ObjectUtils.isEmpty(sessionId)){
            jsonObject.put("code", 401);
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

        long currentTime = System.currentTimeMillis();

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
                    FileIOUtil.transfer(ico, "ios", cfBundleVersion, currentTime);
                }
                //获取大图片
                String bigIconName = IPAUtil.getIconName(iosBigIcoName, appPath);
                File bigIco = new File((IOS_ICO_PREFIX + appPath + "/" + bigIconName));
                if (bigIco != null && bigIco.getTotalSpace() > 0) {
                    //大图片上传
                    FileIOUtil.transfer(bigIco, "ios", cfBundleVersion, currentTime);
                }


                //生成newplist文件
                String newPlistPath = PATH + "ios/" + cfBundleVersion + "/"+ currentTime + "manifest.plist";
                IPAUtil.createPlist(cfBundleIdentifier, newPlistPath, cfBundleVersion, cfBundleDisplayName, PREFIX + "ios" + "/" + cfBundleVersion + "/" + iconName, PREFIX + "ios" + "/" + cfBundleVersion + "/" + bigIconName, PREFIX + "ios" + "/" + cfBundleVersion + "/" + file.getOriginalFilename());
                String newPlistUrl = PLIST_PREFIX  + "ios/" + cfBundleVersion + "/" +  currentTime + "manifest.plist";

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
                appEntity.setIcoUrl(PLIST_PREFIX + "ios/" + cfBundleVersion + "/" + currentTime + bigIconName);
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
                    System.out.println("<-------------- apk 图片路径" + androidIcoPath + "--------->");
                    andriodIco = PATH  + androidIcoPath;
                    File androidIco = new File((andriodIco));
                    versionName = apkInfo.getVersionName();
                    if (androidIco != null && androidIco.getTotalSpace() > 0) {
                        //安卓图片上传
                        System.out.println("<-------------- apk 文件开始上传 --------->");
                        FileIOUtil.transfer(androidIco, "andriodIco", versionName, currentTime);
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
            appEntity.setDownloadUrl(PREFIX + "andriodIco/" + versionName + "/" + currentTime + file.getOriginalFilename());
            appEntity.setVersion(versionName);
            appEntity.setIcoUrl(PLIST_PREFIX + "andriodIco/"+ versionName + "/" + currentTime + androidFileName);
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
                FileIOUtil.transfer(ipaFile, "andriodIco", versionName, currentTime);
            }else if("2".equals(phoneType)){
                FileIOUtil.transfer(ipaFile, "ios", cfBundleVersion, currentTime);
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
    public JSONObject getApp(@RequestParam(required = true, value = "appName") String appName) {
        JSONObject jsonObject = new JSONObject();
        List<AppEntity> appList = new ArrayList<>();
        String appType = null;
        if(StringUtils.isNotBlank(appName)){
            /*if("aios".equals(appName)){
                appType = "1";
            }
            else if("aic".equals(appName)){
                appType = "2";
            }*/
            if("aim".equals(appName)){
                appType = "3";
            }
            else if("aiminer".equals(appName)){
                appType = "4";
            }
            else{
                appType = null;
            }
            if(StringUtils.isNotBlank(appType)){
                AppEntity android = appServiceImpl.getApp(1L, appType);
                AppEntity ios = appServiceImpl.getApp(2L, appType);
                appList.add(android);
                appList.add(ios);
                jsonObject.put("code", 200);
                jsonObject.put("android", android);
                jsonObject.put("ios", ios);
            }
            else{
                jsonObject.put("code", 500);
                jsonObject.put("message", "不存在的项目");
            }
        }
        return jsonObject;
    }

    @PostMapping(value = "/getAllApp")
    public JSONObject getAllApp(@RequestHeader(value = "appToken", required = true) String appToken){

        JSONObject jsonObject = new JSONObject();

        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object sessionId = valueOperations.get(appToken);

        if(org.springframework.util.ObjectUtils.isEmpty(sessionId)){
            jsonObject.put("code", 401);
            jsonObject.put("message", "没有登录");
            return jsonObject;
        }
        List<AppEntity> list = new ArrayList<>();

        /*AppEntity aiosAndroid = appServiceImpl.getAppByAppTypeAndPhoneType("1", 1L);
        if(aiosAndroid != null){
            aiosAndroid.setAppName("aios");
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
            aiosIOS.setAppName("aios");
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
            aicAndroid.setAppName("aic");
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
            aicIOS.setAppName("aic");
            list.add(aicIOS);
        }else{
            AppEntity aiosIOSEntity = new AppEntity();
            aiosIOSEntity.setAppType("2");
            aiosIOSEntity.setPhoneType(2L);
            aiosIOSEntity.setAppName("aic");
            list.add(aiosIOSEntity);
        }*/

        AppEntity aisAndroid = appServiceImpl.getAppByAppTypeAndPhoneType("3", 1L);
        if(aisAndroid != null){
            aisAndroid.setAppName("aim");
            list.add(aisAndroid);
        }else{
            AppEntity aisAndroidEntity = new AppEntity();
            aisAndroidEntity.setAppType("3");
            aisAndroidEntity.setPhoneType(1L);
            aisAndroidEntity.setAppName("aim");
            list.add(aisAndroidEntity);
        }

        AppEntity aisIOS = appServiceImpl.getAppByAppTypeAndPhoneType("3", 2L);
        if(aisIOS != null){
            aisIOS.setAppName("aim");
            list.add(aisIOS);
        }else{
            AppEntity aisIOSEntity = new AppEntity();
            aisIOSEntity.setAppType("3");
            aisIOSEntity.setPhoneType(2L);
            aisIOSEntity.setAppName("aim");
            list.add(aisIOSEntity);
        }

        AppEntity aiminerAndroid = appServiceImpl.getAppByAppTypeAndPhoneType("4", 1L);
        if(aiminerAndroid != null){
            aiminerAndroid.setAppName("aiminer");
            list.add(aiminerAndroid);
        }else{
            AppEntity aiminerAndroidEntity = new AppEntity();
            aiminerAndroidEntity.setAppType("4");
            aiminerAndroidEntity.setPhoneType(1L);
            aiminerAndroidEntity.setAppName("aiminer");
            list.add(aiminerAndroidEntity);
        }

        AppEntity aiminerIOS = appServiceImpl.getAppByAppTypeAndPhoneType("4", 2L);
        if(aiminerIOS != null){
            aiminerIOS.setAppName("aiminer");
            list.add(aiminerIOS);
        }else{
            AppEntity aiminerIOSEntity = new AppEntity();
            aiminerIOSEntity.setAppType("4");
            aiminerIOSEntity.setPhoneType(2L);
            aiminerIOSEntity.setAppName("aiminer");
            list.add(aiminerIOSEntity);
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

            ValueOperations valueOperations = redisTemplate.opsForValue();
            String token = Util.getUuid();
            System.out.println(token);
            System.out.println(session.getId());
            valueOperations.set(token, session.getId(),60 * 60 * 24, TimeUnit.SECONDS);
            jsonObject.put("appToken", token);
            //session.setAttribute("user", user);
            //2.创建User对象保存账号、密码
            //4.登陆成功跳转指定页面
            jsonObject.put("code", 200);
            jsonObject.put("message", "登录成功");
            return jsonObject;
        }else{
            jsonObject.put("code", 401);
            jsonObject.put("message", "登录失败");
        }
        return jsonObject;
    }

    @RequestMapping(value = "/logout",method = RequestMethod.POST)
    public JSONObject logout(@RequestHeader(value = "appToken", required = true) String appToken){
        JSONObject jsonObject = new JSONObject();
        Boolean delete = redisTemplate.delete(appToken);
        if(delete){
            jsonObject.put("code", 200);
            jsonObject.put("message", "登出成功");
            return jsonObject;
        }else{
            jsonObject.put("code", 401);
            jsonObject.put("message", "登出失败");
        }
        return jsonObject;
    }

}
