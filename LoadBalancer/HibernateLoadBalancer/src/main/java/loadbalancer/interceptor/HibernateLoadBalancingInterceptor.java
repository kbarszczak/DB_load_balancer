package loadbalancer.interceptor;

import loadbalancer.LoadBalancer;
import org.hibernate.CallbackException;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.type.Type;

public class HibernateLoadBalancingInterceptor extends LoadBalancingInterceptor<Session> implements Interceptor {

    public HibernateLoadBalancingInterceptor(LoadBalancer<Session> loadBalancer) {
        super(loadBalancer);
    }

    @Override
    public boolean onSave(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) throws CallbackException {
        try {
            interceptOnSave(entity);
            return false;
        } catch (IllegalStateException exception) {
            throw new CallbackException(exception.getMessage());
        }
    }

    @Override
    public void onDelete(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) throws CallbackException {
        try {
            interceptOnDelete(entity);
        } catch (IllegalStateException exception) {
            throw new CallbackException(exception.getMessage());
        }
    }

    @Override
    public boolean onFlushDirty(Object entity, Object id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) throws CallbackException {
        try {
            interceptOnUpdate(entity);
            return false;
        } catch (IllegalStateException exception) {
            throw new CallbackException(exception.getMessage());
        }
    }
}
