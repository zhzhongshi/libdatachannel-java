package tel.schich.libdatachannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class EventListenerContainer<T> {
    private final Consumer<Boolean> lifecycleCallback;
    private final List<T> listeners;
    private final Lock changeLock;

    public EventListenerContainer(Consumer<Boolean> lifecycleCallback) {
        this.lifecycleCallback = lifecycleCallback;
        this.listeners = new CopyOnWriteArrayList<>();
        this.changeLock = new ReentrantLock();
    }

    void invoke(Consumer<T> invoker) {
        for (T listener : this.listeners) {
            invoker.accept(listener);
        }
    }

    public void register(T listener) {
        boolean wasEmpty;
        changeLock.lock();
        try {
            wasEmpty = listeners.isEmpty();
            listeners.add(listener);
        } finally {
            changeLock.unlock();
        }
        if (wasEmpty) {
            lifecycleCallback.accept(true);
        }
    }

    public void deregister(T listener) {
        boolean isNowEmpty;
        changeLock.lock();
        try {
            if (!listeners.remove(listener)) {
                return;
            }
            isNowEmpty = listeners.isEmpty();
        } finally {
            changeLock.unlock();
        }
        if (isNowEmpty) {
            lifecycleCallback.accept(false);
        }
    }
}
