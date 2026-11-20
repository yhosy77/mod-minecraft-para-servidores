package ErrorCapa8.skillIssueAdmin.Report;

public enum ReportType {
    SPAM("SPAM"), TOXICITY("Toxicidad"), HACKS("Hacks"), SCAM("Estafa"), HARASSMENT("Acoso"), OTHER("Otro");
    private final String label;
    ReportType(String label) { this.label = label; }
    public String label() { return label; }
}
