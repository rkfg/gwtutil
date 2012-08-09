package ru.ppsrk.gwt.server;

import java.util.List;

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
import org.hibernate.Session;

import ru.ppsrk.gwt.client.LogicException;

public class HibernateRealm extends AuthorizingRealm {

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principals) {
        try {
            return HibernateUtil.exec(new HibernateCallback<SimpleAuthorizationInfo>() {

                @Override
                public SimpleAuthorizationInfo run(Session session) {
                    SimpleAuthorizationInfo sai = new SimpleAuthorizationInfo();
                    String principal = (String) principals.getPrimaryPrincipal();
                    @SuppressWarnings("unchecked")
                    List<Perm> perms = session.createQuery("from Perm p where p.user.username = :un").setParameter("un", principal).list();
                    for (Perm perm : perms) {
                        sai.addStringPermission(perm.getPermissions());
                    }
                    @SuppressWarnings("unchecked")
                    List<Role> roles = session.createQuery("from Role r where r.user.username = :un").setParameter("un", principal).list();
                    for (Role role : roles) {
                        sai.addRole(role.getRole());
                    }
                    return sai;
                }
            });
        } catch (LogicException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(
            final AuthenticationToken token) throws AuthenticationException {
        Object principal, credentials;
        ByteSource salt;
        List<User> user = null;
        try {
            user = HibernateUtil.exec(new HibernateCallback<List<User>>() {
                
                @SuppressWarnings("unchecked")
                @Override
                public List<User> run(Session session) {
                    return session.createQuery("from User where username = :un")
                            .setParameter("un", token.getPrincipal()).list();
                }
            });
        } catch (LogicException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (user.size() > 1)
            throw new AuthenticationException("Duplicate users");
        if (user.size() == 0)
            // actually, user wasn't found
            throw new AuthenticationException("Invalid creds");

        principal = user.get(0).getUsername();
        credentials = user.get(0).getPassword();
        salt = ByteSource.Util.bytes(Base64.decode(user.get(0).getSalt()));
        SimpleAuthenticationInfo authinfo = new SimpleAuthenticationInfo(
                principal, credentials, salt, "HibernateRealm");
        if (getCredentialsMatcher().doCredentialsMatch(token, authinfo)) {
            return authinfo;
        } else {
            throw new AuthenticationException("Invalid creds");
        }
    }
}
