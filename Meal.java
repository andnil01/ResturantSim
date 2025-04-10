public enum Meal {
    PIZZA("Pizza", 7000, "Italian"), 
    BURGER("Burger", 4000, "American"),
    PASTA("Pasta", 3000, "Italian"),
    SUSHI("Sushi", 6000, "Japanese");

    private final String name;
    private final long preparationTime;
    private final String type;

    Meal(String name, long preparationTime, String type) {
        this.name = name;
        this.preparationTime = preparationTime;
        this.type = type;
    }

    public String getName() { return name; }
    public long getPreparationTime() { return preparationTime; }
    public String getType() { return type; }
}