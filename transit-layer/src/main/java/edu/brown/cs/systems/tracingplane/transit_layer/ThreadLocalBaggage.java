package edu.brown.cs.systems.tracingplane.transit_layer;

/**
 * Implements storage of {@link Baggage} instances in a thread-local variable. Users should use the static methods in
 * {@link Baggage} to access the contents of the thread-local baggage.
 */
class ThreadLocalBaggage {

    private ThreadLocalBaggage() {}

    private static final ThreadLocal<Baggage> current = new ThreadLocal<Baggage>();

    /**
     * Get the Baggage instance, if any, stored for the current thread
     * 
     * @return a Baggage instance, possibly null
     */
    public static Baggage get() {
        return current.get();
    }

    /** Removes the baggage instance being stored in the current thread */
    public static void discard() {
        Baggage.discard(take());
    }

    /**
     * Removes and returns the baggage instance being stored in the current thread
     * 
     * @return the baggage instance being stored in the current thread
     */
    public static Baggage take() {
        Baggage currentBaggage = get();
        if (currentBaggage != null) {
            current.remove();
        }
        return currentBaggage;
    }

    /**
     * Set the Baggage instance for the current thread
     * 
     * @param baggage a Baggage instance, possibly null
     */
    public static void set(Baggage baggage) {
        discard();
        current.set(baggage);
    }

}
