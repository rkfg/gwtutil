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

public class HibernateRealm extends AuthorizingRealm {

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(
            PrincipalCollection principals) {
        String principal = (String) principals.getPrimaryPrincipal();
        SimpleAuthorizationInfo sai = new SimpleAuthorizationInfo();
        Session session = HibernateUtil.getSessionFactory(0).openSession();
        session.beginTransaction();
        @SuppressWarnings("unchecked")
        List<Perm> perms = session
                .createQuery("from Perm p where p.user.username = :un")
                .setParameter("un", principal).list();
        for (Perm perm : perms) {
            sai.addStringPermission(perm.getPermissions());
        }
        @SuppressWarnings("unchecked")
        List<Role> roles = session
                .createQuery("from Role r where r.user.username = :un")
                .setParameter("un", principal).list();
        for (Role role: roles){
            sai.addRole(role.getRole());
        }
        return sai;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(
            AuthenticationToken token) throws AuthenticationException {
        Object principal, credentials;
        ByteSource salt;
        Session session = HibernateUtil.getSession();
        @SuppressWarnings("unchecked")
        List<User> user = session.createQuery("from User where username = :un")
                .setParameter("un", token.getPrincipal()).list();
        HibernateUtil.endSession(session);
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
