package ru.hdghg.service;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

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

    @EJB
    private HistoryManager historyManager;

    @PostConstruct
    public void postConstruct() {
        MucWorker spyWorker = new MucWorker();
        spyWorker.setHistoryManager(historyManager);
        workers.add(spyWorker);
    }

    @Lock(LockType.WRITE)
    public List<WorkerResult> start() {
        List<WorkerResult> result = new LinkedList<>();
        log.info("Start called");
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
