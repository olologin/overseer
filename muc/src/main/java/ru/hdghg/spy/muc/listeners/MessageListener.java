package ru.hdghg.spy.muc.listeners;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;
import ru.hdghg.spy.service.dao.Storage;
import ru.hdghg.spy.service.model.History;

import java.util.Date;

@Slf4j
public class MessageListener implements PacketListener{

    @Setter private Storage storage;
    @Setter private MultiUserChat multiUserChat;

    @Override
    public void processPacket(Packet packet) throws SmackException.NotConnectedException {
        if (!(packet instanceof Message)) {
            log.debug("Packet is not instance of Message. Skipping...");
            return;
        }
        String from = packet.getFrom();
        if (null == from || "".equals(from)) {
            log.debug("Failed to extract sender name from muc");
            return;
        }
        String jid = getJid(from);
        if (null == jid || "".equals(jid)) {
            log.debug("Failed to get jid by name");
            return;
        }
        History history = new History(jid, null, new Date());
        storage.save(history);
        log.info("Persisted {}", history);
    }

    private String getJid(String from) {
        Occupant o = multiUserChat.getOccupant(from);
        return null == o ? "" : o.getJid();
    }
}
