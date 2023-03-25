package loadbalancer.session;

import loadbalancer.session.monitor.SessionMonitorThread;
import loadbalancer.session.unitofwork.UnitOfWork;
import loadbalancer.dbrequest.DbRequest;
import logging.DBLogger;

import java.util.LinkedList;

public abstract class LoadBalancingSession<T> implements UnitOfWork, AutoCloseable {

    public enum Status {
        UP, DOWN
    }

    private final String connectionName;
    private boolean isPrimaryConnection;
    private Status status;
    private final LinkedList<DbRequest> queue;
    protected final SessionMonitorThread<T> thread;
    protected boolean logging;

    public LoadBalancingSession(String connectionName, boolean isPrimaryConnection, int delayMs, boolean logging) {
        this.connectionName = connectionName;
        this.isPrimaryConnection = isPrimaryConnection;
        this.status = Status.DOWN;
        this.queue = new LinkedList<>();
        this.thread = new SessionMonitorThread<>(this, delayMs);
        this.logging = logging;
        this.thread.setLogging(logging);
    }

    public String getConnectionName() {
        return connectionName;
    }

    public boolean isPrimaryConnection() {
        return isPrimaryConnection;
    }

    public void setPrimaryConnection(boolean primaryConnection) {
        isPrimaryConnection = primaryConnection;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isLogging() {
        return logging;
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
        this.thread.setLogging(logging);
    }

    @Override
    public void register(DbRequest value) throws IllegalStateException {
        queue.add(value);
    }

    @Override
    public void commit() throws IllegalStateException {
        if (logging)
            DBLogger.getLogger(getClass()).info("[SESSION '" + getConnectionName() + "'] Commit called with '" + queue.size() + "' DBRequests");
        while (!queue.isEmpty()) {
            DbRequest request = queue.remove();
            try {
                execute(request);
            } catch (Exception exception) {
                if (logging)
                    DBLogger.getLogger(getClass()).warning("[SESSION '" + getConnectionName() + "'] " + exception.getMessage());
                queue.push(request);
                throw new IllegalStateException("Could not execute request. Details: " + exception.getMessage());
            }
        }
    }

    @Override
    public void close() throws Exception {
        thread.disable();
        thread.join();
    }

    public abstract T getConnection();

    public abstract Object execute(DbRequest request) throws Exception;

    public abstract boolean isHealthy();

    public abstract void fix() throws Exception;
}
