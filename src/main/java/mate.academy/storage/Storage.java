package mate.academy.storage;

public interface Storage<J, K> {
    K get(J key);

    boolean isPresent(J key);
}
