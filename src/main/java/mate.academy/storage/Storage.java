package mate.academy.storage;

@FunctionalInterface
public interface Storage<J, K> {
    K get(J key);
}
