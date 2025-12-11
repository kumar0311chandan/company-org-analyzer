package com.company.organalyzer.parser;

import com.company.organalyzer.model.Employee;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class CsvEmployeeParserTest {

    private CsvEmployeeParser.ParseResult parse(String csv) throws Exception {
        return CsvEmployeeParser.parse(
                new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8))
        );
    }

    @Test
    void parsesValidLinesCorrectly() throws Exception {
        String csv = """
                Id,firstName,lastName,salary,managerId
                1,John,Doe,50000,
                2,Jane,Smith,60000,1
                """;

        var result = parse(csv);

        assertEquals(2, result.employees().size());
        assertTrue(result.errors().isEmpty());

        Employee e1 = result.employees().getFirst();
        assertEquals(1, e1.id());
        assertNull(e1.managerId());

        Employee e2 = result.employees().get(1);
        assertEquals(2, e2.id());
        assertEquals(1, e2.managerId());
    }

    @Test
    void skipsAndReportsInvalidNumericValues() throws Exception {
        String csv = """
                Id,firstName,lastName,salary,managerId
                1,Good,Employee,50000,
                2,Bad,Salary,notanumber,1
                3,Bad,Id,badid,1
                4,Good,Employee,55000,1
                """;

        var result = parse(csv);

        // Valid employees: 1 and 4
        assertEquals(2, result.employees().size());

        assertEquals(2, result.errors().size());
        assertTrue(result.errors().get(0).contains("Line 3"));
        assertTrue(result.errors().get(1).contains("Line 4"));
    }

    @Test
    void handlesMissingManagerIdAsNull() throws Exception {
        String csv = """
                Id,firstName,lastName,salary,managerId
                10,Alice,Wonder,75000,
                """;

        var result = parse(csv);

        assertEquals(1, result.employees().size());
        assertNull(result.employees().getFirst().managerId());
    }

    @Test
    void ignoresBlankLines() throws Exception {
        String csv = """
                Id,firstName,lastName,salary,managerId

                1,John,Doe,50000,

                2,Jane,Smith,60000,1

                """;

        var result = parse(csv);

        assertEquals(2, result.employees().size());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void catchesInvalidManagerId() throws Exception {
        String csv = """
                Id,firstName,lastName,salary,managerId
                1,A,AA,50000,
                2,B,BB,60000,x123
                3,C,CC,70000,1
                """;

        var result = parse(csv);

        assertEquals(2, result.employees().size()); // Lines 1 & 3 valid
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().getFirst().contains("Line 3"));
    }

    @Test
    void failsIfSalaryIsNegative() throws Exception {
        String csv = """
                Id,firstName,lastName,salary,managerId
                1,Good,One,50000,
                2,Bad,Salary,-100,1
                3,Good,Two,60000,1
                """;

        var result = parse(csv);

        assertEquals(2, result.employees().size()); // 1 & 3 valid
        assertEquals(1, result.errors().size());

        assertTrue(result.errors().getFirst().contains("Line 3"));
        assertTrue(result.errors().getFirst().contains("Salary must be positive"));
    }

    @Test
    void failsIfIdIsNegative() throws Exception {
        String csv = """
                Id,firstName,lastName,salary,managerId
                -5,Bad,Id,30000,
                1,Good,Entry,40000,
                """;

        var result = parse(csv);

        assertEquals(1, result.employees().size());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().getFirst().contains("Line 2"));
    }

    @Test
    void maintainsCorrectErrorLineNumbers() throws Exception {
        String csv = """
                Id,firstName,lastName,salary,managerId
                1,OK,One,50000,
                2,Bad,Salary,notanumber,1
                3,OK,Two,60000,1
                4,Bad,Manager,70000,X
                """;

        var result = parse(csv);

        assertEquals(2, result.employees().size());
        assertEquals(2, result.errors().size());

        assertTrue(result.errors().get(0).startsWith("Line 3"));
        assertTrue(result.errors().get(1).startsWith("Line 5"));
    }
}
