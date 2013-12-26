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
package ru.ppsrk.gwt.client;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("AuthService")
public interface AuthService extends RemoteService {
    /**
     * Utility class for simplifying access to the instance of async service.
     */
    public static class Util {
        private static AuthServiceAsync instance;

        public static AuthServiceAsync getInstance() {
            if (instance == null) {
                instance = GWT.create(AuthService.class);
            }
            return instance;
        }
    }

    public boolean isRegistrationEnabled();

    public boolean login(String username, String password, boolean remember) throws LogicException, ClientAuthException;

    public void logout();

    public boolean isLoggedIn();

    public Long register(String username, String password) throws LogicException, ClientAuthenticationException;

    public List<String> getUserRoles() throws ClientAuthenticationException, LogicException;

    public String getUsername();
}
