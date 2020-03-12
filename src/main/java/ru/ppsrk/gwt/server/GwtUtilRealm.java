package ru.ppsrk.gwt.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.ClientAuthorizationException;
import ru.ppsrk.gwt.client.GwtUtilException;
import ru.ppsrk.gwt.dto.UserDTO;

public abstract class GwtUtilRealm extends AuthorizingRealm {

    private Logger log = LoggerFactory.getLogger(getClass());

    protected static final String INVALID_CREDS = "invalid creds";

    private Map<Long, String> rolesCache = new HashMap<>();

    public class HashedBase64Password {
        private String password;
        private String salt;

        public HashedBase64Password(String password, RandomNumberGenerator rng) {
            ByteSource salt = rng.nextBytes();
            HashedCredentialsMatcher matcher = ((HashedCredentialsMatcher) GwtUtilRealm.this.getCredentialsMatcher());
            this.password = new SimpleHash(matcher.getHashAlgorithmName(), password, salt, matcher.getHashIterations()).toBase64();
            this.salt = salt.toBase64();
        }

        public HashedBase64Password(String hashedb64password, String b64salt) {
            password = hashedb64password;
            salt = b64salt;
        }

        public String getPassword() {
            return password;
        }

        public String getSalt() {
            return salt;
        }

        public ByteSource getByteSourceSalt() {
            return GwtUtilRealm.getByteSourceSalt(salt);
        }

    }

    public static ByteSource getByteSourceSalt(String salt) {
        return ByteSource.Util.bytes(Base64.decode(salt));
    }

    public boolean login(AuthenticationToken token) throws GwtUtilException {
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(token);
            if (subject.isAuthenticated()) {
                loginSuccessful((String) token.getPrincipal());
            }
        } catch (AuthenticationException e) {
            log.warn("Auth exception:", e);
            throw new ClientAuthenticationException("Authentication exception on login: " + e.getMessage());
        } catch (AuthorizationException e) {
            log.warn("Auth exception:", e);
            throw new ClientAuthorizationException("Authorization exception on login: " + e.getMessage());
        }
        return subject.isAuthenticated();
    }

    public boolean login(final String username, String password, boolean remember) throws GwtUtilException {
        return login(new UsernamePasswordToken(username, password, remember));
    }

    protected SimpleAuthenticationInfo makeAuthInfo(AuthenticationToken token, String principal, String credentials, String salt) {
        return new SimpleAuthenticationInfo(principal, credentials, getByteSourceSalt(salt), getClass().getSimpleName());
    }

    @Override
    protected void assertCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) throws AuthenticationException {
        if (token instanceof WildcardToken) {
            return;
        }
        super.assertCredentialsMatch(token, info);
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principals) {
        try {
            String principal = (String) principals.getPrimaryPrincipal();
            SimpleAuthorizationInfo sai = new SimpleAuthorizationInfo();
            Set<String> perms = getPerms(principal);
            if (perms != null) {
                for (String perm : perms) {
                    sai.addStringPermission(perm);
                }
            }
            Set<String> roles = getRoles(principal);
            if (roles != null) {
                for (String role : roles) {
                    sai.addRole(role);
                }
            }
            return sai;
        } catch (GwtUtilException e) {
            log.warn("Exception getting authorization info: {}", e);
        }
        return null;
    }

    public Long register(final String username, final String password) throws GwtUtilException {
        return register(username, password, new SecureRandomNumberGenerator());
    }

    public abstract Long register(final String username, final String password, RandomNumberGenerator rng) throws GwtUtilException;

    public abstract Set<String> getRoles(String principal) throws GwtUtilException;

    public abstract Set<String> getPerms(String principal) throws GwtUtilException;

    public abstract UserDTO getUser(String principal) throws GwtUtilException;

    public void loginSuccessful(String username) throws GwtUtilException {

    }

    public String getRoleById(Long roleId) throws GwtUtilException {
        String result = rolesCache.get(roleId);
        if (result == null) {
            result = getRoleId(roleId);
            if (result == null) {
                throw new ClientAuthorizationException(INVALID_CREDS);
            }
            rolesCache.put(roleId, result);
        }
        return result;
    }

    protected abstract String getRoleId(Long roleId) throws GwtUtilException;

    @Override
    public boolean supports(AuthenticationToken token) {
        return super.supports(token) || token instanceof WildcardToken;
    }

    public void cleanup() throws GwtUtilException {
    }
}
