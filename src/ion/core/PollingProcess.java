/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ion.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author cmueller
 */
public abstract class PollingProcess extends ion.core.BaseProcess implements java.awt.event.ActionListener {
    
    private static final Logger log = LoggerFactory.getLogger(PollingProcess.class);

    private javax.swing.Timer timer;

    public PollingProcess(String host, String exchangeTopic, String sysName) {
        this(host, exchangeTopic, sysName, 1000);
    }

    public PollingProcess(String host, String exchangeTopic, String sysName, int pollingInterval) {
        super(host, com.rabbitmq.client.AMQP.PROTOCOL.PORT, exchangeTopic);
        mBrokerClient.attach();
        timer = new javax.swing.Timer(0, this);
        timer.setDelay(pollingInterval);
    }

    @Override
    public void spawn() {
        if (log.isDebugEnabled()) {
            log.debug("PollingProcess:: Spawn");
        }
        super.spawn();
        startPolling();
    }

    public void startPolling() {
        if (timer != null) {
            if (log.isDebugEnabled()) {
                log.debug("PollingProcess:: Start Polling");
            }
            timer.start();
        }
    }

    public void stopPolling() {
        if (timer != null) {
            if (log.isDebugEnabled()) {
                log.debug("PollingProcess:: Stop Polling");
            }
            timer.stop();
        }
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("PollingProcess:: Checking messages...");
            }
            /* Stop polling the queue */
            stopPolling();
            ion.core.messaging.IonMessage message = this.receive();//this blocks if there are not any messages...
            if (message != null) {
                if (log.isDebugEnabled()) {
                    log.debug("PollingProcess:: there's a message!");
                }
                /* There's a message - send it to the messageReceived method */
                messageReceived(message);
            } else {
                /* This may only happen when a ShutdownSignalException is encountered within the BaseProcess.receive method... */
                if (log.isDebugEnabled()) {
                    log.debug("PollingProcess:: no message");
                }
            }
        } finally {
            /* Resume polling */
            startPolling();
        }
    }

    public abstract void messageReceived(ion.core.messaging.IonMessage msg);

    @Override
    public void dispose() {
        if (log.isDebugEnabled()) {
            log.debug("PollingProcess:: Dispose PollingProcess");
        }
        stopPolling();
        timer = null;

        super.dispose();
    }

    @Override
    protected void finalize() throws Throwable {
        dispose();
        super.finalize();
    }
}
