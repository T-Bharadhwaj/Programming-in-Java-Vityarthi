package com.campusevent.model;

/**
 * Represents a physical resource or venue in campus inventory.
 */
public class Resource {
    private String id;
    private String name;
    private ResourceType type;
    private int totalQuantity;
    private ResourceStatus status;
    private double costPerUnit;
    private String location;

    public Resource() {
        this.status = ResourceStatus.ACTIVE;
    }

    public Resource(String id, String name, ResourceType type, int totalQuantity, ResourceStatus status, double costPerUnit, String location) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.totalQuantity = totalQuantity;
        this.status = status != null ? status : ResourceStatus.ACTIVE;
        this.costPerUnit = costPerUnit;
        this.location = location;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ResourceType getType() { return type; }
    public void setType(ResourceType type) { this.type = type; }

    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }

    public ResourceStatus getStatus() { return status; }
    public void setStatus(ResourceStatus status) { this.status = status; }

    public double getCostPerUnit() { return costPerUnit; }
    public void setCostPerUnit(double costPerUnit) { this.costPerUnit = costPerUnit; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String toCsvLine() {
        return String.join(",",
                escapeCsv(id),
                escapeCsv(name),
                type.name(),
                String.valueOf(totalQuantity),
                status.name(),
                String.valueOf(costPerUnit),
                escapeCsv(location)
        );
    }

    public static Resource fromCsvLine(String csvLine) throws Exception {
        String[] tokens = csvLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        if (tokens.length < 7) {
            throw new IllegalArgumentException("Insufficient columns in Resource CSV record. Expected 7, got " + tokens.length);
        }
        String id = unescapeCsv(tokens[0]);
        String name = unescapeCsv(tokens[1]);
        ResourceType type = ResourceType.fromString(tokens[2]);
        int totalQuantity = Integer.parseInt(tokens[3].trim());
        ResourceStatus status = ResourceStatus.fromString(tokens[4]);
        double costPerUnit = Double.parseDouble(tokens[5].trim());
        String location = unescapeCsv(tokens[6]);

        return new Resource(id, name, type, totalQuantity, status, costPerUnit, location);
    }

    private static String escapeCsv(String input) {
        if (input == null) return "";
        if (input.contains(",") || input.contains("\"") || input.contains("\n")) {
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }

    private static String unescapeCsv(String input) {
        if (input == null) return "";
        input = input.trim();
        if (input.startsWith("\"") && input.endsWith("\"") && input.length() >= 2) {
            input = input.substring(1, input.length() - 1).replace("\"\"", "\"");
        }
        return input;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) - Total Qty: %d, Location: %s, Status: %s, Rate: $%.2f/unit",
                id, name, type, totalQuantity, location, status, costPerUnit);
    }
}
