package ru.hdghg.spy.muc.listeners;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;
import ru.hdghg.spy.service.dao.Storage;
import ru.hdghg.spy.service.model.History;

import java.util.Date;

@Slf4j
public class AsyncJidResolver implements Runnable {

    @Setter private Date date;
    @Setter private String status;
    @Setter private Storage storage;
    @Setter private MultiUserChat multiUserChat;
    @Setter private String from;

    @Override
    public void run() {
        String jid;
        int i = 10;
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("Resolver thread has been interrupted");
                return;
            }
            jid = getJid(from);
            i--;
        } while (null == jid && i > 0);
        if (null == jid) {
            log.warn("Failed to get jid of participant '{}'", from);
        } else {
            History history = new History(jid, status, date);
            storage.save(history);
            log.info("Asynchronously persisted status {}", history);
        }

    }

    private String getJid(String from) {
        Occupant o = multiUserChat.getOccupant(from);
        return null == o ? "" : o.getJid();
    }
}
