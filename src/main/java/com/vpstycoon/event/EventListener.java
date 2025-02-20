package com.vpstycoon.event;

@FunctionalInterface
public interface EventListener<T> {
    void onEvent(T event);
} 