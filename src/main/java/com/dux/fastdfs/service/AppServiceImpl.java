package com.dux.fastdfs.service;

import com.dux.fastdfs.sqlmapper.AppDownloadMapper;
import com.dux.fastdfs.entity.AppEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
public class AppServiceImpl implements AppService {

    @Resource
    private AppDownloadMapper appDownloadMapper;

    @Override
    public AppEntity getApp(Long phoneType, String appType) {
        return appDownloadMapper.getApp(phoneType, appType);
    }

    @Override
    public void saveApp(AppEntity appEntity) {
        appDownloadMapper.saveApp(appEntity);
    }

    @Override
    public AppEntity getAppByAppTypeAndPhoneType(String appType, Long phoneType) {
        return appDownloadMapper.getAppByAppTypeAndPhoneType(appType, phoneType);
    }

}
