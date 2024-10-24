package com.disposableemail.core.event.producer;

import com.disposableemail.core.event.Event;

public interface EventProducer {
    void send(Event<?> event);
}
