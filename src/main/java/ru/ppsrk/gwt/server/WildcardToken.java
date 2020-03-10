package ru.ppsrk.gwt.server;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * 
 * @author rkfg
 * This token always passes authentication, used for other users impersonation without password
 */

@SuppressWarnings("serial")
public class WildcardToken implements AuthenticationToken {

    private String principal;

    public WildcardToken(String principal) {
        this.principal = principal;
    }
    
    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public Object getCredentials() {
        return "NOPASSWD";
    }

}
