package loadbalancer.loadbalancingmechanism;

import loadbalancer.session.LoadBalancingSession;

import java.util.List;

public class RoundRobin<T> implements LoadBalancingMechanism<T> {

    private int index;

    public RoundRobin() {
        this.index = -1;
    }

    @Override
    public LoadBalancingSession<T> get(List<LoadBalancingSession<T>> sessions) throws IllegalStateException {
        if (++index >= sessions.size()) index = 0;
        for (int i = 0; i < sessions.size(); ++i) {
            LoadBalancingSession<T> session = sessions.get(index);
            if (session.getStatus() == LoadBalancingSession.Status.UP && session.isHealthy()) {
                return session;
            }
            if (++index >= sessions.size()) index = 0;
        }
        throw new IllegalStateException("There is no active session");
    }

}
