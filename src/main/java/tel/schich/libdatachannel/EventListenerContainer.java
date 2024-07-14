package tel.schich.libdatachannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class EventListenerContainer<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventListenerContainer.class);

    private final String eventName;
    private final Consumer<Boolean> lifecycleCallback;
    private final List<T> listeners;
    private final Lock changeLock;
    private final Executor executor;

    public EventListenerContainer(String eventName, Consumer<Boolean> lifecycleCallback, Executor executor) {
        this.eventName = eventName;
        this.lifecycleCallback = lifecycleCallback;
        this.listeners = new CopyOnWriteArrayList<>();
        this.changeLock = new ReentrantLock();
        this.executor = executor;
    }

    public String eventName() {
        return eventName;
    }

    void invoke(Consumer<T> invoker) {
        executor.execute(() -> {
            for (T listener : this.listeners) {
                try {
                    invoker.accept(listener);
                } catch (Throwable t) {
                    LOGGER.error("Handler for event {} failed!", eventName, t);
                }
            }
        });
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

    @Override
    public String toString() {
        return eventName;
    }
}
