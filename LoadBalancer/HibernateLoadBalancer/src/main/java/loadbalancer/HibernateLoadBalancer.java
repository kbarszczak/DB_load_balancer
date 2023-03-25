package loadbalancer;

import loadbalancer.dbrequest.DbRequest;
import loadbalancer.interceptor.HibernateLoadBalancingInterceptor;
import loadbalancer.session.HibernateLoadBalancingSession;
import loadbalancer.loadbalancingmechanism.LoadBalancingMechanism;
import loadbalancer.loadbalancingmechanism.RoundRobin;
import loadbalancer.session.LoadBalancingSession;
import logging.DBLogger;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;

public class HibernateLoadBalancer extends LoadBalancer<Session> {

    private LoadBalancingSession<Session> primarySession;

    public HibernateLoadBalancer(List<String> configs, LoadBalancingMechanism<Session> loadBalancingMechanism, boolean logging, int monitorDelayMs) throws Exception {
        // set up load balancing mechanism
        super(loadBalancingMechanism, logging);
        this.sessions = new ArrayList<>();
        this.primarySession = null;

        // create sessions
        int index = 1;
        HibernateLoadBalancingInterceptor interceptor = new HibernateLoadBalancingInterceptor(this);
        for (String config : configs) {
            sessions.add(new HibernateLoadBalancingSession(
                    config,
                    interceptor,
                    false,
                    "db_" + index,
                    monitorDelayMs,
                    logging
            ));
            ++index;
        }

        // verify load balancer state
        if (sessions.isEmpty()) throw new Exception("No valid session detected");
        if (logging)
            DBLogger.getLogger(getClass()).info("[HIBERNATE LOAD BALANCER ROOT] Created load balancer with '" + sessions.size() + "' sessions.");
    }

    public HibernateLoadBalancer(List<String> configs) throws Exception {
        this(configs, new RoundRobin<>(), true, 3000);
    }

    @Override
    public Object redirect(DbRequest request) throws IllegalStateException {
        if (!primarySession.isHealthy())
            throw new IllegalStateException("The session marked as the primary session is not healthy. Cannot execute any requests to not loose any data");

        Object result = null;
        for (LoadBalancingSession<Session> session : sessions) {
            try {
                if (session.isPrimaryConnection()) continue;
                result = session.execute(request);
            } catch (Exception ignore) {
            }
        }
        return result;
    }

    @Override
    public Session connection() {
        if (primarySession != null) primarySession.setPrimaryConnection(false);
        primarySession = loadBalancingMechanism.get(sessions);
        primarySession.setPrimaryConnection(true);
        primarySession.getConnection().clear();
        if (logging)
            DBLogger.getLogger(getClass()).info("[HIBERNATE LOAD BALANCER ROOT] Chosen session: '" + primarySession.getConnectionName() + "'");
        return primarySession.getConnection();
    }
}
