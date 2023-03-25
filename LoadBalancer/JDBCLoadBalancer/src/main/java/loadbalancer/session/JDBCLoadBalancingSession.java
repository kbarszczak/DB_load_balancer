package loadbalancer.session;

import loadbalancer.dbrequest.DbRequest;

import java.sql.Connection;

public class JDBCLoadBalancingSession extends LoadBalancingSession<Connection> {

    public JDBCLoadBalancingSession(Connection connection, String connectionName, boolean isPrimaryConnection, int delayMs, boolean logging) {
        super(connectionName, isPrimaryConnection, delayMs, logging);
    }

    @Override
    public Connection getConnection() {
        return null;
    }

    @Override
    public Object execute(DbRequest request) throws Exception {
        return null;
    }

    @Override
    public boolean isHealthy() {
        return false;
    }

    @Override
    public void fix() throws Exception {

    }
}
