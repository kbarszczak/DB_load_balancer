package loadbalancer.loadbalancingmechanism;

import loadbalancer.session.LoadBalancingSession;

import java.util.List;
import java.util.Random;

public class RandConnection<T> implements LoadBalancingMechanism<T> {

    private static final Random RANDOM = new Random();
    public static int ATTEMPTS_BEFORE_EXCEPTION = 10;

    @Override
    public LoadBalancingSession<T> get(List<LoadBalancingSession<T>> loadBalancingSessions) throws IllegalStateException {
        int attempt = ATTEMPTS_BEFORE_EXCEPTION;
        do {
            LoadBalancingSession<T> session = loadBalancingSessions.get(RANDOM.nextInt(0, loadBalancingSessions.size()));
            if (session.getStatus() == LoadBalancingSession.Status.UP && session.isHealthy()) {
                return session;
            }
        } while (attempt-- > 0);
        throw new IllegalStateException("There is no active session");
    }
}
