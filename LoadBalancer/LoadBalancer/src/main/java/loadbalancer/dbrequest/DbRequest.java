package loadbalancer.dbrequest;

public class DbRequest {

    public enum Type {
        SELECT, INSERT, DELETE, UPDATE
    }

    private final Object object;
    private final Type type;

    public DbRequest(Object object, Type type) {
        this.object = object;
        this.type = type;
    }

    public Object getObject() {
        return object;
    }

    public Type getType() {
        return type;
    }
}
