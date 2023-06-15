package loadbalancer;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import loadbalancer.loadbalancingmechanism.RoundRobin;
import loadbalancer.session.LoadBalancingSession;
import model.TestUser;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HibernateLoadBalancerTest {

    private static final int TIMEOUT = 5000;
    private HibernateLoadBalancer loadBalancer;

    @BeforeEach
    void setUp() throws Exception {
        loadBalancer = new HibernateLoadBalancer(List.of(
                "hibernate-h2-1.cfg.xml",
                "hibernate-h2-2.cfg.xml",
                "hibernate-h2-3.cfg.xml"
        ), new RoundRobin<>(), true, 50);
    }

    @AfterEach
    void tearDown() throws Exception {
        loadBalancer.close();
    }

    @Test
    void executeWhenConnectionsAliveInsert() {
        // test add & select
        TestUser user1 = new TestUser("Kamil123");
        Session connection = loadBalancer.connection();
        connection.beginTransaction();
        connection.persist(user1);
        connection.getTransaction().commit();

        loadBalancer.sessions.forEach(x -> {
            Session s = x.getConnection();

            CriteriaBuilder cb = s.getCriteriaBuilder();
            CriteriaQuery<TestUser> cq = cb.createQuery(TestUser.class);
            Root<TestUser> rootEntry = cq.from(TestUser.class);
            CriteriaQuery<TestUser> all = cq.select(rootEntry);

            TypedQuery<TestUser> allQuery = s.createQuery(all);
            List<TestUser> users = allQuery.getResultList();

            assertEquals(1, users.size());
            assertEquals("Kamil123", users.get(0).getName());
        });
    }

    @Test
    void executeWhenConnectionsAliveUpdate() {
        // test update & select
        TestUser user1 = new TestUser("Kamil123");
        Session connection = loadBalancer.connection();
        connection.beginTransaction();
        connection.persist(user1);
        connection.getTransaction().commit();

        Query<TestUser> query = connection.createQuery("from TestUser where name = ?1", TestUser.class);
        query.setParameter(1, "Kamil123");
        TestUser user = query.uniqueResult();
        assertNotNull(user);

        connection.beginTransaction();
        user.setName("KAMIL321");
        connection.merge(user);
        connection.getTransaction().commit();

        loadBalancer.sessions.forEach(x -> {
            Session s = x.getConnection();

            CriteriaBuilder cb = s.getCriteriaBuilder();
            CriteriaQuery<TestUser> cq = cb.createQuery(TestUser.class);
            Root<TestUser> rootEntry = cq.from(TestUser.class);
            CriteriaQuery<TestUser> all = cq.select(rootEntry);

            TypedQuery<TestUser> allQuery = s.createQuery(all);
            List<TestUser> users = allQuery.getResultList();

            assertEquals(1, users.size());
            assertEquals("KAMIL321", users.get(0).getName());
        });
    }

    @Test
    void executeWhenConnectionsAliveDelete() {
        // test update & select
        TestUser user1 = new TestUser("Kamil123");
        Session connection = loadBalancer.connection();
        connection.beginTransaction();
        connection.persist(user1);
        connection.getTransaction().commit();

        Query<TestUser> query = connection.createQuery("from TestUser where name = ?1", TestUser.class);
        query.setParameter(1, "Kamil123");
        TestUser user = query.uniqueResult();
        assertNotNull(user);

        connection.beginTransaction();
        connection.remove(user);
        connection.getTransaction().commit();

        loadBalancer.sessions.forEach(x -> {
            Session s = x.getConnection();

            CriteriaBuilder cb = s.getCriteriaBuilder();
            CriteriaQuery<TestUser> cq = cb.createQuery(TestUser.class);
            Root<TestUser> rootEntry = cq.from(TestUser.class);
            CriteriaQuery<TestUser> all = cq.select(rootEntry);

            TypedQuery<TestUser> allQuery = s.createQuery(all);
            List<TestUser> users = allQuery.getResultList();
            assertEquals(0, users.size());
        });
    }

    @Test
    void executeWhenFewConnectionsDead() {
        try{
            // test add & select when all
            Session session = loadBalancer.connection();
            TestUser user1 = new TestUser("Kamil123");


            loadBalancer.sessions.get(2).getConnection().close();
            assertFalse(loadBalancer.sessions.get(2).isHealthy());
            session.beginTransaction();
            session.persist(user1);
            session.getTransaction().commit();

            Thread.sleep(500);

            loadBalancer.sessions.forEach(x -> {
                Session s = x.getConnection();

                CriteriaBuilder cb = s.getCriteriaBuilder();
                CriteriaQuery<TestUser> cq = cb.createQuery(TestUser.class);
                Root<TestUser> rootEntry = cq.from(TestUser.class);
                CriteriaQuery<TestUser> all = cq.select(rootEntry);

                TypedQuery<TestUser> allQuery = s.createQuery(all);
                List<TestUser> users = allQuery.getResultList();

                assertEquals(1, users.size());
                assertEquals("Kamil123", users.get(0).getName());
            });

        }catch (InterruptedException ignore){}
    }

    @Test
    void isHealthyAndFix() {
        try{
            loadBalancer.sessions.forEach(x -> {
                assertEquals(LoadBalancingSession.Status.UP, x.getStatus());
                assertTrue(x.isHealthy());
                assertFalse(x.isPrimaryConnection());
            });

            // interrupt the connection
            assertDoesNotThrow(() -> loadBalancer.sessions.get(0).getConnection().close());

            // it should be dead now
            assertFalse(loadBalancer.sessions.get(0).isHealthy());

            // give time for the connection to restore
            int buf = TIMEOUT / 10;
            boolean result;
            while (buf-- > 0){
                result = loadBalancer.sessions.get(0).isHealthy();
                if(result) break;
                Thread.sleep(10);
            }

            // check if the connection was restored
            assertTrue(loadBalancer.sessions.get(0).isHealthy());
        } catch (InterruptedException ignore) {}
    }

    @Test
    void connectionAndGetConnection() {
        final Session session1 = loadBalancer.connection();
        loadBalancer.sessions.forEach(x -> {
            if(x.getConnection() != session1){
                assertFalse(x.isPrimaryConnection());
            } else{
                assertTrue(x.isPrimaryConnection());
            }
        });

        assertSame(loadBalancer.sessions.get(0).getConnection(), session1);

        loadBalancer.sessions.get(1).getConnection().close();
        final Session session3 = loadBalancer.connection();
        assertSame(loadBalancer.sessions.get(2).getConnection(), session3);
    }
}