package tel.schich.libdatachannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class EventListenerContainer<T> {
    private final Consumer<Boolean> onFirstListener;
    private final List<T> listeners;
    private final Lock changeLock;

    public EventListenerContainer(Consumer<Boolean> lifecycleCallback) {
        this.onFirstListener = lifecycleCallback;
        this.listeners = new ArrayList<>();
        this.changeLock = new ReentrantLock();
    }

    void invoke(Consumer<T> invoker) {
        for (T listener : this.listeners) {
            invoker.accept(listener);
        }
    }

    public void register(T listener) {
        changeLock.lock();
        boolean nowNonEmpty = false;
        try {
            nowNonEmpty = listeners.isEmpty();
            listeners.add(listener);
        } finally {
            changeLock.unlock();
        }
        if (nowNonEmpty) {
            onFirstListener.accept(true);
        }
    }

    public void deregister(T listener) {
        changeLock.lock();
        boolean nowEmpty = false;
        try {
            listeners.remove(listener);
            nowEmpty = listeners.isEmpty();
        } finally {
            changeLock.unlock();
        }
        if (nowEmpty) {
            onFirstListener.accept(false);
        }
    }
}
