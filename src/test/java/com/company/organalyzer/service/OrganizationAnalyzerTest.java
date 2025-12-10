package com.company.organalyzer.service;

import com.company.organalyzer.model.AnalysisReport;
import com.company.organalyzer.model.Employee;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrganizationAnalyzerTest {

    @Test
    void detectsUnderpaidAndOverpaidManagers() {
        var employees = List.of(
                new Employee(1, "CEO", "X", 100000, null),
                new Employee(2, "Mgr1", "A", 50000, 1L),    // underpaid
                new Employee(3, "Emp1", "B", 60000, 2L),

                new Employee(4, "Mgr2", "C", 200000, 1L),  // overpaid
                new Employee(5, "Emp2", "D", 80000, 4L)
        );

        var repo = new EmployeeRepository(employees);
        var analyzer = new OrganizationAnalyzer(repo);

        AnalysisReport report = analyzer.analyze(5, 0);

        // CEO + Mgr1 = 2 underpaid
        assertEquals(2, report.getUnderpaidManagers().size());

        // Mgr2 only
        assertEquals(1, report.getOverpaidManagers().size());
    }

    @Test
    void detectsTooDeepReportingChain() {
        // CEO → A → B → C → D → E → F
        var employees = List.of(
                new Employee(1, "CEO", "", 90000, null),
                new Employee(2, "A", "", 50000, 1L),
                new Employee(3, "B", "", 50000, 2L),
                new Employee(4, "C", "", 50000, 3L),
                new Employee(5, "D", "", 50000, 4L),
                new Employee(6, "E", "", 50000, 5L),
                new Employee(7, "F", "", 50000, 6L)
        );

        var repo = new EmployeeRepository(employees);
        var analyzer = new OrganizationAnalyzer(repo);

        AnalysisReport report = analyzer.analyze(7, 0);

        // E and F both exceed depth 4 → 2 employees
        assertEquals(2, report.getLongReportingLines().size());

        Employee emp6 = report.getLongReportingLines().keySet().stream()
                .filter(e -> e.id() == 6)
                .findFirst()
                .orElseThrow();

        assertEquals(1, report.getLongReportingLines().get(emp6));

        Employee emp7 = report.getLongReportingLines().keySet().stream()
                .filter(e -> e.id() == 7)
                .findFirst()
                .orElseThrow();

        assertEquals(2, report.getLongReportingLines().get(emp7));
    }

    @Test
    void detectsCircularReferences() {
        // Cycle: 11 → 12 → 11
        var employees = List.of(
                new Employee(1, "CEO", "", 90000, null),
                new Employee(10, "A", "", 70000, 1L),
                new Employee(11, "B", "", 70000, 10L),
                new Employee(12, "C", "", 70000, 11L),

                // Duplicate ID 11 makes a cycle 11 → 12 → 11
                new Employee(11, "B2", "", 70000, 12L)
        );

        var repo = new EmployeeRepository(employees);
        var analyzer = new OrganizationAnalyzer(repo);

        AnalysisReport report = analyzer.analyze(5, 1);

        assertFalse(report.getCircularReferences().isEmpty());
        assertTrue(report.getCircularReferences().get(0).contains("Circular"));

        // Cycle nodes excluded from all analysis
        assertTrue(report.getUnderpaidManagers().isEmpty());
        assertTrue(report.getOverpaidManagers().isEmpty());
        assertTrue(report.getLongReportingLines().isEmpty());
    }

    @Test
    void invalidManagerReferencesAreReported() {
        var employees = List.of(
                new Employee(1, "CEO", "", 90000, null),
                new Employee(2, "A", "", 50000, 9999L) // invalid manager
        );

        var repo = new EmployeeRepository(employees);
        var analyzer = new OrganizationAnalyzer(repo);

        AnalysisReport report = analyzer.analyze(2, 1);

        assertEquals(1, report.getInvalidManagerReferences().size());
        assertTrue(report.getInvalidManagerReferences().get(0).contains("9999"));
    }

    @Test
    void duplicateEmployeeIdsAreReported() {
        var employees = List.of(
                new Employee(1, "CEO", "", 90000, null),
                new Employee(5, "A", "", 50000, 1L),
                new Employee(5, "A2", "", 60000, 1L) // duplicate
        );

        var repo = new EmployeeRepository(employees);
        var analyzer = new OrganizationAnalyzer(repo);

        AnalysisReport report = analyzer.analyze(3, 1);

        assertEquals(1, report.getDuplicateIds().size());
        assertTrue(report.getDuplicateIds().get(0).contains("5"));
    }

    @Test
    void summaryCountsAreReturnedCorrectly() {
        var employees = List.of(
                new Employee(1, "CEO", "", 90000, null),
                new Employee(2, "A", "", 50000, 1L)
        );

        var repo = new EmployeeRepository(employees);
        var analyzer = new OrganizationAnalyzer(repo);

        AnalysisReport report = analyzer.analyze(2, 0);

        assertEquals(2, report.getTotalProcessed());
        assertEquals(0, report.getInvalidEntries());
    }
}
