package ru.hdghg.spy.ejb;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hdghg.spy.service.SpyWorker;

@Singleton
public class SpyBean {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Inject @Any
    private Instance<SpyWorker> workers;

    @Lock(LockType.WRITE)
    public boolean start() {
        log.info("Start called");
        for (SpyWorker worker : workers) {
            worker.startSpy();
        }
        return true;
    }

    @Lock(LockType.WRITE)
    public boolean stop() {
        log.info("Stop called");
        for (SpyWorker worker : workers) {
            worker.stopSpy();
        }
        return true;
    }
}
