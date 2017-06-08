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

import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.apache.shiro.util.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import ru.ppsrk.gwt.client.AuthService;
import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.ClientAuthorizationException;
import ru.ppsrk.gwt.client.GwtUtilException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.domain.User;
import ru.ppsrk.gwt.dto.UserDTO;

public class AuthServiceImpl extends RemoteServiceServlet implements AuthService {
    private static final String NOT_AUTHENTICATED = "Not authenticated";

    private static final String USER_DTO = "userDTO";

    private static Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    public static GwtUtilRealm getRealm() throws LogicException {
        Iterator<Realm> realms = ((RealmSecurityManager) SecurityUtils.getSecurityManager()).getRealms().iterator();
        Realm realm = realms.next();
        if (!(realm instanceof GwtUtilRealm)) {
            throw new LogicException(
                    "Realm " + realm.getName() + " isn't compatible to GwtUtilRealm, its type is: " + realm.getClass().getSimpleName());
        }
        return (GwtUtilRealm) realm;
    }

    public static List<String> getRoles() throws GwtUtilException {
        return getRealm().getRoles(requiresAuthUser().getUsername());
    }

    public static UserDTO getUserDTO() throws GwtUtilException {
        Subject subject = SecurityUtils.getSubject();
        if (subject == null) {
            throw new ClientAuthenticationException(NOT_AUTHENTICATED);
        }
        return getRealm().getUser((String) subject.getPrincipal());
    }

    public static Object getSessionAttribute(Object key) throws ClientAuthException {
        Subject subject = SecurityUtils.getSubject();
        if (subject == null) {
            throw new ClientAuthenticationException(NOT_AUTHENTICATED);
        }
        return subject.getSession().getAttribute(key);
    }

    public static boolean hasPerm(String perm) throws ClientAuthException {
        Subject subject = SecurityUtils.getSubject();
        if (subject == null) {
            throw new ClientAuthenticationException(NOT_AUTHENTICATED);
        }
        return subject.isPermitted(perm);
    }

    public static boolean hasRole(String role) throws GwtUtilException {
        Subject subject = SecurityUtils.getSubject();
        if (subject == null) {
            throw new ClientAuthenticationException(NOT_AUTHENTICATED);
        }
        return subject.hasRole(role);
    }

    public static boolean hasRole(Long roleId) throws GwtUtilException {
        return hasRole(getRealm().getRoleById(roleId));
    }

    public static void initShiro() {
        Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
        SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);
    }

    public static void removeSessionAttribute(Object key) throws ClientAuthenticationException {
        Subject subject = SecurityUtils.getSubject();
        if (subject == null) {
            throw new ClientAuthenticationException(NOT_AUTHENTICATED);
        }
        subject.getSession().removeAttribute(key);
    }

    public static Long requiresAuth() throws GwtUtilException {
        User user = requiresAuthUser();
        if (user == null) {
            return 0L;
        } else {
            return user.getId();
        }
    }

    public static User requiresAuthUser() throws GwtUtilException {
        if (!isAuthenticated())
            throw new ClientAuthenticationException(NOT_AUTHENTICATED);
        UserDTO user = (UserDTO) getSessionAttribute("user");
        if (user == null) {
            user = getRealm().getUser((String) SecurityUtils.getSubject().getPrincipal());
            setSessionAttribute("user", user);
        }
        return ServerUtils.mapModel(user, User.class);
    }

    public static void requiresPerm(String perm) throws GwtUtilException {
        if (!hasPerm(perm))
            throw new ClientAuthorizationException("Not authorized [perm: " + perm + "]");
    }

    public static void requiresRole(String role) throws GwtUtilException {
        if (!hasRole(role))
            throw new ClientAuthorizationException("Not authorized [role: " + role + "]");
    }

    public static void setSessionAttribute(Object key, Object value) {
        SecurityUtils.getSubject().getSession().setAttribute(key, value);
    }

    public static UserDTO getUserByName(String username) throws GwtUtilException {
        return getRealm().getUser(username);
    }

    public static void startTest(String DBConfig) {
        HibernateUtil.initSessionFactory(DBConfig);
        Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
        SecurityManager sm = factory.getInstance();
        ThreadContext.bind(sm);
    }

    final transient RandomNumberGenerator rng = new SecureRandomNumberGenerator();

    /**
     * 
     */
    private static final long serialVersionUID = -5049525086987492554L;

    public static boolean registrationEnabled = false; // NOSONAR
    public static boolean rememberMeOverridesLogin = true; // NOSONAR

    @Override
    public String getUsername() {
        return (String) SecurityUtils.getSubject().getPrincipal();
    }

    @Override
    public List<String> getUserRoles() throws GwtUtilException {
        requiresAuth();
        return getRoles();
    }

    @Override
    public boolean isLoggedIn() {
        return isAuthenticated();
    }

    private static boolean isAuthenticated() {
        return SecurityUtils.getSubject().isAuthenticated() || (rememberMeOverridesLogin && SecurityUtils.getSubject().isRemembered());
    }

    @Override
    public boolean isRegistrationEnabled() {
        return registrationEnabled;
    }

    @Override
    public boolean login(final String username, String password, boolean remember) throws GwtUtilException {
        try {
            setMDCIP(false);
            boolean result = getRealm().login(username, password, remember);
            if (result) {
                logger.info("User \"{}\" logged in, {}remembered.", username, (remember ? "" : "not "));
                ServletConfig servletConfig = getServletConfig();
                if (servletConfig != null && "ireallywantthis".equals(servletConfig.getInitParameter("savePassword"))) {
                    getThreadLocalRequest().getSession().setAttribute("password", password);
                    logger.debug("Password for user {} stored in the session.", username);
                }
                return result;
            }
        } catch (ClientAuthException e) {
            logger.warn("User \"{}\" failed to login.", username);
            throw e;
        }
        return false;
    }

    @Override
    public void logout() throws GwtUtilException {
        setMDCIP(true);
        logger.info("User \"{}\" logged out.", SecurityUtils.getSubject().getPrincipal());
        removeSessionAttribute("userid");
        SecurityUtils.getSubject().logout();
    }

    @Override
    public Long register(final String username, final String password) throws GwtUtilException {
        if (!registrationEnabled) {
            return -1L;
        }
        return getRealm().register(username, password, rng);
    }

    private void setMDCIP(boolean setUser) throws GwtUtilException {
        HttpServletRequest req = getThreadLocalRequest();
        setMDCIP(req, setUser);
    }

    private static void setMDCIP(HttpServletRequest req, boolean setUser) throws GwtUtilException {
        if (req != null) {
            MDC.put("ip", req.getRemoteAddr());
            if (setUser) {
                try {
                    HttpSession session = req.getSession();
                    UserDTO userDTO = (UserDTO) session.getAttribute(USER_DTO);
                    if (userDTO == null) {
                        userDTO = getUserDTO();
                        session.setAttribute(USER_DTO, userDTO);
                    }
                    if (userDTO != null && userDTO.getUsername() != null) {
                        MDC.put("user", userDTO.getUsername());
                    } else {
                        MDC.remove("user");
                    }
                } catch (Exception e) {
                    MDC.remove("user");
                    logger.warn("Setting MDC IP failed:", e);
                }
            }
        } else {
            MDC.put("ip", "---");
        }
    }

    public static void setMDCIP(HttpServletRequest req) throws GwtUtilException {
        setMDCIP(req, true);
    }

    @Override
    public void destroy() {
        MDC.clear();
    }
}
