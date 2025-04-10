import java.util.ArrayList;
import java.util.List;

public class Restaurant {
    private final OrderQueue orderQueue;
    private final List<Customer> customers = new ArrayList<>();
    private final List<Order> activeOrders = new ArrayList<>();
    private final EventLogger logger;

    public Restaurant(int maxOrders, EventLogger logger) {
        this.orderQueue = new OrderQueue(maxOrders);
        this.logger = logger;
    }

    public boolean placeOrder(Order order) {
        synchronized (activeOrders) {
            activeOrders.add(order);
        }
        return orderQueue.addOrder(order, logger);
    }

    public Order takeOrder(String specialization) throws InterruptedException {
        Order order = orderQueue.takeOrder(specialization, logger);
        return order;
    }

    public void completeOrder(Order order) {
        order.complete();
        synchronized (activeOrders) {
            activeOrders.remove(order);
        }
    }

    public void addCustomer(Customer customer) {
        customers.add(customer);
    }

    public int getHappyCustomers() {
        return (int) customers.stream().filter(Customer::isHappy).count();
    }

    public int getAngryCustomers() {
        return (int) customers.stream()
            .filter(customer -> !customer.isHappy() && customer.getState() == Thread.State.TERMINATED)
            .count();
    }

    public List<Order> getActiveOrders() {
        synchronized (activeOrders) {
            return new ArrayList<>(activeOrders);
        }
    }

    public int getActiveOrderCount() {
        synchronized (activeOrders) {
            return activeOrders.size();
        }
    }
}