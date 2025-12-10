package com.company.organalyzer.model;

import java.util.List;
import java.util.Map;

public class AnalysisReport {

    private final Map<Employee, Double> underpaidManagers;
    private final Map<Employee, Double> overpaidManagers;
    private final Map<Employee, Integer> longReportingLines;

    private final List<String> invalidManagerRefs;
    private final List<String> duplicateIds;
    private final List<String> circularRefs;

    private final int totalProcessed;
    private final int invalidEntries;

    public AnalysisReport(
            Map<Employee, Double> underpaidManagers,
            Map<Employee, Double> overpaidManagers,
            Map<Employee, Integer> longReportingLines,
            List<String> invalidManagerRefs,
            List<String> duplicateIds,
            List<String> circularRefs,
            int totalProcessed,
            int invalidEntries
    ) {
        this.underpaidManagers = Map.copyOf(underpaidManagers);
        this.overpaidManagers = Map.copyOf(overpaidManagers);
        this.longReportingLines = Map.copyOf(longReportingLines);

        this.invalidManagerRefs = List.copyOf(invalidManagerRefs);
        this.duplicateIds = List.copyOf(duplicateIds);
        this.circularRefs = List.copyOf(circularRefs);

        this.totalProcessed = totalProcessed;
        this.invalidEntries = invalidEntries;
    }


    public Map<Employee, Double> getUnderpaidManagers() { return underpaidManagers; }
    public Map<Employee, Double> getOverpaidManagers() { return overpaidManagers; }
    public Map<Employee, Integer> getLongReportingLines() { return longReportingLines; }

    public List<String> getInvalidManagerReferences() { return invalidManagerRefs; }
    public List<String> getDuplicateIds() { return duplicateIds; }
    public List<String> getCircularReferences() { return circularRefs; }

    public int getTotalProcessed() { return totalProcessed; }
    public int getInvalidEntries() { return invalidEntries; }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("═".repeat(80)).append("\n");
        sb.append("               COMPANY ORGANIZATION ANALYSIS REPORT\n");
        sb.append("═".repeat(80)).append("\n\n");

        if (!underpaidManagers.isEmpty()) {
            sb.append("MANAGERS EARNING LESS THAN REQUIRED:\n");
            underpaidManagers.forEach((m, diff) -> sb.append(
                    " • " + m + " → short by $" + String.format("%,.2f", diff) + "\n"
            ));
            sb.append("\n");
        }

        if (!overpaidManagers.isEmpty()) {
            sb.append("MANAGERS EARNING MORE THAN ALLOWED:\n");
            overpaidManagers.forEach((m, diff) -> sb.append(
                    " • " + m + " → over by $" + String.format("%,.2f", diff) + "\n"
            ));
            sb.append("\n");
        }

        if (!longReportingLines.isEmpty()) {
            sb.append("EMPLOYEES WITH TOO LONG REPORTING LINES (>4 levels):\n");
            longReportingLines.forEach((e, extra) -> sb.append(
                    " • " + e + " → " + extra + " level(s) too deep\n"
            ));
            sb.append("\n");
        }

        if (!invalidManagerRefs.isEmpty()) {
            sb.append("INVALID MANAGER REFERENCES:\n");
            invalidManagerRefs.forEach(s -> sb.append(" • ").append(s).append("\n"));
            sb.append("\n");
        }

        if (!duplicateIds.isEmpty()) {
            sb.append("DUPLICATE EMPLOYEE IDs:\n");
            duplicateIds.forEach(s -> sb.append(" • ").append(s).append("\n"));
            sb.append("\n");
        }

        if (!circularRefs.isEmpty()) {
            sb.append("CIRCULAR REFERENCES DETECTED:\n");
            circularRefs.forEach(s -> sb.append(" • ").append(s).append("\n"));
            sb.append("\n");
        }

        sb.append("FILE PROCESS SUMMARY:\n");
        sb.append(" • Total employees processed: ").append(totalProcessed).append("\n");
        sb.append(" • Invalid entries: ").append(invalidEntries).append("\n");

        sb.append("\n").append("═".repeat(80)).append("\n");
        return sb.toString();
    }
}
