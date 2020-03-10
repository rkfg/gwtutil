package ru.ppsrk.gwt.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.dto.UserDTO;

public class IniRealm extends GwtUtilRealm {

    SettingsManager smAuth = new SettingsManager();
    SettingsManager smRoles = new SettingsManager();
    private Logger log = LoggerFactory.getLogger(getClass());

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
            return makeAuthInfo(token, (String) token.getPrincipal(), credentials[0], credentials[1]);
        } catch (FileNotFoundException e) {
            throw new AuthenticationException("auth.ini not found.", e);
        } catch (IOException e) {
            throw new AuthenticationException(e);
        }
    }

    /**
     * Not implemented, always returns an empty set
     */
    @Override
    public Set<String> getPerms(String principal) throws LogicException, ClientAuthenticationException {
        return new HashSet<>();
    }

    @Override
    public Set<String> getRoles(String principal) {
        Set<String> result = new HashSet<>();
        try {
            smRoles.setFilename("roles.ini");
            smRoles.loadSettings();
            String roles = smRoles.getStringSetting(principal);
            if (roles != null) {
                for (String role : roles.split(",")) {
                    result.add(role);
                }
            }
        } catch (IOException e) {
            throw new AuthenticationException(e);
        }
        return result;
    }

    @Override
    public Long register(String username, String password, RandomNumberGenerator rng) throws LogicException, ClientAuthenticationException {
        SettingsManager sm = new SettingsManager("auth.ini");
        try {
            sm.loadSettings();
            HashedBase64Password base64password = new HashedBase64Password(password, rng);
            String credentials = base64password.getPassword() + "|" + base64password.getSalt();
            sm.setStringSetting(username, credentials);
            sm.saveSettings();
        } catch (FileNotFoundException e) {
            log.warn("File auth.ini not found:", e);
            throw new LogicException("File auth.ini not found.");
        } catch (IOException e) {
            log.warn("IOException:", e);
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
