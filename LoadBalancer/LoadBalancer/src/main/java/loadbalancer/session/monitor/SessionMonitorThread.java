package loadbalancer.session.monitor;

import loadbalancer.session.LoadBalancingSession;
import logging.DBLogger;

public class SessionMonitorThread<T> extends Thread implements Monitor<LoadBalancingSession<T>> {

    private final LoadBalancingSession<T> session;
    private final long delayMs;
    private boolean running;
    private boolean logging;

    public SessionMonitorThread(LoadBalancingSession<T> session, long delayMs) {
        this.session = session;
        this.delayMs = delayMs;
        this.running = false;
    }

    public boolean isLogging() {
        return logging;
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
    }

    public void disable() {
        running = false;
        interrupt();
    }

    @Override
    public void run() {
        try {
            running = true;
            while (running) {
                watch(session);
                Thread.sleep(delayMs);
            }
        } catch (InterruptedException ignore) {
        }
    }

    @Override
    public void watch(LoadBalancingSession<T> object) throws IllegalStateException {
        try {
            if (session.isHealthy()) {
                if (session.getStatus() == LoadBalancingSession.Status.DOWN) session.commit();
                session.setStatus(LoadBalancingSession.Status.UP);
            } else {
                if (logging)
                    DBLogger.getLogger(getClass()).info("[SESSION '" + session.getConnectionName() + "']" + " Not healthy. Fix attempt");
                session.setStatus(LoadBalancingSession.Status.DOWN);
                session.fix();
            }
        } catch (Exception ignore) {
        }
    }
}
