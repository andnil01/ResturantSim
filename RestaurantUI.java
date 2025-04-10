import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;
import java.util.Comparator;

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
        
                String status = (String) table.getValueAt(row, 2); // Status-kolonnen
                String result = (String) table.getValueAt(row, 3); // Result-kolonnen
        
                if ("Left".equals(status) && result.contains("Angry")) {
                    label.setBackground(new Color(255, 102, 102)); // Lys rød bakgrunn
                    label.setOpaque(true); // Gjør bakgrunnen synlig
                    label.setForeground(Color.BLACK); // Svart tekst for kontrast
                } else if ("Completed".equals(status)) {
                    label.setFont(new Font("SansSerif", Font.BOLD, 18));
                    label.setForeground(result.contains("Happy") ? new Color(0, 128, 0) : new Color(200, 0, 0));
                    label.setOpaque(false); // Fjern bakgrunn
                } else {
                    label.setFont(new Font("SansSerif", Font.PLAIN, 16));
                    label.setForeground(status.equals("Waiting") ? Color.BLUE : Color.ORANGE);
                    label.setOpaque(false); // Fjern bakgrunn
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
        // Opprett kokker
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
                long randomWaitTime = 15000 + random.nextInt(10000); 
                Customer customer = new Customer(randomName, restaurant, randomWaitTime, logger);
                Order order = new Order(customer, Meal.values()[random.nextInt(Meal.values().length)]);
                restaurant.addCustomer(customer);
                if (restaurant.placeOrder(order)) {
                    synchronized (orderStatuses) {
                        orderStatuses.put(order, "Waiting");
                        allOrders.add(order);
                    }
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

            new Thread(() -> {
            while (true) {
                // Oppdater resultattavlen
                happyLabel.setText("Happy Customers 😊: " + restaurant.getHappyCustomers());
                angryLabel.setText("Angry Customers 😣: " + restaurant.getAngryCustomers());
        
                synchronized (orderStatuses) {
                    synchronized (tableModel) {
                        tableModel.setRowCount(0); // Tøm tabellen og fyll på nytt
                        List<Order> active = new ArrayList<>(restaurant.getActiveOrders());
                        List<Order> completed = new ArrayList<>(allOrders);
                        completed.removeAll(active); // Fullførte ordre
        
                        // Sorter aktive ordre (nyeste først)
                        active.sort(Comparator.comparing(Order::getCreationTime).reversed());
                        // Sorter fullførte ordre (eldste først)
                        completed.sort(Comparator.comparing(Order::getCreationTime));
        
                        // Legg til aktive ordre øverst
                        for (Order order : active) {
                            String status = orderStatuses.getOrDefault(order, "Waiting");
                            tableModel.addRow(new Object[]{
                                order.getCustomer().getName(),
                                order.getMeal().getName(),
                                status,
                                "Waiting..."
                            });
                        }
        
                        // Legg til fullførte ordre under
                        for (Order order : completed) {
                            if (!"Completed".equals(orderStatuses.get(order)) && !"Left".equals(orderStatuses.get(order))) {
                                if (order.getCustomer().isHappy()) {
                                    orderStatuses.put(order, "Completed"); // Oppdater til Completed
                                } else {
                                    orderStatuses.put(order, "Left"); // Oppdater til Left
                                }
                            }
                            String result = orderStatuses.get(order).equals("Completed") ? "Happy 😊" : "Angry 😣";
                            tableModel.addRow(new Object[]{
                                order.getCustomer().getName(),
                                order.getMeal().getName(),
                                orderStatuses.get(order), // Bruk statusen (Completed eller Left)
                                result
                            });
                        }
                    }
                }
        
                try {
                    Thread.sleep(500); // Oppdater hvert 500 ms
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