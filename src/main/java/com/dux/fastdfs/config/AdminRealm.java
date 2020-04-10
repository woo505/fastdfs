package com.dux.fastdfs.config;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

public class AdminRealm extends AuthorizingRealm {

    private static final String ACCOUNT = "amazingmq";

    private static final String PASSWORD = "qqwwee";

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        AuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(ACCOUNT,PASSWORD,getName());
        return authenticationInfo;

        /*if (sellerInfo != null){
            AuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(sellerInfo.getUsername(),sellerInfo.getPassword(),getName());
            return authenticationInfo;
        }else {
            return null;
        }*/
    }
}
