package ru.hdghg.muc.listeners;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hdghg.jpa.HistoryManager;
import ru.hdghg.model.History;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class StatusListener implements PacketListener {

    private static final Logger log = LoggerFactory.getLogger(StatusListener.class);

    private MultiUserChat multiUserChat;
    private HistoryManager historyManager;
    private Map<String, String> occupantsJids = new HashMap<>();

    public void setOccupants(Iterable<String> occupants) {
        occupantsJids.clear();
        for (String occupant: occupants) {
            occupantsJids.put(occupant, getJid(occupant));
        }
    }

    @Override
    public void processPacket(Packet packet) throws SmackException.NotConnectedException {
        if (!(packet instanceof Presence)) {
            log.error("Not an instance of presence");
            return;
        }
        Presence presence = (Presence) packet;
        String from = packet.getFrom();
        if (null == from || "".equals(from)) {
            log.debug("Failed to extract sender name from muc");
            return;
        }
        Presence.Mode mode = presence.getMode();
        String status = null != mode ? mode.toString() : presence.getType().toString();

        String jid = getJid(from);
        if (null == jid) {
            log.debug("Can not get jid directly, trying another ways");
            if ("available".equals(status)) {
                AsyncJidResolver asyncJidResolver = new AsyncJidResolver();
                asyncJidResolver.setDate(Calendar.getInstance());
                asyncJidResolver.setFrom(from);
                asyncJidResolver.setMultiUserChat(multiUserChat);
                asyncJidResolver.setStatus(status);
                asyncJidResolver.setHistoryManager(historyManager);
                new Thread(asyncJidResolver).start();
                return;
            } else {
                jid = occupantsJids.get(from);
                if (null == jid || "".equals(jid)) {
                    log.debug("Failed to get jid by name: {}", from);
                    return;
                }
            }
        }
        History history = new History(jid, status, Calendar.getInstance());
        historyManager.save(history);
        log.info("Persisted status {}", history);
        occupantsJids.put(from, jid);
    }

    private String getJid(String from) {
        Occupant o = multiUserChat.getOccupant(from);
        return null == o ? "" : o.getJid();
    }

    public void setMultiUserChat(MultiUserChat multiUserChat) {
        this.multiUserChat = multiUserChat;
    }

    public void setHistoryManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

}
