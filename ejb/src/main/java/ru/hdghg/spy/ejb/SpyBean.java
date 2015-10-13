package ru.hdghg.spy.ejb;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import lombok.extern.slf4j.Slf4j;
import ru.hdghg.spy.muc.MucWorker;
import ru.hdghg.spy.service.SpyWorker;
import ru.hdghg.spy.service.WorkerResult;
import ru.hdghg.spy.service.dao.Storage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Singleton
@Slf4j
public class SpyBean {

    private List<SpyWorker> workers = new ArrayList<>();

    @PersistenceContext
    private EntityManager em;

    @Inject
    private Storage storage;

    @PostConstruct
    public void postConstruct() {
        MucWorker mucWorker = new MucWorker();
        mucWorker.setStorage(storage);
        workers.add(mucWorker);
    }

    @Lock(LockType.WRITE)
    public List<WorkerResult> start() {
        List<WorkerResult> result = new LinkedList<>();
        log.info("Start called");
        log.debug("Entity manager is {}", em);
        for (SpyWorker worker : workers) {
            result.add(worker.startSpy());
        }
        return result;
    }

    @Lock(LockType.WRITE)
    public List<WorkerResult> stop() {
        List<WorkerResult> result = new LinkedList<>();
        log.info("Stop called");
        for (SpyWorker worker : workers) {
            result.add(worker.stopSpy());
        }
        return result;
    }
}
