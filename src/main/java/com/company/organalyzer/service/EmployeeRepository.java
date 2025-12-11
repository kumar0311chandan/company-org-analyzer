package com.company.organalyzer.service;

import com.company.organalyzer.model.Employee;

import java.util.*;

public class EmployeeRepository {

    private final Map<Long, Employee> byId = new LinkedHashMap<>();
    private final Map<Long, List<Employee>> subordinates = new HashMap<>();

    private final List<String> invalidManagers = new ArrayList<>();
    private final List<String> duplicateIds = new ArrayList<>();

    private final Employee ceo;

    public EmployeeRepository(List<Employee> employees) {

        Set<Long> seen = new HashSet<>();

        for (Employee e : employees) {
            if (seen.contains(e.id())) {
                duplicateIds.add("Duplicate employee ID: " + e.id());
            }
            seen.add(e.id());
            byId.put(e.id(), e);
        }

        for (Employee e : byId.values()) {
            if (e.managerId() != null) {
                if (!byId.containsKey(e.managerId())) {
                    invalidManagers.add("Employee " + e.id() +
                            " references missing manager ID: " + e.managerId());
                    continue;
                }
                subordinates.computeIfAbsent(e.managerId(), k -> new ArrayList<>()).add(e);
            }
        }

        ceo = byId.values().stream()
                .filter(e -> e.managerId() == null)
                .findFirst()
                .orElse(null);
    }

    public Employee getCeo() { return ceo; }
    public Map<Long, Employee> allById() { return byId; }
    public Collection<Employee> allEmployees() { return byId.values(); }

    public List<Employee> getSubordinates(long id) {
        return subordinates.getOrDefault(id, List.of());
    }

    public List<String> getInvalidManagers() { return invalidManagers; }
    public List<String> getDuplicateIds() { return duplicateIds; }
}
