package ru.hdghg.spy.muc;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;
import ru.hdghg.spy.muc.listeners.MessageListener;
import ru.hdghg.spy.muc.listeners.StatusListener;
import ru.hdghg.spy.service.SpyWorker;
import ru.hdghg.spy.service.WorkerResult;
import ru.hdghg.spy.service.dao.Storage;
import ru.hdghg.spy.service.model.History;

import java.util.Date;

@Slf4j
public class MucWorker implements SpyWorker {

    private XMPPConnection xmppc;
    private MultiUserChat muc;
    @Setter private Storage storage;

    @Override
    public WorkerResult startSpy() {
        storage.save(new History("dfdf", null, new Date()));
        String xmppServer = "adastra.re";
        String xmppUsername = "overseer";
        String xmppPassword = "rfrek.ltq";
        String xmppResource = "res";
        String mucRoom = "s@pyos.anoosdy.org";
        String mucName = "overseer";
        String mucPassword = "";
        ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration(xmppServer);
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
            messageListener.setStorage(storage);
            muc.addMessageListener(messageListener);
            StatusListener statusListener = new StatusListener();
            statusListener.setStorage(storage);
            statusListener.setMultiUserChat(muc);
            muc.addParticipantListener(statusListener);
        } catch (Exception ex) {
            log.error("error", ex);
            String message = ex.getMessage();
            if (null == message) {
                message = ex.getClass().getSimpleName();
            }
            return new WorkerResult(false, ex.getMessage());
        }
        return new WorkerResult(true, "Connected");
    }

    @Override
    public WorkerResult stopSpy() {
        log.info("Worker stopped");
        try {
            muc.leave();
            xmppc.disconnect();
        } catch (SmackException.NotConnectedException e) {
            return new WorkerResult(false, "Not connected");
        } finally {
            xmppc = null;
        }
        return new WorkerResult(true, "Disconnected");
    }
}
