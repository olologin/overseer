package ru.hdghg.spy.service.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
public class History {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jid;

    private String status;

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date timestamp;

    public History() {}

    public History(String jid, String status, Date timestamp) {
        this.jid = jid;
        this.status = status;
        this.timestamp = timestamp;
    }
}
