package ru.hdghg.muc.listeners;

import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hdghg.jpa.HistoryManager;
import ru.hdghg.model.History;

import java.util.Calendar;

public class AsyncJidResolver implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(AsyncJidResolver.class);

    private Calendar date;
    private String status;
    private HistoryManager historyManager;
    private MultiUserChat multiUserChat;
    private String from;

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
            historyManager.save(history);
            log.info("Asynchronously persisted status {}", history);
        }

    }

    private String getJid(String from) {
        Occupant o = multiUserChat.getOccupant(from);
        return null == o ? "" : o.getJid();
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMultiUserChat(MultiUserChat multiUserChat) {
        this.multiUserChat = multiUserChat;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setHistoryManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }
}
