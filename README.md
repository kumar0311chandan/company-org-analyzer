# 📘 **Company Organizational Analyzer**

## 📌 Overview

This Java application analyzes a company’s organizational structure from a CSV file and reports:

### ✔ Managers earning **less** than they should

### ✔ Managers earning **more** than they should

### ✔ Employees with **too long** reporting lines (>4 levels to CEO)

### ✔ Invalid manager references

### ✔ Duplicate employee IDs

### ✔ Circular reporting chains

### ✔ A final summary of processed vs. invalid records

The program automatically reads **employees.csv** from:

```
src/main/resources/employees.csv
```

---

## 🧱 Design Approach

### 🏗 Architecture Layers

#### **1. Model Layer**

Immutable domain classes:

* `Employee`
* `AnalysisReport`
  Produces fully formatted output including all required analysis sections.

#### **2. Parser Layer**

* `CsvEmployeeParser`
  Reads CSV input, validates numeric fields, and records invalid lines.

#### **3. Repository Layer**

* `EmployeeRepository`
  Builds hierarchy maps, detects:

    * invalid manager references
    * duplicate employee IDs

#### **4. Service Layer**

* `OrganizationAnalyzer`
  Core business rules:

    * Salary analysis (underpaid/overpaid managers)
    * Depth analysis (too-long reporting chains)
    * Cycle detection (circular manager reporting)

---

## 📌 Key Assumptions

1. **Manager Salary Calculation**

    * Only **direct subordinates** used to compute averages.
    * Required salary range:

      ```
      minimum = avgSubSalary * 1.20
      maximum = avgSubSalary * 1.50
      ```
    * CEO cannot be “overpaid” but may be “underpaid”.

2. **CEO Detection**

    * Employee whose `managerId` is empty.
    * If multiple CEOs exist, the first encountered is used.

3. **Reporting Line Rule**

    * More than **4 managers** between employee and CEO is considered *too deep*.

4. **Data Validation**

    * Salary must be positive.
    * Employee IDs must be positive.
    * Manager ID must reference an existing employee.
    * Duplicate IDs are allowed but logged (last entry wins).

5. **Circular References**

    * Cycles like `A → B → C → A` are detected.
    * All participants in the cycle are excluded from salary/depth analysis.
    * Logged in final output.

---

## 📂 Project Structure

```
src/
 ├── main/
 │   ├── java/com/company/organalyzer/
 │   │   ├── Main.java
 │   │   ├── model/
 │   │   ├── parser/
 │   │   └── service/
 │   └── resources/employees.csv
 └── test/
     ├── parser/
     └── service/
```

---

## 🛠 Build & Run

### **Build**

```bash
mvn clean install
```

### **Run Program**

The application automatically loads:

```
src/main/resources/employees.csv
```

Run:

```bash
java -jar target/company-org-analyzer-1.0-SNAPSHOT.jar
```

No command-line arguments required.

---

## 🧪 Tests

### Run test suite:

```bash
mvn test
```

### Test coverage includes:

* CSV parsing with invalid lines
* Missing manager references
* Duplicate employee IDs
* Cycle detection
* Salary rule correctness
* Depth rule correctness

---

## ✔ Output Categories

The output includes the following sections:

1. **Managers earning less than required**
2. **Managers earning more than allowed**
3. **Employees with too deep reporting lines (>4 levels)**
4. **Invalid manager references**
5. **Duplicate employee IDs**
6. **Circular references detected**
7. **File processing summary**

    * total valid employees processed
    * total invalid entries

---