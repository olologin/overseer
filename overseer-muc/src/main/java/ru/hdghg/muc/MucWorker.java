package ru.hdghg.muc;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hdghg.jpa.HistoryManager;
import ru.hdghg.model.WorkerResult;
import ru.hdghg.muc.listeners.MessageListener;
import ru.hdghg.muc.listeners.StatusListener;

public class MucWorker {

    private static final Logger log = LoggerFactory.getLogger(MucWorker.class);

    private XMPPConnection xmppc;
    private MultiUserChat muc;
    private HistoryManager historyManager;

    public WorkerResult startSpy() {
        String xmppServer = "jabbim.cz";
        String xmppUsername = "overs33r";
        String xmppPassword = "rfrek.ltq";
        String xmppResource = "res";
        String mucRoom = "s@pyos.anoosdy.org";
        String mucName = "overseer";
        String mucPassword = "";
        SmackConfiguration.setDefaultPacketReplyTimeout(15000);
        ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration("conn443.netlab.cz", 443, xmppServer);

        connectionConfiguration.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        if (null != xmppc && xmppc.isConnected()) {
            log.error("Already connected");
            return new WorkerResult(false, "Already connected");
        }
        xmppc = new XMPPTCPConnection(connectionConfiguration);

        try {
            xmppc.connect();
            xmppc.login(xmppUsername, xmppPassword, xmppResource);
            muc = new MultiUserChat(xmppc, mucRoom);
            muc.join(mucName, mucPassword);
            MessageListener messageListener = new MessageListener();
            messageListener.setMultiUserChat(muc);
            messageListener.setHistoryManager(historyManager);
            muc.addMessageListener(messageListener);
            StatusListener statusListener = new StatusListener();
            statusListener.setHistoryManager(historyManager);
            statusListener.setMultiUserChat(muc);
            statusListener.setOccupants(muc.getOccupants());
            muc.addParticipantListener(statusListener);
        } catch (Exception ex) {
            log.error("error", ex);
            String message = ex.getMessage();
            if (null == message) {
                message = ex.getClass().getSimpleName();
            }
            return new WorkerResult(false, message);
        }
        return new WorkerResult(true, "Connected");
    }

    public WorkerResult stopSpy() {
        log.info("Worker stopped");
        try {
            if (null != muc) {
                muc.leave();
            }
            if (null != xmppc) {
                xmppc.disconnect();
            }
        } catch (SmackException.NotConnectedException e) {
            return new WorkerResult(false, "Not connected");
        } finally {
            xmppc = null;
        }
        return new WorkerResult(true, "Disconnected");
    }

    public void setHistoryManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }
}
