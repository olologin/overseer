package ru.hdghg.muc.listeners;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hdghg.jpa.HistoryManager;
import ru.hdghg.model.History;

import java.util.Calendar;

public class MessageListener implements PacketListener {

    private static final Logger log = LoggerFactory.getLogger(MessageListener.class);

    private HistoryManager historyManager;
    private MultiUserChat multiUserChat;

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
            log.debug("Failed to get jid by name: {}", from);
            return;
        }
        History history = new History(jid, null, Calendar.getInstance());
        historyManager.save(history);
        log.info("Persisted message {}", history);
    }

    private String getJid(String from) {
        Occupant o = multiUserChat.getOccupant(from);
        return null == o ? "" : o.getJid();
    }

    public void setHistoryManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    public void setMultiUserChat(MultiUserChat multiUserChat) {
        this.multiUserChat = multiUserChat;
    }
}
