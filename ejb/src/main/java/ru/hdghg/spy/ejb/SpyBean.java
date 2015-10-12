package ru.hdghg.spy.ejb;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;

@Startup
@Singleton
public class SpyBean {

    @Inject
    private Logger log;

    @Lock(LockType.WRITE)
    public boolean start() {
        log.info("Start called");
        return true;
    }

    @Lock(LockType.WRITE)
    public boolean stop() {
        log.info("Stop called");
        return true;
    }

}
