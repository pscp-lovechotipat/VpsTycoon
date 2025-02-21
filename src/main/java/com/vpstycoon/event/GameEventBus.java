package com.vpstycoon.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameEventBus {
    private static final GameEventBus instance = new GameEventBus();
    private final Map<Class<?>, List<EventListener<?>>> listeners = new ConcurrentHashMap<>();

    private GameEventBus() {}

    public static GameEventBus getInstance() {
        return instance;
    }

    public <T> void subscribe(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(listener);
    }

    public <T> void unsubscribe(Class<T> eventType, EventListener<T> listener) {
        List<EventListener<?>> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove(listener);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void publish(T event) {
        List<EventListener<?>> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            eventListeners.forEach(listener -> 
                ((EventListener<T>) listener).onEvent(event)
            );
        }
    }
} 