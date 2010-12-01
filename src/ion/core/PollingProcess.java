/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ion.core;


/**
 *
 * @author cmueller
 */
public abstract class PollingProcess extends ion.core.BaseProcess implements java.awt.event.ActionListener {

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
        System.out.println("PollingProcess:: Spawn");
        super.spawn();
        startPolling();
    }

    public void startPolling() {
        if (timer != null) {
            System.out.println("PollingProcess:: Start Polling");
            timer.start();
        }
    }

    public void stopPolling() {
        if (timer != null) {
            System.out.println("PollingProcess:: Stop Polling");
            timer.stop();
        }
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        try {
            System.out.println("PollingProcess:: Checking messages...");
            /* Stop polling the queue */
            stopPolling();
            ion.core.messaging.IonMessage message = this.receive();//this blocks if there are not any messages...
            if (message != null) {
                System.out.println("PollingProcess:: there's a message!");
                /* There's a message - send it to the messageReceived method */
                messageReceived(message);
            } else {
                /* This may only happen when a ShutdownSignalException is encountered within the BaseProcess.receive method... */
                System.out.println("PollingProcess:: no message");
            }
        } finally {
            /* Resume polling */
            startPolling();
        }
    }

    public abstract void messageReceived(ion.core.messaging.IonMessage msg);

    @Override
    public void dispose() {
        System.out.println("PollingProcess:: Dispose PollingProcess");
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
