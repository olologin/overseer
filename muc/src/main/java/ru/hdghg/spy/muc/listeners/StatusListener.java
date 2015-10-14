package ru.hdghg.spy.muc.listeners;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;
import ru.hdghg.spy.service.dao.Storage;
import ru.hdghg.spy.service.model.History;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class StatusListener implements PacketListener {

    @Setter private MultiUserChat multiUserChat;
    @Setter private Storage storage;
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
                asyncJidResolver.setDate(new Date());
                asyncJidResolver.setFrom(from);
                asyncJidResolver.setMultiUserChat(multiUserChat);
                asyncJidResolver.setStatus(status);
                asyncJidResolver.setStorage(storage);
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
        History history = new History(jid, status, new Date());
        storage.save(history);
        log.info("Persisted status {}", history);
        occupantsJids.put(from, jid);
    }

    private String getJid(String from) {
        Occupant o = multiUserChat.getOccupant(from);
        return null == o ? "" : o.getJid();
    }

}
