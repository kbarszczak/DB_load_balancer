package loadbalancer;

import loadbalancer.loadbalancingmechanism.LoadBalancingMechanism;

import java.sql.Connection;

public class JDBCLoadBalancer extends LoadBalancer<Connection>{

    public JDBCLoadBalancer(LoadBalancingMechanism<Connection> loadBalancingMechanism, boolean logging) throws Exception {
        super(loadBalancingMechanism, logging);
    }

    @Override
    public Connection connection() {
        return null;
    }

    @Override
    public void close() throws Exception {

    }
}
