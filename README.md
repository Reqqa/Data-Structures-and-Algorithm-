# Job Sequencing – Financial Investment Allocation

**MIDA (Malaysian Investment Development Authority)** receives thousands of investment proposals annually but has limited facilitation capacity per quarter. Using the **Job Sequencing with Deadlines** algorithm, this program determines the optimal sequence of approved investment projects to **maximise total capital investment (RM billion)** within quarterly funding deadlines.

> All figures are directly citable from [mida.gov.my](https://www.mida.gov.my)


## Problem Statement

| Element | Description |
|---------|-------------|
| **Scenario** | A bank / investor has multiple projects with returns and deadlines for funding |
| **Objective** | Allocate funds to projects with the highest returns that can be completed in time |
| **Rule** | High-yield investments are chosen first; lower-yield options are skipped if deadline slots are full |
| **Real-world context** | Malaysia MIDA approved RM329.5 billion across 5,101 projects in 2023 |

---

## Dataset

File: `dataset.csv`

| Column | Description |
|--------|-------------|
| `Job_ID` | Unique identifier (J1–J10) |
| `Project_Name` | Name of the investment project |
| `Sector` | Industry sector |
| `Investment_RM_Billion` | Expected return / profit (RM billion) — used as **profit** in algorithm |
| `Jobs_Created` | Number of jobs generated |
| `Deadline_Slot` | Quarterly funding deadline — used as **deadline** in algorithm (Slot 1 = Q1, Slot 2 = Q2, Slot 3 = Q3) |

### Sample Data

```
Job_ID,Project_Name,Sector,Investment_RM_Billion,Jobs_Created,Deadline_Slot
J1,E&E Manufacturing (Penang),Manufacturing,85.4,73939,2
J2,ICT & Data Centres,Services,63.7,52732,1
J3,Real Estate Development,Property,15.5,12000,3
J4,Distributive Trade,Trade,6.7,8500,2
J5,Financial Services,Finance,2.2,4200,1
J6,INV Battery Separator (Penang),Manufacturing,3.2,2032,2
J7,Xinyi Solar PV Glass,Green Energy,5.0,1800,3
J8,EVE Energy Li-Ion Batteries,Manufacturing,2.0,1500,1
J9,Green Technology Projects,Green Energy,1.3,900,3
J10,Mining (Primary Sector),Primary,8.8,661,2
```



## Project Structure

```
project-root/
Main.java           — Entry point, CSV loader, menu, output
Schedule.java       — Data class implementing Comparable/Comparator
Backtracking.java   — Tharmu's Algorithm
dataset.csv         — Real MIDA Malaysia investment dataset
README.md           — This file

```
## OOP Design

### 1.1 Framework Design — Interfaces & Abstract Classes

| Component | Type | Purpose |
|-----------|------|---------|
| `Comparator<Schedule>` | Interface (Java built-in) | Custom sort orders — by deadline. Used by `PriorityQueue` and `List.sort()` |

### 1.2 Concrete Classes

| Class | Role |
|-------|------|
| `Schedule` | Represents one investment project. Implements `Comparable<Schedule>`. Stores all 6 CSV fields |
| `Backtracking` | Algorithm — explores every subset recursively to find the globally optimal schedule |
| `Main` | Entry point — CSV loader, menu, algorithms 1–3, and formatted output |

### 1.3 Data Structures

| Structure | Used in | Purpose |
|-----------|---------|---------|
| `ArrayList<Schedule>` | All classes | Dynamic list of investment projects |
| `HashSet<String>` | `printOutput()` | O(1) lookup to separate selected vs unselected jobs |
| `PriorityQueue<Schedule>` | Algorithm 3 | Min-heap ordered by deadline using `Comparator` |

### 1.4 Algorithms

| # | Name | Sorting Mechanism | Complexity |
|---|------|------------------|------------|
| 1 | Backtracking | Recursive include/exclude with backtrack | O(2ⁿ) |



## References

- MIDA Official Statistics: https://www.mida.gov.my
- 12th Malaysia Plan (12MP): https://www.epu.gov.my/en/twelfth-malaysia-plan
- Total approved investments 2023: RM329.5 billion across 5,101 projects (MIDA Annual Report 2023)