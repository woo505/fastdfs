package com.dux.fastdfs.sqlmapper;

import com.dux.fastdfs.entity.AppEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;


@Repository("AppMapper")
public interface AppDownloadMapper {

    AppEntity getApp(@Param("phoneType") Long phoneType, @Param("appType") String appType);

    void saveApp(AppEntity appEntity);

    AppEntity getAppByAppTypeAndPhoneType(@Param("appType") String appType, @Param("phoneType") Long phoneType);

}
