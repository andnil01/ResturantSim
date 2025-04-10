import java.util.LinkedList;
import java.util.Queue;

public class OrderQueue {
    private final Queue<Order> orders = new LinkedList<>();
    private final int maxOrders;

    public OrderQueue(int maxOrders) {
        this.maxOrders = maxOrders;
    }

    public synchronized boolean addOrder(Order order, EventLogger logger) {
        while (orders.size() >= maxOrders) {
            try {
                logger.log("Order queue is full, customer " + order.getCustomer().getName() + " is waiting.");
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        orders.add(order);
        logger.log("Order placed by " + order.getCustomer().getName() + " for " + order.getMeal().getName());
        notifyAll();
        return true;
    }

    public synchronized Order takeOrder(String cookSpecialization, EventLogger logger) throws InterruptedException {
        while (orders.isEmpty()) {
            logger.log("No orders available, cook waiting.");
            wait();
        }
        Order order = orders.poll(); // Tar alltid neste ordre
        if (order != null) {
            logger.log("Cook takes order: " + order.getMeal().getName());
            notifyAll();
            return order;
        }
        return null;
    }
}