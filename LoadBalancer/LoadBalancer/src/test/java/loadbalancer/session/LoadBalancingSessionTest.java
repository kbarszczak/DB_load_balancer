package loadbalancer.session;

import loadbalancer.dbrequest.DbRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Semaphore;

import static org.junit.jupiter.api.Assertions.*;

class LoadBalancingSessionTest {

    private LoadBalancingSession<Integer> session;
    private int value;
    private boolean started;

    @BeforeEach
    void setUp(){
        try {
            started = false;
            session = new LoadBalancingSession<>("test", true, 10,false) {
                @Override
                public Integer getConnection() {
                    return value;
                }

                @Override
                public Object execute(DbRequest request) throws Exception {
                    int v = (int)request.getObject();
                    value = getConnection() - v;
                    if(value < 0) throw new Exception();
                    return value;
                }

                @Override
                public boolean isHealthy() {
                    return value > 0;
                }

                @Override
                public void fix() throws Exception {
                    if(!started){
                        thread.start();
                        started = true;
                    }
                    value = 10;
                }
            };
            session.fix();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown(){
        try{
            session.close();
        }catch (Exception ignore){}
    }

    @Test
    void registerAndCommit() {
        session.register(new DbRequest(2, DbRequest.Type.SELECT));
        assertThrows(Exception.class, () -> session.execute(new DbRequest(15, DbRequest.Type.SELECT)));
        Integer []buffer = new Integer[1];
        buffer[0] = null;
        assertDoesNotThrow(() -> {
            while(!session.isHealthy()){
                Thread.sleep(10);
            }
            Thread.sleep(100);
            buffer[0] = session.getConnection();
        });

        assertNotNull(buffer[0]);
        assertEquals(8, buffer[0]);
    }
}