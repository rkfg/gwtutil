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

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class AuthImpl extends RemoteServiceServlet implements Auth {
    RandomNumberGenerator rng = new SecureRandomNumberGenerator();

    /**
     * 
     */
    private static final long serialVersionUID = -5049525086987492554L;
    public static boolean registrationEnabled = false;

    @Override
    public boolean login(String username, String password) throws ClientAuthenticationException, ClientAuthorizationException {
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(new UsernamePasswordToken(username, password));
            if (subject.isAuthenticated()) {
                Session session = HibernateUtil.getSession();
                @SuppressWarnings("unchecked")
                List<User> user = session.createQuery("from User where username = :un").setParameter("un", username).list();
                HibernateUtil.endSession(session);
                subject.getSession().setAttribute("userid", user.get(0).getId());
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
    public boolean register(String username, String password) {
        if (!registrationEnabled) {
            return false;
        }
        ByteSource salt = rng.nextBytes();
        String hashedPasswordBase64 = new Sha256Hash(password, salt, 1024).toBase64();

        User user = new User(username, hashedPasswordBase64);
        // save the salt with the new account. The HashedCredentialsMatcher
        // will need it later when handling login attempts:
        user.setSalt(salt.toBase64());
        Session session = HibernateUtil.getSession();
        session.save(user);
        HibernateUtil.endSession(session);
        return true;
    }

    public static void requiresAuth() throws ClientAuthenticationException {
        if (!SecurityUtils.getSubject().isAuthenticated())
            throw new ClientAuthenticationException("Not authenticated");
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
        SecurityUtils.getSubject().getSession().removeAttribute("userid");
        SecurityUtils.getSubject().logout();
    }

    @Override
    public boolean isRegistrationEnabled() {
        return registrationEnabled;
    }

    public static List<String> getRoles() {
        Long userId = (Long) SecurityUtils.getSubject().getSession().getAttribute("userid");
        if (userId == null)
            return new ArrayList<String>();
        
        Session session = HibernateUtil.getSession();
        Set<Role> roles = ((User) session.get(User.class, userId)).getRoles();
        HibernateUtil.endSession(session);
        List<String> result = new ArrayList<String>();
        for (Role role : roles) {
            result.add(role.getRole());
        }
        return result;
    }
}
