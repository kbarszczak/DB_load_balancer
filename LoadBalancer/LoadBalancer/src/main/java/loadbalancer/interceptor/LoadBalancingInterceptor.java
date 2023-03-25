package loadbalancer.interceptor;

import loadbalancer.LoadBalancer;
import loadbalancer.dbrequest.DbRequest;

public abstract class LoadBalancingInterceptor<T> {

    protected final LoadBalancer<T> loadBalancer;

    public LoadBalancingInterceptor(LoadBalancer<T> loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    protected void interceptOnSave(Object object) throws IllegalStateException {
        loadBalancer.redirect(new DbRequest(object, DbRequest.Type.INSERT));
    }

    protected void interceptOnDelete(Object object) throws IllegalStateException {
        loadBalancer.redirect(new DbRequest(object, DbRequest.Type.DELETE));
    }

    protected void interceptOnUpdate(Object object) throws IllegalStateException {
        loadBalancer.redirect(new DbRequest(object, DbRequest.Type.UPDATE));
    }

    protected Object interceptOnLoad(Object object) throws IllegalStateException {
        return loadBalancer.redirect(new DbRequest(object, DbRequest.Type.SELECT));
    }

}
