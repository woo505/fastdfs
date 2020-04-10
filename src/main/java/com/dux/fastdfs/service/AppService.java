package com.dux.fastdfs.service;

import com.dux.fastdfs.entity.AppEntity;

public interface AppService {

    AppEntity getApp(Long phoneType, String appType);

    void saveApp(AppEntity appEntity);

    AppEntity getAppByAppTypeAndPhoneType(String appType, Long phoneType);

}
