package ru.ppsrk.gwt.server;

import java.util.LinkedList;
import java.util.List;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.util.ByteSource;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.ClientAuthorizationException;
import ru.ppsrk.gwt.client.GwtUtilException;
import ru.ppsrk.gwt.domain.Perm;
import ru.ppsrk.gwt.domain.Role;
import ru.ppsrk.gwt.domain.User;
import ru.ppsrk.gwt.dto.UserDTO;

public class HibernateRealm extends GwtUtilRealm {

    private static final String USERNAME_QUERY = "from User where username = :un";
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token) throws AuthenticationException {
        Object principal;
        Object credentials;
        ByteSource salt;
        List<User> user = null;
        try {
            user = HibernateUtil.exec(new HibernateCallback<List<User>>() {

                @SuppressWarnings("unchecked")
                @Override
                public List<User> run(Session session) {
                    return session.createQuery(USERNAME_QUERY).setParameter("un", token.getPrincipal()).list();
                }
            });
        } catch (GwtUtilException e) {
            log.warn("Exception while getting user from the database:", e);
        }
        if (user == null || user.isEmpty()) {
            // actually, user wasn't found
            throw new AuthenticationException(INVALID_CREDS);
        }
        if (user.size() > 1) {
            throw new AuthenticationException("Duplicate users");
        }

        principal = user.get(0).getUsername();
        credentials = user.get(0).getPassword();
        salt = ByteSource.Util.bytes(Base64.decode(user.get(0).getSalt()));
        SimpleAuthenticationInfo authinfo = new SimpleAuthenticationInfo(principal, credentials, salt, "HibernateRealm");
        if (getCredentialsMatcher().doCredentialsMatch(token, authinfo)) {
            return authinfo;
        } else {
            throw new AuthenticationException(INVALID_CREDS);
        }
    }

    @Override
    public void loginSuccessful(final String username) throws GwtUtilException {
        List<User> user = HibernateUtil.exec(new HibernateCallback<List<User>>() {

            @SuppressWarnings("unchecked")
            @Override
            public List<User> run(Session session) {
                return session.createQuery(USERNAME_QUERY).setParameter("un", username).list();
            }
        });
        AuthServiceImpl.setSessionAttribute("userid", user.get(0).getId());
    }

    @Override
    public Long register(final String username, final String password, final RandomNumberGenerator rng) throws GwtUtilException {
        return HibernateUtil.exec(new HibernateCallback<Long>() {

            @Override
            public Long run(Session session) {
                HashedBase64Password base64Password = new HashedBase64Password(password, rng);
                User user = (User) session.createQuery("from User where username = :username").setParameter("username", username)
                        .setMaxResults(1).uniqueResult();
                if (user == null) {
                    user = new User(username, base64Password.getPassword());
                } else {
                    // change password for existing user
                    user.setPassword(base64Password.getPassword());
                }
                // save the salt with the new account. The
                // HashedCredentialsMatcher
                // will need it later when handling login attempts:
                user.setSalt(base64Password.getSalt());
                user = (User) session.merge(user);
                return user.getId();
            }
        });
    }

    @Override
    public List<String> getRoles(final String principal) throws GwtUtilException {
        return HibernateUtil.exec(new HibernateCallback<List<String>>() {

            @Override
            public List<String> run(Session session) {
                List<String> result = new LinkedList<>();
                @SuppressWarnings("unchecked")
                List<Role> roles = session.createQuery("from Role r where r.user.username = :un").setParameter("un", principal).list();
                for (Role role : roles) {
                    result.add(role.getRole());
                }
                return result;
            }
        });
    }

    @Override
    public List<String> getPerms(final String principal) throws GwtUtilException {
        return HibernateUtil.exec(new HibernateCallback<List<String>>() {

            @Override
            public List<String> run(Session session) {
                List<String> result = new LinkedList<>();
                @SuppressWarnings("unchecked")
                List<Perm> perms = session.createQuery("from Perm p where p.user.username = :un").setParameter("un", principal).list();
                for (Perm perm : perms) {
                    result.add(perm.getPermissions());
                }
                return result;
            }
        });
    }

    @Override
    public UserDTO getUser(final String principal) throws GwtUtilException {
        return HibernateUtil.exec(new HibernateCallback<UserDTO>() {

            @Override
            public UserDTO run(Session session) throws GwtUtilException {
                @SuppressWarnings("unchecked")
                List<User> users = session.createQuery(USERNAME_QUERY).setParameter("un", principal).list();
                if (users.size() != 1) {
                    throw new ClientAuthenticationException("Not authenticated");
                }
                return ServerUtils.mapModel(users.get(0), UserDTO.class);
            }
        });
    }

    @Override
    protected String getRoleId(final Long roleId) throws GwtUtilException {
        return HibernateUtil.exec(new HibernateCallback<String>() {

            @Override
            public String run(Session session) throws ClientAuthorizationException {
                Role role = (Role) session.get(Role.class, roleId);
                if (role != null) {
                    return role.getRole();
                }
                throw new ClientAuthorizationException(INVALID_CREDS);
            }
        });
    }
}
