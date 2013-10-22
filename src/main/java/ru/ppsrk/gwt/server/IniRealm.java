package ru.ppsrk.gwt.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;

import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.ClientAuthorizationException;
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
                throw new AuthenticationException("Invalid creds");
            }
            String passData = smAuth.getStringSetting(principal);
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

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo sai = new SimpleAuthorizationInfo();
        String principal = (String) principals.getPrimaryPrincipal();
        for (String role : getRoles(principal)) {
            sai.addRole(role);
        }
        return sai;
    }

    @Override
    public List<String> getPerms(String principal) throws LogicException, ClientAuthenticationException {
        return new LinkedList<String>();
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
    public boolean login(String username, String password, boolean remember) throws ClientAuthenticationException, ClientAuthorizationException {
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(new UsernamePasswordToken(username, password, remember));
        } catch (AuthenticationException e) {
            throw new ClientAuthenticationException(e.getMessage());
        } catch (AuthorizationException e) {
            throw new ClientAuthorizationException(e.getMessage());
        }
        return subject.isAuthenticated();
    }

    @Override
    public Long register(String username, String password, RandomNumberGenerator rng) throws LogicException, ClientAuthenticationException {
        SettingsManager sm = new SettingsManager();
        sm.setFilename("auth.ini");
        ByteSource salt = rng.nextBytes();
        String hashedPasswordBase64 = new Sha256Hash(password, salt, 1024).toBase64();
        String credentials = hashedPasswordBase64 + "|" + salt.toBase64();
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

}
