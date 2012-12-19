/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ru.ppsrk.gwt.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.hibernate.Session;

import ru.ppsrk.gwt.client.Auth;
import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.ClientAuthorizationException;
import ru.ppsrk.gwt.client.LogicException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class AuthImpl extends RemoteServiceServlet implements Auth {
    RandomNumberGenerator rng = new SecureRandomNumberGenerator();

    /**
     * 
     */
    private static final long serialVersionUID = -5049525086987492554L;
    public static boolean registrationEnabled = false;

    @Override
    public boolean login(final String username, String password, boolean remember) throws ClientAuthenticationException, ClientAuthorizationException,
            LogicException {
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(new UsernamePasswordToken(username, password, remember));
            if (subject.isAuthenticated()) {
                List<User> user = HibernateUtil.exec(new HibernateCallback<List<User>>() {

                    @SuppressWarnings("unchecked")
                    @Override
                    public List<User> run(Session session) {
                        // TODO Auto-generated method stub
                        return session.createQuery("from User where username = :un").setParameter("un", username).list();
                    }
                });
                setSessionAttribute("userid", user.get(0).getId());
                return true;
            }
        } catch (AuthenticationException e) {
            throw new ClientAuthenticationException(e.getMessage());
        } catch (AuthorizationException e) {
            throw new ClientAuthorizationException(e.getMessage());
        }
        return subject.isAuthenticated();
    }

    @Override
    public boolean register(final String username, final String password) throws LogicException, ClientAuthenticationException {
        if (!registrationEnabled) {
            return false;
        }
        HibernateUtil.exec(new HibernateCallback<Void>() {

            @Override
            public Void run(Session session) {
                ByteSource salt = rng.nextBytes();
                String hashedPasswordBase64 = new Sha256Hash(password, salt, 1024).toBase64();

                User user = (User) session.createQuery("from User where username = :username").setParameter("username", username).setMaxResults(1)
                        .uniqueResult();
                if (user == null) {
                    user = new User(username, hashedPasswordBase64);
                } else {
                    // change password for existing user
                    user.setPassword(hashedPasswordBase64);
                }
                // save the salt with the new account. The
                // HashedCredentialsMatcher
                // will need it later when handling login attempts:
                user.setSalt(salt.toBase64());
                session.save(user);
                return null;
            }
        });
        return true;
    }

    public static User requiresAuthUser() throws ClientAuthenticationException, LogicException {
        if (!SecurityUtils.getSubject().isAuthenticated() && !SecurityUtils.getSubject().isRemembered())
            throw new ClientAuthenticationException("Not authenticated");
        UserDTO user = (UserDTO) getSessionAttribute("user");
        if (user == null) {
            user = HibernateUtil.exec(new HibernateCallback<UserDTO>() {

                @Override
                public UserDTO run(Session session) throws LogicException, ClientAuthenticationException {
                    @SuppressWarnings("unchecked")
                    List<User> users = session.createQuery("from User where username = :un").setParameter("un", SecurityUtils.getSubject().getPrincipal())
                            .list();
                    if (users.size() != 1) {
                        throw new ClientAuthenticationException("Not authenticated");
                    }
                    return ServerUtils.mapModel(users.get(0), UserDTO.class);
                }
            });
            setSessionAttribute("user", user);
        }
        return ServerUtils.mapModel(user, User.class);
    }

    public static Long requiresAuth() throws ClientAuthenticationException, LogicException {
        User user = requiresAuthUser();
        if (user == null) {
            return 0L;
        } else {
            return user.getId();
        }
    }

    public static void requiresPerm(String perm) throws ClientAuthorizationException {
        if (!SecurityUtils.getSubject().isPermitted(perm))
            throw new ClientAuthorizationException("Not authorized [perm: " + perm + "]");
    }

    public static void requiresRole(String role) throws ClientAuthorizationException {
        if (!SecurityUtils.getSubject().hasRole(role))
            throw new ClientAuthorizationException("Not authorized [role: " + role + "]");
    }

    @Override
    public void logout() {
        removeSessionAttribute("userid");
        SecurityUtils.getSubject().logout();
    }

    @Override
    public boolean isRegistrationEnabled() {
        return registrationEnabled;
    }

    public static List<String> getRoles() throws LogicException, ClientAuthenticationException {
        final Long userId = requiresAuth();
        return HibernateUtil.exec(new HibernateCallback<List<String>>() {

            @Override
            public List<String> run(Session session) {
                User user = (User) session.get(User.class, userId);
                if (user == null) {
                    return new ArrayList<String>();
                }
                Set<Role> roles = user.getRoles();
                List<String> result = new ArrayList<String>();
                for (Role role : roles) {
                    result.add(role.getRole());
                }
                setSessionAttribute("roles", result);
                return result;
            }
        });

    }

    public static boolean hasRole(String role) throws LogicException, ClientAuthenticationException {
        return getRoles().contains(role);
    }

    public static void setSessionAttribute(Object key, Object value) {
        SecurityUtils.getSubject().getSession().setAttribute(key, value);
    }

    public static Object getSessionAttribute(Object key) {
        return SecurityUtils.getSubject().getSession().getAttribute(key);
    }

    public static void removeSessionAttribute(Object key) {
        SecurityUtils.getSubject().getSession().removeAttribute(key);
    }
    
    public boolean isLoggedIn() {
        return SecurityUtils.getSubject().isAuthenticated() || SecurityUtils.getSubject().isRemembered();
    }

    /*public static interface CacheCallback<T> {
        public T exec() throws LogicException, ClientAuthenticationException;
    }*/

    /*
     * public static <T> T getCachedData(String key, CacheCallback<T> callback)
     * throws LogicException, ClientAuthenticationException { T cachedData; //
     * the following code should be used for caching purposes but currently it's
     * too naive to really help if (GWT.isProdMode()) { cachedData = (T)
     * getSessionAttribute(key); if (cachedData != null) { return cachedData; }
     * }
     * 
     * cachedData = callback.exec(); // setSessionAttribute(key, cachedData);
     * return cachedData; }
     */
}
