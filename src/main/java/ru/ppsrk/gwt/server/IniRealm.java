package ru.ppsrk.gwt.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.util.ByteSource;

import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.dto.UserDTO;

public class IniRealm extends GwtUtilRealm {

    SettingsManager smAuth = new SettingsManager();
    SettingsManager smRoles = new SettingsManager();

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        try {
            smAuth.setFilename("auth.ini");
            smAuth.loadSettings();
            String principal = (String) token.getPrincipal();
            if (principal == null) {
                throw new AuthenticationException(INVALID_CREDS);
            }
            String passData = smAuth.getStringSetting(principal);
            if (passData == null) {
                throw new AuthenticationException(INVALID_CREDS);
            }
            if (passData.indexOf('|') < 0) {
                throw new AuthenticationException(INVALID_CREDS);
            }
            String[] credentials = passData.split("\\|");
            SimpleAuthenticationInfo authinfo = new SimpleAuthenticationInfo(token.getPrincipal(), credentials[0],
                    ByteSource.Util.bytes(Base64.decode(credentials[1])), "IniRealm");
            if (getCredentialsMatcher().doCredentialsMatch(token, authinfo)) {
                return authinfo;
            } else {
                throw new AuthenticationException(INVALID_CREDS);
            }
        } catch (FileNotFoundException e) {
            throw new AuthenticationException("auth.ini not found.");
        } catch (IOException e) {
            throw new AuthenticationException("IOException: " + e.getMessage());
        }
    }

    @Override
    public List<String> getPerms(String principal) throws LogicException, ClientAuthenticationException {
        return null;
    }

    @Override
    public List<String> getRoles(String principal) {
        List<String> result = new LinkedList<String>();
        try {
            smRoles.setFilename("roles.ini");
            smRoles.loadSettings();
            String roles = smRoles.getStringSetting(principal);
            if (roles != null) {
                for (String role : roles.split(",")) {
                    result.add(role);
                }
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            throw new AuthenticationException("IOException: " + e.getMessage());
        }
        return result;
    }

    @Override
    public Long register(String username, String password, RandomNumberGenerator rng) throws LogicException, ClientAuthenticationException {
        SettingsManager sm = new SettingsManager();
        sm.setFilename("auth.ini");
        HashedBase64Password base64password = new HashedBase64Password(password, rng);
        String credentials = base64password.getPassword() + "|" + base64password.getSalt();
        sm.setStringSetting(username, credentials);
        try {
            sm.saveSettings();
        } catch (FileNotFoundException e) {
            throw new LogicException("File auth.ini not found.");
        } catch (IOException e) {
            throw new LogicException("IOException: " + e.getMessage());
        }
        return 1L;
    }

    @Override
    public UserDTO getUser(String principal) throws LogicException, ClientAuthenticationException {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername(principal);
        return userDTO;
    }

    @Override
    protected String getRoleId(Long roleId) throws LogicException, ClientAuthException {
        return null;
    }

}
