package com.company.organalyzer;

import com.company.organalyzer.parser.CsvEmployeeParser;
import com.company.organalyzer.service.EmployeeRepository;
import com.company.organalyzer.service.OrganizationAnalyzer;

import java.io.InputStream;

public class Main {

    public static void main(String[] args) {

        try {
            InputStream in = Main.class.getClassLoader()
                    .getResourceAsStream("employees.csv");

            if (in == null) {
                System.err.println("employees.csv not found in resources folder.");
                System.exit(1);
            }

            var parsed = CsvEmployeeParser.parse(in);
            var repo = new EmployeeRepository(parsed.employees());
            var analyzer = new OrganizationAnalyzer(repo);

            int invalid =
                    parsed.errors().size()
                            + repo.getInvalidManagers().size()
                            + repo.getDuplicateIds().size()
                            + analyzer.getCircularReferences().size();

            var report = analyzer.analyze(parsed.employees().size(), invalid);

            System.out.println(report);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
