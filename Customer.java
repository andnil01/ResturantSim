public class Customer extends Thread {
    private final String name;
    private final Restaurant restaurant;
    private final long maxWaitTime;
    private final EventLogger logger;
    private boolean isHappy = false;
    private java.util.function.Consumer<Order> orderAbandonedListener;

    public Customer(String name, Restaurant restaurant, long maxWaitTime, EventLogger logger) {
        this.name = name;
        this.restaurant = restaurant;
        this.maxWaitTime = maxWaitTime;
        this.logger = logger;
        this.setName(name);
    }

    public void addOrderAbandonedListener(java.util.function.Consumer<Order> listener) {
        this.orderAbandonedListener = listener;
    }

    @Override
    public void run() {
        Order order = new Order(this, Meal.values()[(int)(Math.random() * Meal.values().length)]);
        if (restaurant.placeOrder(order)) {
            try {
                long startTime = System.currentTimeMillis();
                order.awaitCompletion();
                long waitTime = System.currentTimeMillis() - startTime;
                isHappy = waitTime <= maxWaitTime;
                if (isHappy) {
                    logger.log(name + " received their meal and is happy!");
                } else {
                    logger.log(name + " waited too long (" + waitTime + "ms) and is unhappy!");
                }
            } catch (InterruptedException e) {
                logger.log(name + " gave up waiting and left unhappy!");
                isHappy = false;
                restaurant.abandonOrder(order); // Fjern fra activeOrders
                if (orderAbandonedListener != null) {
                    orderAbandonedListener.accept(order); // Oppdater status
                }
                Thread.currentThread().interrupt();
            }
        }
    }

    public String getCustomerName() {
        return name;
    }

    public boolean isHappy() {
        return isHappy;
    }
}