package loadbalancer.session.monitor;

public interface Monitor<T> {

    void watch(T object) throws IllegalStateException;

}
