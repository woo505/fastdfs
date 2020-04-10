package com.dux.fastdfs.utils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;

import com.alibaba.fastjson.JSONObject;
import com.dd.plist.*;
import com.github.tobato.fastdfs.domain.MataData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.TypedValue;

import java.io.File;

import java.io.FileInputStream;

import java.io.FileOutputStream;

import java.io.IOException;

import java.io.InputStream;

import java.util.zip.ZipInputStream;

public class IPAUtil {

    Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 压缩得到的文件的后缀名
     */
    private static final String SUFFIX = ".zip";

    /**
     * 缓冲器大小
     */
    private static final int BUFFER = 512;


    private static final String TEST_PATH = "E:\\";

    private static final String PATH = "/home/www/temporary/";


    /**
     * IPA文件的拷贝，把一个IPA文件复制为Zip文件,同时返回Info.plist文件
     * 参数 oldfile 为 IPA文件
     */
    public static File getIpaInfo(File oldfile) throws IOException {
        try {
            int byteread = 0;
            String filename = oldfile.getAbsolutePath().replaceAll(".ipa", ".zip");
            File newfile = new File(filename);
            if (oldfile.exists()) {
                // 创建一个Zip文件
                InputStream inStream = new FileInputStream(oldfile);
                FileOutputStream fs = new FileOutputStream(newfile);
                byte[] buffer = new byte[1444];
                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
                if (inStream != null) {
                    inStream.close();
                }
                if (fs != null) {
                    fs.close();
                }
                // 解析Zip文件
                //unzip(newfile, newfile.getParent());
                decompression(new File(newfile.getPath()), PATH);

                //获取Info.plist目录信息
                String appPath = getAppPath();
                File file = new File(PATH + "Payload/" + appPath + "/Info.plist");
                return file;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getAppPath(){
        String payLoad = PATH + "Payload";
        File payLoadDir = new File((payLoad));
        File[] payLoadDirFileArray = payLoadDir.listFiles();
        String appPath = null;
        for(int i = 0; i < payLoadDirFileArray.length; i++){
            if(payLoadDirFileArray[i].getName().contains(".app")){
                appPath = payLoadDirFileArray[i].getName();
                break;
            }
        }
        return appPath;
    }

    //获取icon完整名称
    public static String getIconName(String iconName, String appName){
        String payLoad = PATH + "Payload/" + appName;
        File payLoadDir = new File((payLoad));
        File[] payLoadDirFileArray = payLoadDir.listFiles();
        String name = null;
        for(int i = 0; i < payLoadDirFileArray.length; i++){
            if(payLoadDirFileArray[i].getName().contains(iconName)){
                name = payLoadDirFileArray[i].getName();
                break;
            }
        }
        return name;
    }

    @Test
    public void test10(){
        String iconName = getIconName("AppIcon20x20@3x", "AIOS.app");
        System.out.println(iconName);
    }

    /**
     * 通过IPA文件获取Info信息
     * 这个方法可以重构，原因是指获取了部分重要信息，如果想要获取全部，那么应该返回一个Map<String,Object>
     * 对于plist文件中的数组信息应该序列化存储在Map中，那么只需要第三发jar提供的NSArray可以做到！
     */
    public static Map<String, String> getIpaInfoMap(File ipa) throws Exception {

        Map<String, String> map = new HashMap<String, String>();
        File file = getIpaInfo(ipa);

        // 第三方jar包提供
        NSDictionary rootDict = (NSDictionary) PropertyListParser.parse(file);
        // 应用包名
        NSString parameters = (NSString) rootDict.objectForKey("CFBundleIdentifier");
        map.put("CFBundleIdentifier", parameters.toString());
        // 应用名称
        parameters = (NSString) rootDict.objectForKey("CFBundleName");
        map.put("CFBundleName", parameters.toString());
        // 应用版本
        parameters = (NSString) rootDict.objectForKey("CFBundleShortVersionString");
        map.put("CFBundleShortVersionString", parameters.toString());
        // 应用展示的名称
        parameters = (NSString) rootDict.objectForKey("CFBundleName");
        map.put("CFBundleName", parameters.toString());
        // 应用所需IOS最低版本
        parameters = (NSString) rootDict.objectForKey("MinimumOSVersion");
        map.put("MinimumOSVersion", parameters.toString());

        NSDictionary nSDictionary = (NSDictionary)rootDict.get("CFBundleIcons");
        HashMap<String, NSObject> hashMap = nSDictionary.getHashMap();
        NSDictionary cfBundlePrimaryIcon = (NSDictionary) hashMap.get("CFBundlePrimaryIcon");
        NSArray cfBundleIconFiles = (NSArray)cfBundlePrimaryIcon.get("CFBundleIconFiles");
        NSObject[] array = cfBundleIconFiles.getArray();
        parameters = (NSString) array[0];
        map.put("IconPath", parameters.toString());

        parameters = (NSString) array[array.length - 1];
        map.put("bigIconPath", parameters.toString());
        // 如果有必要，应该删除解压的结果文件
        file.delete();
        file.getParentFile().delete();

        return map;
    }


    /**
     * @param zipPath    zip路径
     * @param outPutPath 输出路径
     * @Description (解压)
     */
    public static void decompression(File file, String outPutPath) {
        try {
            ZipInputStream Zin = new ZipInputStream(new FileInputStream(file));//输入源zip路径
            BufferedInputStream Bin = new BufferedInputStream(Zin);
            String Parent = outPutPath; //输出路径（文件夹目录）
            File Fout = null;
            java.util.zip.ZipEntry entry;
            try {
                while ((entry = Zin.getNextEntry()) != null) {
                    if (!entry.isDirectory()) {
                        Fout = new File(Parent, entry.getName());
                        if (!Fout.exists()) {
                            (new File(Fout.getParent())).mkdirs();
                        }
                        FileOutputStream out = new FileOutputStream(Fout);
                        BufferedOutputStream Bout = new BufferedOutputStream(out);
                        int b;
                        while ((b = Bin.read()) != -1) {
                            Bout.write(b);
                        }
                        Bout.close();
                        out.close();
                    }
                }
                Bin.close();
                Zin.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static boolean createPlist(String identifier, String path, String version, String displayName, String icoPath, String bigIcoPath, String ipaPath) {
        boolean success = true;
        File file = new File(path);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("创建plist文件目录异常", e);
            }
        }
        String plist = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
                "<plist version=\"1.0\">\n" +
                "<dict>\n" +
                "\t<key>items</key>\n" +
                "\t<array>\n" +
                "\t\t<dict>\n" +
                "\t\t\t<key>assets</key>\n" +
                "\t\t\t<array>\n" +
                "\t\t\t\t<dict>\n" +
                "\t\t\t\t\t<key>kind</key>\n" +
                "\t\t\t\t\t<string>software-package</string>\n" +
                "\t\t\t\t\t<key>url</key>\n" +
                "\t\t\t\t\t<string>" + ipaPath + "</string>\n" +
                "\t\t\t\t</dict>\n" +
                "\t\t\t\t<dict>\n" +
                "\t\t\t\t\t<key>kind</key>\n" +
                "\t\t\t\t\t<string>full-size-image</string>\n" +
                "\t\t\t\t\t<key>needs-shine</key>\n" +
                "\t\t\t\t\t<true/>\n" +
                "\t\t\t\t\t<key>url</key>\n" +
                "\t\t\t\t\t<string>" + icoPath + "</string>\n" +
                "\t\t\t\t</dict>\n" +
                "\t\t\t\t<dict>\n" +
                "\t\t\t\t\t<key>kind</key>\n" +
                "\t\t\t\t\t<string>display-image</string>\n" +
                "\t\t\t\t\t<key>needs-shine</key>\n" +
                "\t\t\t\t\t<true/>\n" +
                "\t\t\t\t\t<key>url</key>\n" +
                "\t\t\t\t\t<string>" + bigIcoPath + "</string>\n" +
                "\t\t\t\t</dict>\n" +
                "\t\t\t</array>\n" +
                "\t\t\t<key>metadata</key>\n" +
                "\t\t\t<dict>\n" +
                "\t\t\t\t<key>bundle-identifier</key>\n" +
                "\t\t\t\t<string>" + identifier + "</string>\n" +
                "\t\t\t\t<key>kind</key>\n" +
                "\t\t\t\t<string>software</string>\n" +
                "\t\t\t\t<key>bundle-version</key>\n" +
                "\t\t\t\t<string>" + version + "</string>\n" +
                "\t\t\t\t<key>subtitle</key>\n" +
                "\t\t\t\t<string>install app</string>\n" +
                "\t\t\t\t<key>title</key>\n" +
                "\t\t\t\t<string>" + displayName + "</string>\n" +
                "\t\t\t</dict>\n" +
                "\t\t</dict>\n" +
                "\t</array>\n" +
                "</dict>\n" +
                "</plist>";

        try {
            FileOutputStream output = new FileOutputStream(file);
            OutputStreamWriter writer;
            writer = new OutputStreamWriter(output, "UTF-8");
            writer.write(plist);
            writer.close();
            output.close();
        } catch (Exception e) {
            throw new RuntimeException("创建plist文件异常", e);
        }
        return success;
    }

    @Test
    public void test1() throws Exception {
        createPlist("111", TEST_PATH + "new.plist", "111", "AIOS", "icopath", "bigicopath", "ipaPath");
    }

    @Test
    public void test2() {
        File file = new File("E:\\abc.txt");
        System.out.println(file.getName());
    }

    @Test
    public void test3() throws MalformedURLException {
        File file = new File("E:\\abc.txt");
        System.out.println(file.toURL());
    }

    @Test
    public void teset4() {
        String[] strArray = new String[]{"1", "2"};
        System.out.println(strArray[0]);
        System.out.println(strArray[1]);
    }

    @Test
    public void test5(){
        String apkPath = "app-release.apk";
        File apkFile = new File(apkPath);
        decompression(apkFile, TEST_PATH);
    }

    @Test
    public void test6(){
        File file = new File("E:\\app-release.apk");
        System.out.println(file.getName());
    }

    @Test
    public void test7(){
        File dir = new File("E:\\Payload");
        printDir2(dir);
    }

    @Test
    public void test8(){
        String dirPath = "E:\\Payload";
        deleteDir(dirPath);
    }

    @Test
    public void test9(){
        String payLoad = TEST_PATH + "Payload";
        File payLoadDir = new File((payLoad));
        File[] payLoadDirFileArray = payLoadDir.listFiles();
        File appPath = payLoadDirFileArray[0];

        File file = new File(TEST_PATH + appPath + "/AIOS.app/Info.plist");
        System.out.println(file.getName());
    }

    @Test
    public void test11(){
        int[] array = new int[]{1,2,3,4,5};
        int i = 0;
        for (i = 0; i < array.length; i++) {
            getPrint(i);
        }
    }

    public static void getPrint(int i){
        System.out.println(i);
        getPrint(i);
    }

    public static void deleteDir(String dirPath)
    {
        File file = new File(dirPath);
        if(file.isFile())
        {
            file.delete();
        }else
        {
            File[] files = file.listFiles();
            if(files == null)
            {
                file.delete();
            }else
            {
                for (int i = 0; i < files.length; i++)
                {
                    deleteDir(files[i].getAbsolutePath());
                }
                file.delete();
            }
        }
    }

    public static void printDir2(File dir) {
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".plist")||pathname.isDirectory();
            }
        });

        for (File file : files) {
            if (file.isFile()) {
                System.out.println("文件名:" + file.getAbsolutePath());
            } else {
                printDir2(file);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        /*String path = "D:\\444.zip";
        File file = new File(path);
        file.getName();
        System.out.println(file.getName());
        unzip(file, file.getParent());*/

        //ipa
        String path = "E:\\PodDemo.ipa";
        File file = new File(path);
        Map<String, String> ipaInfoMap = getIpaInfoMap(file);
        for (String key : ipaInfoMap.keySet()) {
            System.out.println("Key: " + key + " Value: " + ipaInfoMap.get(key));
        }
        System.out.println(ipaInfoMap.keySet());
        /*String str = "E:\\digitalCurrency.ipa";
        String[] split = str.split(".");
        for (int i = 0; i < split.length; i++) {
            System.out.println(split[i]);
        }*/

        /*String path = "E:\\digitalCurrency.ipa";
        File file = new File(path);
        file.getName();
        System.out.println(file.getName());
        getIpaInfoMap(file);*/
        //decompression("E:\\digitalCurrency.zip", "E:\\abc\\");
    }
}
