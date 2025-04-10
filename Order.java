import java.util.concurrent.CountDownLatch;

public class Order {
    private final Customer customer;
    private final Meal meal;
    private final long creationTime;
    private final CountDownLatch latch;

    public Order(Customer customer, Meal meal) {
        this.customer = customer;
        this.meal = meal;
        this.creationTime = System.currentTimeMillis();
        this.latch = new CountDownLatch(1);
    }

    public boolean isCompleted() {
        return latch.getCount() == 0;
    }

    public Customer getCustomer() { return customer; }
    public Meal getMeal() { return meal; }
    public long getCreationTime() { return creationTime; }
    public void complete() { latch.countDown(); }
    public void awaitCompletion() throws InterruptedException { latch.await(); }
}