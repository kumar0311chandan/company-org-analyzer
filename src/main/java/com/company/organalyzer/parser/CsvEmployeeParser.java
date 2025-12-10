package com.company.organalyzer.parser;

import com.company.organalyzer.model.Employee;

import java.io.*;
import java.util.*;

public class CsvEmployeeParser {

    public record ParseResult(List<Employee> employees, List<String> errors) {}

    public static ParseResult parse(InputStream in) throws IOException {

        List<Employee> employees = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        int lineNo = 0;
        boolean header = true;

        while ((line = br.readLine()) != null) {
            lineNo++;
            if (header) {
                header = false;
                continue;
            }
            if (line.isBlank()) continue;

            try {
                String[] p = line.split(",", -1);
                long id = Long.parseLong(p[0].trim());
                String first = p[1].trim();
                String last = p[2].trim();
                double salary = Double.parseDouble(p[3].trim());
                Long managerId = p.length > 4 && !p[4].isBlank()
                        ? Long.parseLong(p[4].trim())
                        : null;

                employees.add(new Employee(id, first, last, salary, managerId));

            } catch (Exception ex) {
                errors.add("Line " + lineNo + ": " + ex.getMessage());
            }
        }

        return new ParseResult(employees, errors);
    }
}
