public enum EventType {
    DATA_BREACH("Data Breach", "Install Firewall and upgrade security"),
    POWER_OUTAGE("Power Outage", "Install UPS or move to co-location"),
    NETWORK_CONGESTION("Network Congestion", "Upgrade bandwidth or add load balancer"),
    HARDWARE_FAILURE("Hardware Failure", "Replace hardware or use backup"),
    PRICE_COMPLAINT("Price Complaint", "Adjust pricing or add promotions"),
    DDOS_ATTACK("DDoS Attack", "Install DDoS protection"),
    COMPETITOR_PRESSURE("Competitor Pressure", "Upgrade services or increase marketing"),
    IP_SHORTAGE("IP Shortage", "Purchase additional IP blocks"),
    SYSTEM_ERROR("System Error", "Rollback system or update patches"),
    SPECIAL_EVENT("Special Event", "Positive event - no action needed");

    private final String displayName;
    private final String solution;

    EventType(String displayName, String solution) {
        this.displayName = displayName;
        this.solution = solution;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSolution() {
        return solution;
    }
} 