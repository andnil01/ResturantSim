import java.util.Random;

public class Customer extends Thread {
    private final String name;
    private final Restaurant restaurant;
    private final long maxWaitTime; // Maksimal ventetid før kunden blir sint
    private boolean isHappy = true;
    private boolean statusFinalized = false; // Sikrer at statusen ikke endres
    private final EventLogger logger;

    public Customer(String name, Restaurant restaurant, long maxWaitTime ,EventLogger logger) {
        super(name); // Setter trådens navn
        this.name = name;
        this.restaurant = restaurant;
        this.maxWaitTime = maxWaitTime;
        this.logger = logger;
    }

    public boolean isHappy() {
        return isHappy;
    }

    @Override
    public void run() {
        try {
            // Velg tilfeldig måltid
            Meal[] meals = Meal.values();
            Meal meal = meals[new Random().nextInt(meals.length)];
            Order order = new Order(this, meal);

            // Legg inn bestilling
            if (!restaurant.placeOrder(order)) {
                logger.log(getName() + " could not place order and leaves angry 😣.");
                finalizeStatus(false); // Sett status til unhappy
                return;
            }

            // Vent på måltid
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < maxWaitTime) {
                if (order.isCompleted()) {
                    finalizeStatus(true); // Sett status til happy
                    logger.log(getName() + " received " + meal.getName() + " in time and is happy 😊!");
                    return;
                }
                Thread.sleep(100); // Sjekk hvert 100 ms
            }

            // Hvis ventetiden er over og ordren ikke er ferdig
            finalizeStatus(false); // Sett status til unhappy
            logger.log(getName() + " waited too long for " + meal.getName() + " and leaves angry 😣.");
        } catch (InterruptedException e) {
            logger.log(getName() + " was interrupted.");
            Thread.currentThread().interrupt();
        }
    }

    private synchronized void finalizeStatus(boolean happy) {
        if (!statusFinalized) {
            isHappy = happy;
            statusFinalized = true; // Sikrer at statusen ikke endres igjen
        }
    }
}