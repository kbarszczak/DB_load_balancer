package loadbalancer.session.monitor;

import loadbalancer.dbrequest.DbRequest;
import loadbalancer.session.LoadBalancingSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionMonitorThreadTest {

    private LoadBalancingSession<Integer> session;
    private SessionMonitorThread<Integer> monitor;
    private int value;

    @BeforeEach
    void setUp() {
        value = 0;
        session = new LoadBalancingSession<>("", false, 1, false) {
            @Override
            public Integer getConnection() {
                return value;
            }

            @Override
            public Object execute(DbRequest request) throws Exception {
                value = (int) request.getObject();
                return value;
            }

            @Override
            public boolean isHealthy() {
                return value != 0;
            }

            @Override
            public void fix() throws Exception {
                value = 1;
            }
        };
        monitor = new SessionMonitorThread<>(session, 10);
        monitor.start();
    }

    @AfterEach
    void tearDown() {
        try{
            monitor.disable();
            monitor.join();
            session.close();
        } catch (Exception ignore){}
    }

    @Test
    void watch() {
        Integer []buffer = new Integer[3];
        buffer[0] = null;
        buffer[1] = null;
        buffer[2] = null;

        assertDoesNotThrow(() -> {
            Thread.sleep(20);
            buffer[0] = session.getConnection();
            session.execute(new DbRequest(0, DbRequest.Type.SELECT));
            Thread.sleep(20);
            buffer[1] = session.getConnection();
            session.execute(new DbRequest(0, DbRequest.Type.DELETE));
            Thread.sleep(20);
            buffer[2] = session.getConnection();
        });

        assertNotNull(buffer[0]);
        assertNotNull(buffer[1]);
        assertNotNull(buffer[2]);
        assertEquals(1, buffer[0]);
        assertEquals(1, buffer[1]);
        assertEquals(1, buffer[2]);
    }
}