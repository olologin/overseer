package ru.hdghg.service;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hdghg.jpa.HistoryManager;
import ru.hdghg.model.WorkerResult;
import ru.hdghg.muc.MucWorker;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Singleton
public class SpyBean {

    private Logger log = LoggerFactory.getLogger(SpyBean.class);

    private List<MucWorker> workers = new ArrayList<>();

    @PersistenceContext
    private EntityManager em;

    @Inject
    private HistoryManager historyManager;

    @PostConstruct
    public void postConstruct() {
        MucWorker mucWorker = new MucWorker();
        mucWorker.setHistoryManager(historyManager);
        workers.add(mucWorker);
    }

    @Lock(LockType.WRITE)
    public List<WorkerResult> start() {
        List<WorkerResult> result = new LinkedList<>();
        log.info("Start called");
        log.debug("Entity manager is {}", em);
        for (MucWorker worker : workers) {
            result.add(worker.startSpy());
        }
        return result;
    }

    @Lock(LockType.WRITE)
    public List<WorkerResult> stop() {
        List<WorkerResult> result = new LinkedList<>();
        log.info("Stop called");
        for (MucWorker worker : workers) {
            result.add(worker.stopSpy());
        }
        return result;
    }
}
