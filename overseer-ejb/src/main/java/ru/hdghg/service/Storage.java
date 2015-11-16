package ru.hdghg.service;


import ru.hdghg.jpa.HistoryManager;
import ru.hdghg.model.History;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;

@Stateless
public class Storage {

    @EJB
    private HistoryManager historyManager;

    public void save(History history) {
        historyManager.save(history);
    }

    public List<History> all() {
        return historyManager.all();
    }

    public List<String> allJids() {
        return historyManager.allJids();
    }

}
