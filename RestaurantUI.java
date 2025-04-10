import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class RestaurantUI extends JFrame {
    private final Restaurant restaurant;
    private final EventLogger logger;
    private final JLabel happyLabel;
    private final JLabel angryLabel;
    private final JTextField customerNameField;
    private final JComboBox<Meal> mealComboBox;
    private final JTable orderTable;
    private final DefaultTableModel tableModel;
    private final Map<Order, String> orderStatuses;
    private final List<Order> allOrders;
    private final Random random = new Random();
    private final String[] realNames = {
        "Ola", "Kari", "Lars", "Ingrid", "Erik", "Sofie", "Thomas", "Marit",
        "Anders", "Hanne", "Jonas", "Emma", "Petter", "Liv", "Magnus", "Astrid"
    };

    public RestaurantUI() {
        setTitle("Restaurant Simulation");
        setSize(1600, 1200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize customerNameField and mealComboBox
        customerNameField = new JTextField(20);
        mealComboBox = new JComboBox<>(Meal.values());

        // Logger
        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        logger = new EventLogger(logArea);
        restaurant = new Restaurant(5, logger);
        orderStatuses = new HashMap<>();
        allOrders = new ArrayList<>();

        // Resultattavle
        JPanel scorePanel = new JPanel();
        scorePanel.setBackground(new Color(220, 220, 220));
        happyLabel = new JLabel("Happy Customers 😊: 0");
        angryLabel = new JLabel("Angry Customers 😣: 0");
        happyLabel.setForeground(new Color(0, 128, 0));
        angryLabel.setForeground(new Color(200, 0, 0));
        scorePanel.add(happyLabel);
        scorePanel.add(angryLabel);

        // Ordretabell
        String[] columns = {"Customer", "Meal", "Status", "Result"};
        tableModel = new DefaultTableModel(columns, 0);
        orderTable = new JTable(tableModel);
        orderTable.setRowHeight(50);
        orderTable.setFont(new Font("SansSerif", Font.PLAIN, 16));
        orderTable.setGridColor(Color.LIGHT_GRAY);
        orderTable.setShowGrid(true);
        JScrollPane tableScrollPane = new JScrollPane(orderTable);

        orderTable.setDefaultRenderer(Object.class, new TableCellRenderer() {
            private final JLabel label = new JLabel();

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                label.setText(value != null ? value.toString() : "");
                label.setHorizontalAlignment(SwingConstants.CENTER);

                // Endre farge basert på resultatet
                if (column == 3) { // "Result"-kolonnen
                    if ("Happy 😊".equals(value)) {
                        label.setForeground(new Color(0, 128, 0)); // Grønn for fornøyde kunder
                    } else if ("Angry 😣".equals(value)) {
                        label.setForeground(new Color(200, 0, 0)); // Rød for sinte kunder
                    } else {
                        label.setForeground(Color.BLACK); // Standard farge
                    }
                } else {
                    label.setForeground(Color.BLACK); // Standard farge for andre kolonner
                }

                return label;
            }
        });

        // Loggvisning
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setPreferredSize(new Dimension(0, 200));

        // Layout
        add(scorePanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(logScrollPane, BorderLayout.SOUTH);

        // Start the simulation
        startSimulation();
    }

    private void startSimulation() {
        Cook cook1 = new Cook("Chef Mario", restaurant, logger);
        Cook cook2 = new Cook("Chef Bob", restaurant, logger);

        cook1.addOrderTakenListener(order -> {
            synchronized (orderStatuses) {
                orderStatuses.put(order, "Preparing");
            }
        });
        cook2.addOrderTakenListener(order -> {
            synchronized (orderStatuses) {
                orderStatuses.put(order, "Preparing");
            }
        });

        cook1.start();
        cook2.start();

        // Tråd for å opprettholde 5 bestillinger
        new Thread(() -> {
            while (true) {
                int activeCount = restaurant.getActiveOrderCount();
                if (activeCount < 5) {
                    String randomName = realNames[random.nextInt(realNames.length)];
                    long randomWaitTime = 10000 + random.nextInt(10000);
                    Customer customer = new Customer(randomName, restaurant, randomWaitTime, logger);
                    Order order = new Order(customer, Meal.values()[random.nextInt(Meal.values().length)]);
                    restaurant.addCustomer(customer);
                    if (restaurant.placeOrder(order)) {
                        synchronized (orderStatuses) {
                            orderStatuses.put(order, "Waiting");
                            allOrders.add(order);
                        }
                        customer.addOrderAbandonedListener(o -> {
                            synchronized (orderStatuses) {
                                if (!"Completed".equals(orderStatuses.get(o))) {
                                    orderStatuses.put(o, "Left");
                                }
                            }
                        });
                        customer.start();
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();

        // Oppdater tabell og resultattavle
        new Thread(() -> {
            while (true) {
                happyLabel.setText("Happy Customers 😊: " + restaurant.getHappyCustomers());
                angryLabel.setText("Angry Customers 😣: " + restaurant.getAngryCustomers());

                synchronized (orderStatuses) {
                    synchronized (tableModel) {
                        tableModel.setRowCount(0);
                        List<Order> active = new ArrayList<>(restaurant.getActiveOrders());
                        List<Order> completed = new ArrayList<>(allOrders);
                        completed.removeAll(active);

                        // Oppdater aktive ordre
                        for (Order order : active) {
                            String status = orderStatuses.getOrDefault(order, "Waiting");
                            tableModel.addRow(new Object[]{
                                order.getCustomer().getName(),
                                order.getMeal().getName(),
                                status,
                                "Waiting..."
                            });
                        }

                        // Oppdater fullførte ordre
                        for (Order order : completed) {
                            String status = orderStatuses.getOrDefault(order, "Completed");
                            String result = order.getCustomer().isHappy() ? "Happy 😊" : "Angry 😣"; // Direkte fra isHappy()
                            tableModel.addRow(new Object[]{
                                order.getCustomer().getName(),
                                order.getMeal().getName(),
                                status,
                                result
                            });
                        }
                    }
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new RestaurantUI().setVisible(true);
        });
    }
}
