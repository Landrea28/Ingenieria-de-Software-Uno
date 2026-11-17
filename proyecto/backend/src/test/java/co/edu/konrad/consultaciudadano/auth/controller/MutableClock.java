package co.edu.konrad.consultaciudadano.auth.controller;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

final class MutableClock extends Clock {
    private final AtomicReference<Instant> instant = new AtomicReference<>(Instant.now());

    void reset() {
        instant.set(Instant.now());
    }

    void advance(Duration duration) {
        instant.updateAndGet(actual -> actual.plus(duration));
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return instant.get();
    }
}
