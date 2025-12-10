package com.company.organalyzer.service;

import com.company.organalyzer.model.AnalysisReport;
import com.company.organalyzer.model.Employee;

import java.util.*;

public class OrganizationAnalyzer {

    private static final double MIN_MULTIPLIER = 1.20;
    private static final double MAX_MULTIPLIER = 1.50;
    private static final int MAX_DEPTH = 4;

    private final EmployeeRepository repo;
    private final List<String> circularRefs = new ArrayList<>();

    public OrganizationAnalyzer(EmployeeRepository repo) {
        this.repo = repo;
    }

    public AnalysisReport analyze(int processed, int invalid) {

        Set<Long> cycles = detectCycles();

        Map<Employee, Double> underpaid = new LinkedHashMap<>();
        Map<Employee, Double> overpaid = new LinkedHashMap<>();
        Map<Employee, Integer> deep = new LinkedHashMap<>();

        for (Employee m : repo.allEmployees()) {
            if (cycles.contains(m.id())) continue;

            var subs = repo.getSubordinates(m.id());
            if (subs.isEmpty()) continue;

            double avg = subs.stream().mapToDouble(Employee::salary).average().orElse(0);
            double minReq = avg * MIN_MULTIPLIER;
            double maxReq = avg * MAX_MULTIPLIER;

            if (m.salary() < minReq) underpaid.put(m, minReq - m.salary());
            if (m.managerId() != null && m.salary() > maxReq)
                overpaid.put(m, m.salary() - maxReq);
        }

        Map<Long, Integer> depth = computeDepth(cycles);
        depth.forEach((id, d) -> {
            if (d > MAX_DEPTH) {
                deep.put(repo.allById().get(id), d - MAX_DEPTH);
            }
        });

        return new AnalysisReport(
                underpaid,
                overpaid,
                deep,
                repo.getInvalidManagers(),
                repo.getDuplicateIds(),
                circularRefs,
                processed,
                invalid
        );
    }

    private Map<Long, Integer> computeDepth(Set<Long> exclude) {
        Map<Long, Integer> depth = new HashMap<>();
        Employee ceo = repo.getCeo();
        if (ceo == null) return depth;

        Queue<Long> q = new ArrayDeque<>();
        q.add(ceo.id());
        depth.put(ceo.id(), 0);

        while (!q.isEmpty()) {
            long id = q.poll();
            int d = depth.get(id);

            for (Employee sub : repo.getSubordinates(id)) {
                if (exclude.contains(sub.id())) continue;
                if (!depth.containsKey(sub.id())) {
                    depth.put(sub.id(), d + 1);
                    q.add(sub.id());
                }
            }
        }

        return depth;
    }

    private Set<Long> detectCycles() {
        Set<Long> visited = new HashSet<>();
        Set<Long> stack = new HashSet<>();
        Set<Long> cyc = new LinkedHashSet<>();

        for (Employee e : repo.allEmployees()) {
            if (!visited.contains(e.id())) {
                dfs(e.id(), visited, stack, cyc);
            }
        }

        if (!cyc.isEmpty()) circularRefs.add("Circular reporting chain detected: " + cyc);
        return cyc;
    }

    private boolean dfs(long id, Set<Long> visited, Set<Long> stack, Set<Long> cyc) {

        visited.add(id);
        stack.add(id);

        Employee e = repo.allById().get(id);
        if (e != null && e.managerId() != null) {
            long mgr = e.managerId();

            if (!visited.contains(mgr)) {
                if (dfs(mgr, visited, stack, cyc)) {
                    cyc.add(id);
                    stack.remove(id);
                    return true;
                }
            }
            else if (stack.contains(mgr)) {
                cyc.add(id);
                cyc.add(mgr);
                stack.remove(id);
                return true;
            }
        }

        stack.remove(id);
        return false;
    }

    public List<String> getCircularReferences() { return circularRefs; }
}
