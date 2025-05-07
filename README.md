# 🚀 CS331 Programming Language Lab

This repository contains six progressive assignments from **CS331: Programming Language Lab**, where I explored a variety of programming paradigms and languages including Java concurrent programming, Haskell functional programming, and Prolog logical programming.

## 🧭 Table of Contents
- [Overview](#overview)
- [Project Structure](#project-structure)
- [How to Run](#how-to-run)
- [Project Modules](#project-modules)
  - [Java Concurrent Programming I](#java-concurrent-programming-i)
  - [Advanced Java Concurrent Programming](#advanced-java-concurrent-programming)
  - [Basic Haskell Programming](#basic-haskell-programming)
  - [Advanced Haskell Programming](#advanced-haskell-programming)
  - [Basic Prolog Programming](#basic-prolog-programming)
  - [Advanced Prolog Programming](#advanced-prolog-programming)
- [Author](#author)

## 📖 Overview

This project represents a comprehensive exploration of multiple programming paradigms through six carefully structured assignments. Starting with Java concurrent programming for practical multi-threading applications, progressing through functional programming in Haskell, and concluding with logical programming in Prolog, each module builds proficiency in a different programming paradigm.

## 📁 Project Structure

```
cs331-programming-language-lab/
├── Assignment1_Java_Concurrent_Programming/
├── Assignment2_Advanced_Java_Concurrent/
├── Assignment3_Basic_Haskell/
├── Assignment4_Advanced_Haskell/
├── Assignment5_Basic_Prolog/
├── Assignment6_Advanced_Prolog/
└── README.md
```

## ▶️ How to Run

1. **Clone this repository**:
   ```bash
   git clone https://github.com/yourusername/cs331-programming-language-lab.git
   ```

2. **For Java assignments (1-2)**:
   ```bash
   cd Assignment1_Java_Concurrent_Programming
   javac *.java
   java MainClass <number_of_threads>
   ```

3. **For Haskell assignments (3-4)**:
   ```bash
   cd Assignment3_Basic_Haskell
   ghc -o program Main.hs
   ./program
   ```

4. **For Prolog assignments (5-6)**:
   ```bash
   cd Assignment5_Basic_Prolog
   swipl -s main.pl
   ```

## 📦 Project Modules

### 🧵 **Assignment 1 – Java Concurrent Programming I**

**Implementation**:
- Developed multi-threaded applications for numerical computation
- Part A: Integration using composite Simpson 1/3 rule
- Part B: Matrix multiplication for square matrices

**Features**:
- **Numerical Integration**: Applied Simpson's rule for approximating definite integrals with 10^6+ points
- **Matrix Operations**: Implemented parallel matrix multiplication for 1000×1000 matrices
- **Performance Analysis**: Evaluated scaling with 4-16 threads for integration and 8-500 threads for matrix multiplication

**Technologies**:
- Java Threads API
- Thread synchronization mechanisms
- Command-line parameter handling

### 🏦 **Assignment 2 – Advanced Java Concurrent Programming**

**Implementation**:
- Simulated a bank transaction system (Guwahati National Bank) with 10^6 users
- Designed thread-safe data structures for 10 branches with 10 updaters each

**Operations Supported**:
- **Balance Checks**: Read-only account balance queries (30% probability)
- **Cash Transactions**: Deposits and withdrawals with validation (23% each)
- **Money Transfers**: Inter-account transfers, potentially across branches (23%)
- **Account Management**: Customer addition (0.3%), deletion (0.3%), and branch transfers (0.4%)

**Technical Features**:
- Thread-safe linked lists and hash data structures
- Advanced locking protocols and synchronization
- High-throughput design handling 10^6 transactions per updater

### 🧮 **Assignment 3 – Basic Haskell Programming**

**Implementation**:
- Developed functional programming solutions for mathematical algorithms
- Focused on efficient and elegant implementation using Haskell features

**Functions Implemented**:
- **Square Root Calculator**: Efficient algorithm for computing square roots to 0.00001 accuracy
- **Fibonacci Number Generator**: O(n) or better algorithm for calculating the nth Fibonacci number with arbitrary precision
- **QuickSort Algorithm**: Implementation using list comprehension in functional style

**Features**:
- Pure functional programming approach
- Handling of arbitrarily large numbers using Integer type
- Embedded test cases for verification

### 🌳 **Assignment 4 – Advanced Haskell Programming**

**Implementation**:
- Created interactive program for tree operations in Haskell
- Combined user input parsing with advanced data structure manipulation

**Features**:
- **Input Parsing**: Converts comma-separated user input to list of numbers
- **Binary Search Tree**: Construction and multiple traversal methods (pre-order, in-order, post-order)
- **Breadth First Search**: BFS tree generation and traversal from the constructed BST

**Techniques**:
- Functional data structure representation
- Higher-order functions for tree operations
- User interaction in purely functional context

### 🔍 **Assignment 5 – Basic Prolog Programming**

**Implementation**:
- Explored logic programming fundamentals using Prolog
- Developed declarative solutions for list processing and numerical computation

**Predicates Implemented**:
- **has_duplicates/1**: Identifies if a list contains duplicate elements
- **squareroot/3**: Calculates square root of a positive number to specified accuracy

**Features**:
- Logical rule-based programming
- Recursive predicate definitions
- Iterative numerical approximation in logic programming context

### 🗺️ **Assignment 6 – Advanced Prolog Programming**

**Implementation**:
- Created a shortest path solver for maze/grid navigation with faulty nodes
- Modeled as mesh network routing problem with node failures

**Features**:
- **Dynamic Predicates**: Ability to add or remove faulty nodes during queries
- **Path Finding**: Logic for finding optimal routes between source and destination
- **Fault Tolerance**: Navigation around faulty nodes in the grid

**Technical Aspects**:
- Graph representation in Prolog
- Shortest path algorithm implementation
- Dynamic knowledge base management

## 👨‍💻 Author

Sushant Kumar  
Course: CS331 – Programming Language Lab  
Institution: IIT Guwahati  
Year: 2025
