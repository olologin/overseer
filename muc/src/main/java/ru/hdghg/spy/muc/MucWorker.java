package ru.hdghg.spy.muc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hdghg.spy.service.SpyWorker;

import javax.ejb.Stateful;

@Stateful
public class MucWorker implements SpyWorker {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public boolean startSpy() {
        log.info("Worker started");
        return true;
    }

    @Override
    public boolean stopSpy() {
        log.info("Worker stopped");
        return false;
    }
}
