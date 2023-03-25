package loadbalancer;

import loadbalancer.loadbalancingmechanism.LoadBalancingMechanism;
import loadbalancer.loadbalancingmechanism.RoundRobin;
import loadbalancer.session.LoadBalancingSession;
import loadbalancer.dbrequest.DbRequest;
import logging.DBLogger;

import java.util.List;

public abstract class LoadBalancer<T> implements AutoCloseable {

    protected LoadBalancingMechanism<T> loadBalancingMechanism;
    protected List<LoadBalancingSession<T>> sessions;
    protected boolean logging;

    public LoadBalancer(LoadBalancingMechanism<T> loadBalancingMechanism, boolean logging) throws Exception {
        this.loadBalancingMechanism = loadBalancingMechanism;
        this.logging = logging;
    }

    public LoadBalancer() throws Exception {
        this(new RoundRobin<>(), true);
    }

    public void setLoadBalancingMechanism(LoadBalancingMechanism<T> loadBalancingMechanism) {
        this.loadBalancingMechanism = loadBalancingMechanism;
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
        for (LoadBalancingSession<T> session : sessions) {
            session.setLogging(logging);
        }
    }

    public boolean isLogging() {
        return logging;
    }

    public Object redirect(DbRequest request) throws IllegalStateException {
        if (request.getType() == DbRequest.Type.SELECT) {
            while (true) {
                try {
                    LoadBalancingSession<T> session = loadBalancingMechanism.get(sessions);
                    return session.execute(request);
                } catch (IllegalStateException exception) { // there is no valid session
                    if (logging)
                        DBLogger.getLogger(getClass()).warning("[LOAD BALANCER ROOT] " + exception.getMessage());
                    throw exception;
                } catch (Exception exception) { // another exception related with db occurred. Repeat until there is a valid session
                    if (logging)
                        DBLogger.getLogger(getClass()).warning("[LOAD BALANCER ROOT] " + exception.getMessage());
                }
            }
        }

        Object result = null;
        for (LoadBalancingSession<T> session : sessions) {
            try {
                result = session.execute(request);
            } catch (Exception exception) {
                if (logging) DBLogger.getLogger(getClass()).warning("[LOAD BALANCER ROOT] " + exception.getMessage());
            }
        }
        return result;
    }

    @Override
    public void close() throws Exception {
        for (LoadBalancingSession<T> session : sessions) session.close();
    }

    public abstract T connection();

}
