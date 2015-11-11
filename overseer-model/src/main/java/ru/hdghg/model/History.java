package ru.hdghg.model;

import javax.persistence.*;
import java.util.Calendar;

@Entity
public class History {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jid;

    private String status;

    @Temporal(value = TemporalType.TIMESTAMP)
    private Calendar timestamp;

    public History() {}

    public History(String jid, String status, Calendar timestamp) {
        this.jid = jid;
        this.status = status;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Calendar getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Calendar timestamp) {
        this.timestamp = timestamp;
    }
}
