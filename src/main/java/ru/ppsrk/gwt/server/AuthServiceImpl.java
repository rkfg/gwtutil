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

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.util.Factory;
import org.apache.shiro.util.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import ru.ppsrk.gwt.client.AuthService;
import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.ClientAuthorizationException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.domain.User;
import ru.ppsrk.gwt.dto.UserDTO;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class AuthServiceImpl extends RemoteServiceServlet implements AuthService {
    private static Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    public static GwtUtilRealm getRealm() throws LogicException {
        Iterator<Realm> realms = ((RealmSecurityManager) SecurityUtils.getSecurityManager()).getRealms().iterator();
        Realm realm = realms.next();
        if (!(realm instanceof GwtUtilRealm)) {
            throw new LogicException("Realm " + realm.getName() + " isn't compatible to GwtUtilRealm, its type is: "
                    + realm.getClass().getSimpleName());
        }
        return (GwtUtilRealm) realm;
    }

    public static List<String> getRoles() throws LogicException, ClientAuthException {
        return getRealm().getRoles(requiresAuthUser().getUsername());
    }

    public static UserDTO getUserDTO() throws LogicException, ClientAuthException {
        return getRealm().getUser((String) SecurityUtils.getSubject().getPrincipal());
    }

    public static Object getSessionAttribute(Object key) {
        return SecurityUtils.getSubject().getSession().getAttribute(key);
    }

    public static boolean hasPerm(String perm) {
        return SecurityUtils.getSubject().isPermitted(perm);
    }

    public static boolean hasRole(String role) throws LogicException, ClientAuthException {
        return SecurityUtils.getSubject().hasRole(role);
    }

    public static boolean hasRole(Long roleId) throws LogicException, ClientAuthException {
        return hasRole(getRealm().getRoleById(roleId));
    }

    public static void initShiro() {
        Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
        SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);
    }

    public static void removeSessionAttribute(Object key) {
        SecurityUtils.getSubject().getSession().removeAttribute(key);
    }

    public static Long requiresAuth() throws LogicException, ClientAuthException {
        User user = requiresAuthUser();
        if (user == null) {
            return 0L;
        } else {
            return user.getId();
        }
    }

    public static User requiresAuthUser() throws LogicException, ClientAuthException {
        if (!SecurityUtils.getSubject().isAuthenticated() && !SecurityUtils.getSubject().isRemembered())
            throw new ClientAuthenticationException("Not authenticated");
        UserDTO user = (UserDTO) getSessionAttribute("user");
        if (user == null) {
            user = getRealm().getUser((String) SecurityUtils.getSubject().getPrincipal());
            setSessionAttribute("user", user);
        }
        return ServerUtils.mapModel(user, User.class);
    }

    public static void requiresPerm(String perm) throws ClientAuthorizationException {
        if (!hasPerm(perm))
            throw new ClientAuthorizationException("Not authorized [perm: " + perm + "]");
    }

    public static void requiresRole(String role) throws LogicException, ClientAuthException {
        if (!hasRole(role))
            throw new ClientAuthorizationException("Not authorized [role: " + role + "]");
    }

    public static void setSessionAttribute(Object key, Object value) {
        SecurityUtils.getSubject().getSession().setAttribute(key, value);
    }

    public static UserDTO getUserByName(String username) throws LogicException, ClientAuthException {
        return getRealm().getUser(username);
    }

    public static void startTest(String DBConfig) {
        HibernateUtil.initSessionFactory(DBConfig);
        Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
        SecurityManager sm = factory.getInstance();
        ThreadContext.bind(sm);
    }

    RandomNumberGenerator rng = new SecureRandomNumberGenerator();

    /**
     * 
     */
    private static final long serialVersionUID = -5049525086987492554L;

    public static boolean registrationEnabled = false;

    @Override
    public String getUsername() {
        return (String) SecurityUtils.getSubject().getPrincipal();
    }

    @Override
    public List<String> getUserRoles() throws LogicException, ClientAuthException {
        requiresAuth();
        return getRoles();
    }

    @Override
    public boolean isLoggedIn() {
        return SecurityUtils.getSubject().isAuthenticated() || SecurityUtils.getSubject().isRemembered();
    }

    @Override
    public boolean isRegistrationEnabled() {
        return registrationEnabled;
    }

    @Override
    public boolean login(final String username, String password, boolean remember) throws LogicException, ClientAuthException {
        try {
            setMDCIP();
            boolean result = getRealm().login(username, password, remember);
            if (result) {
                logger.info("User \"{}\" logged in, {}remembered.", username, (remember ? "" : "not "));
                if ("ireallywantthis".equals(getServletConfig().getInitParameter("savePassword"))) {
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
    public void logout() {
        setMDCIP();
        logger.info("User \"{}\" logged out.", SecurityUtils.getSubject().getPrincipal());
        removeSessionAttribute("userid");
        SecurityUtils.getSubject().logout();
    }

    @Override
    public Long register(final String username, final String password) throws LogicException, ClientAuthException {
        if (!registrationEnabled) {
            return -1L;
        }
        return getRealm().register(username, password, rng);
    }

    private void setMDCIP() {
        HttpServletRequest req = getThreadLocalRequest();
        setMDCIP(req);
    }

    public static void setMDCIP(HttpServletRequest req) {
        if (req != null) {
            MDC.put("ip", req.getRemoteAddr());
        } else {
            MDC.put("ip", "---");
        }
    }

    @Override
    public void destroy() {
        MDC.clear();
    }
}
