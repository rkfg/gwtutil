package ru.ppsrk.gwt.server;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;

public class IniRealm extends AuthorizingRealm {

    SettingsManager sm = new SettingsManager();

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo sai = new SimpleAuthorizationInfo();
        // String principal = (String) principals.getPrimaryPrincipal();
        return sai;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        try {
            sm.setFilename("auth.ini");
            sm.loadSettings();
            String principal = (String) token.getPrincipal();
            if (principal == null) {
                throw new AuthenticationException("Invalid creds");
            }
            String passData = sm.getStringSetting(principal);
            if (passData == null) {
                throw new AuthenticationException("Invalid creds");
            }
            if (passData.indexOf('|') < 0) {
                throw new AuthenticationException("Invalid creds");
            }
            String[] credentials = passData.split("\\|");
            SimpleAuthenticationInfo authinfo = new SimpleAuthenticationInfo(token.getPrincipal(), credentials[0], ByteSource.Util.bytes(Base64
                    .decode(credentials[1])), "IniRealm");
            if (getCredentialsMatcher().doCredentialsMatch(token, authinfo)) {
                return authinfo;
            } else {
                throw new AuthenticationException("Invalid creds");
            }
        } catch (FileNotFoundException e) {
            throw new AuthenticationException("auth.ini not found.");
        } catch (IOException e) {
            throw new AuthenticationException("IOException: " + e.getMessage());
        }
    }

}
