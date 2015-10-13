package ru.hdghg.spy.service.dao;

import ru.hdghg.spy.service.model.History;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class Storage {

    @PersistenceContext
    private EntityManager em;

    public void save(History history) {
        em.persist(history);
    }
}
