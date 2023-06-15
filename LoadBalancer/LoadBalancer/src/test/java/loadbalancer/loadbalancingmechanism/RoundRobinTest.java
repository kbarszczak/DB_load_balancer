package loadbalancer.loadbalancingmechanism;

import loadbalancer.dbrequest.DbRequest;
import loadbalancer.session.LoadBalancingSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoundRobinTest {

    private RoundRobin<Integer> roundRobin;
    private List<LoadBalancingSession<Integer>> sessions;
    private boolean healthy;

    @BeforeEach
    void setUp() {
        roundRobin = new RoundRobin<>();
        healthy = false;
        sessions = List.of(
                new LoadBalancingSession<Integer>("", false, 1, false) {
                    @Override
                    public Integer getConnection() {
                        return 1;
                    }

                    @Override
                    public Object execute(DbRequest request) throws Exception {
                        return 1;
                    }

                    @Override
                    public boolean isHealthy() {
                        return true;
                    }

                    @Override
                    public void fix() throws Exception {}
                },
                new LoadBalancingSession<Integer>("", false, 1, false) {
                    @Override
                    public Integer getConnection() {
                        return 2;
                    }

                    @Override
                    public Object execute(DbRequest request) throws Exception {
                        return 2;
                    }

                    @Override
                    public boolean isHealthy() {
                        return healthy;
                    }

                    @Override
                    public void fix() throws Exception {}
                },
                new LoadBalancingSession<Integer>("", false, 1, false) {
                    @Override
                    public Integer getConnection() {
                        return 3;
                    }

                    @Override
                    public Object execute(DbRequest request) throws Exception {
                        return 3;
                    }

                    @Override
                    public boolean isHealthy() {
                        return true;
                    }

                    @Override
                    public void fix() throws Exception {}
                }
        );
        sessions.forEach(x -> x.setStatus(LoadBalancingSession.Status.UP));
    }

    @Test
    void get() {
        assertEquals(1, roundRobin.get(sessions).getConnection());
        assertEquals(3, roundRobin.get(sessions).getConnection());
        assertEquals(1, roundRobin.get(sessions).getConnection());
        assertEquals(3, roundRobin.get(sessions).getConnection());
        healthy = true;
        assertEquals(1, roundRobin.get(sessions).getConnection());
        assertEquals(2, roundRobin.get(sessions).getConnection());
        assertEquals(3, roundRobin.get(sessions).getConnection());
        assertEquals(1, roundRobin.get(sessions).getConnection());
        assertEquals(2, roundRobin.get(sessions).getConnection());
        assertEquals(3, roundRobin.get(sessions).getConnection());
    }
}