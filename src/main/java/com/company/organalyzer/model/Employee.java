package com.company.organalyzer.model;

public record Employee(long id, String firstName, String lastName, double salary, Long managerId) {

    public Employee {
        if (id <= 0) throw new IllegalArgumentException("Employee ID must be positive: " + id);
        if (salary <= 0) throw new IllegalArgumentException("Salary must be positive: " + salary);
    }

    @Override
    public String toString() {
        return String.format("%d: %s %s ($%.0f%s)",
                id, firstName, lastName, salary,
                managerId == null ? ", CEO" : ", reports to " + managerId);
    }
}
