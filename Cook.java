import java.util.function.Consumer;

public class Cook extends Thread {
    private final String name;
    private final Restaurant restaurant;
    private final EventLogger logger;
    private Consumer<Order> orderTakenListener;

    public Cook(String name, Restaurant restaurant, EventLogger logger) {
        super(name);
        this.name = name;
        this.restaurant = restaurant;
        this.logger = logger;
    }

    public void addOrderTakenListener(Consumer<Order> listener) {
        this.orderTakenListener = listener;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Hent en ordre fra køen
                Order order = restaurant.takeOrder(null); // Ingen spesialisering
                if (order != null) {
                    if (orderTakenListener != null) {
                        orderTakenListener.accept(order); // Varsle UI om "Preparing"
                    }
                    logger.log(name + " is preparing " + order.getMeal().getName());

                    // Bruk tilberedningstiden fra måltidet
                    long preparationTime = order.getMeal().getPreparationTime();
                    Thread.sleep(preparationTime);

                    // Fullfør ordren
                    restaurant.completeOrder(order);
                    logger.log(name + " completed " + order.getMeal().getName());
                } else {
                    // Ingen ordre tilgjengelig, vent litt
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                logger.log(name + " was interrupted.");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}