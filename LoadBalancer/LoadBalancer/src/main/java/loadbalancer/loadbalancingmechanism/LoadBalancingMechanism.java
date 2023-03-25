package loadbalancer.loadbalancingmechanism;

import loadbalancer.session.LoadBalancingSession;

import java.util.List;

public interface LoadBalancingMechanism<T> {

    LoadBalancingSession<T> get(List<LoadBalancingSession<T>> sessions) throws IllegalStateException;

}
