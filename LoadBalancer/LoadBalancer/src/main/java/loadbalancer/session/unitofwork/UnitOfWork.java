package loadbalancer.session.unitofwork;

import loadbalancer.dbrequest.DbRequest;

public interface UnitOfWork {

    void register(DbRequest value) throws IllegalStateException;

    void commit() throws IllegalStateException;

}
