package loadbalancer.session;

import jakarta.persistence.FlushModeType;
import loadbalancer.dbrequest.DbRequest;
import loadbalancer.interceptor.HibernateLoadBalancingInterceptor;
import logging.DBLogger;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;

import java.util.List;

public class HibernateLoadBalancingSession extends LoadBalancingSession<Session> {

    private static final String HEALTH_NATIVE_QUERY = "select 1";

    private final String config;
    private final HibernateLoadBalancingInterceptor interceptor;
    private Session interceptedSession;
    private Session session;

    public HibernateLoadBalancingSession(String config, HibernateLoadBalancingInterceptor interceptor, boolean isPrimarySession, String sessionName, int delayMs, boolean logging) throws Exception {
        super(sessionName, isPrimarySession, delayMs, logging);
        this.config = config;
        this.interceptor = interceptor;
        try{
            connect();
        }catch (Exception exception){
            if(logging) DBLogger.getLogger(getClass()).warning("[HIBERNATE SESSION '" + getConnectionName() + "'] Could not create connection. Details: " + exception.getMessage());
        }
        this.thread.start();
    }

    @Override
    public Object execute(DbRequest request) throws Exception {
        try {
            if (!session.getTransaction().isActive()) session.beginTransaction();
            switch (request.getType()) {
                case INSERT -> session.save(request.getObject());
                case UPDATE -> session.update(request.getObject());
                case DELETE -> session.delete(request.getObject());
                default -> throw new UnsupportedOperationException("Operation '" + request.getType() + "' is not supported");
            }
            session.getTransaction().commit();
            session.clear();
            return null;
        } catch (UnsupportedOperationException exception) {
            if (logging)
                DBLogger.getLogger(getClass()).warning("[HIBERNATE SESSION '" + getConnectionName() + "'] " + exception.getMessage());
            throw exception;
        } catch (Exception exception) {
            if (logging)
                DBLogger.getLogger(getClass()).warning("[HIBERNATE SESSION '" + getConnectionName() + "'] " + exception.getMessage());
            setStatus(Status.DOWN);
            register(request);
            throw exception;
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            if(session.isOpen()) session.setFlushMode(FlushModeType.COMMIT);
            List<Integer> resultFromSession = session.createNativeQuery(HEALTH_NATIVE_QUERY, Integer.class).list();
            session.setFlushMode(FlushModeType.AUTO);
            if(interceptedSession.isOpen()) interceptedSession.setFlushMode(FlushModeType.COMMIT);
            List<Integer> resultFromInterceptedSession = interceptedSession.createNativeQuery(HEALTH_NATIVE_QUERY, Integer.class).list();
            interceptedSession.setFlushMode(FlushModeType.AUTO);
            return resultFromSession.get(0) == 1 && resultFromInterceptedSession.get(0) == 1;
        } catch (Exception exception) {
            return false;
        }
    }

    @Override
    public Session getConnection() {
        return interceptedSession;
    }

    @Override
    public void fix() throws Exception {
        if(session != null){
            session.getSessionFactory().close();
            session.close();
        }

        if(interceptedSession != null){
            interceptedSession.getSessionFactory().close();
            interceptedSession.close();
        }

        connect();

        if (isHealthy()) commit();
        else throw new IllegalStateException("Could not fix the session");

        setStatus(Status.UP);
    }

    @Override
    public void close() throws Exception {
        try {
            commit();
        } catch (IllegalStateException ignore) {
        }
        super.close();
        session.getSessionFactory().close();
        session.close();
        interceptedSession.getSessionFactory().close();
        interceptedSession.close();
    }

    private void connect(){
        session = new Configuration().configure(config).buildSessionFactory().openSession();
        interceptedSession = new Configuration().configure(config).buildSessionFactory().withOptions().interceptor(interceptor).openSession();
    }
}
