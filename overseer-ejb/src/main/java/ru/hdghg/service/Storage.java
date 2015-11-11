package ru.hdghg.service;


import ru.hdghg.jpa.HistoryManager;
import ru.hdghg.model.History;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

@Stateless
public class Storage {

    @Inject
    private HistoryManager historyManager;

    public void save(History history) {
        historyManager.save(history);
    }

    public List<History> all() {
        return historyManager.all();
    }

}
