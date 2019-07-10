package ua.skillsup.practice;

import java.util.concurrent.atomic.AtomicLong;

/**
 * TODO
 */
public class IdGenerationService {
    private static final AtomicLong ID = new AtomicLong(0);

    /**
     * TODO
     *
     * @return
     */
    public long getNext() {
        return ID.getAndIncrement();
    }
}
