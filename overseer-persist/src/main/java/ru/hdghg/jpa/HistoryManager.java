package ru.hdghg.jpa;

import ru.hdghg.model.History;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Stateless
public class HistoryManager {

    @PersistenceContext
    private EntityManager em;

    public void save(History history) {
        em.persist(history);
    }

    public List<History> all() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<History> cq = cb.createQuery(History.class);
        Root<History> rootEntry = cq.from(History.class);
        CriteriaQuery<History> all = cq.select(rootEntry);
        TypedQuery<History> allQuery = em.createQuery(all);
        return allQuery.getResultList();
    }

    public List<String> allJids() {
        return em.createQuery("select distinct h.jid from History h " +
                "where jid != '' order by jid", String.class).getResultList();
    }
}
