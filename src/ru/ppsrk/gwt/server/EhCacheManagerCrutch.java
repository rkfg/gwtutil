package ru.ppsrk.gwt.server;

import net.sf.ehcache.CacheManager;

import org.apache.shiro.util.Factory;

public class EhCacheManagerCrutch implements Factory<CacheManager>{

    @Override
    public CacheManager getInstance() {
        return CacheManager.create();
    }

}
