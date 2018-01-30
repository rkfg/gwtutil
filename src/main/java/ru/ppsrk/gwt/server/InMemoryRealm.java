package ru.ppsrk.gwt.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.crypto.RandomNumberGenerator;

import ru.ppsrk.gwt.client.GwtUtilException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.dto.UserDTO;

public class InMemoryRealm extends GwtUtilRealm {

    private Map<String, Set<String>> roles = new HashMap<>();
    private Map<String, UserDTO> users = new HashMap<>();
    private Long id = 0L;

    @Override
    public Long register(String username, String password, RandomNumberGenerator rng) throws GwtUtilException {
        HashedBase64Password base64Password = new HashedBase64Password(password, rng);
        UserDTO user = new UserDTO();
        user.setId(++id);
        user.setUsername(username);
        user.setPassword(base64Password.getPassword());
        user.setSalt(base64Password.getSalt());
        users.put(username, user);
        roles.put(username, new HashSet<>());
        return user.getId();
    }

    @Override
    public Set<String> getRoles(String principal) throws GwtUtilException {
        return roles.get(principal);
    }

    @Override
    public Set<String> getPerms(String principal) throws GwtUtilException {
        return new HashSet<>();
    }

    @Override
    public UserDTO getUser(String principal) throws GwtUtilException {
        return users.get(principal);
    }

    @Override
    protected String getRoleId(Long roleId) throws GwtUtilException {
        throw new LogicException("Unsupported");
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        Object principal = token.getPrincipal();
        UserDTO user = users.get(principal);
        return verify(token, user.getUsername(), user.getPassword(), user.getSalt());
    }

}
