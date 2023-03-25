package loadbalancer.interceptor;

import loadbalancer.LoadBalancer;

import java.sql.Connection;

public class JDBCLoadBalancingInterceptor extends LoadBalancingInterceptor<Connection> {

    public JDBCLoadBalancingInterceptor(LoadBalancer<Connection> loadBalancer) {
        super(loadBalancer);
    }

}
