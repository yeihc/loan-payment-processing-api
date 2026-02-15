# Loan Payment Processing API

A serious and realistic service, perfect for a banking experience.

Designed as a small but robust backend, with professional expertise.

---

This mini core banking system prioritizes accounting consistency, traceability, and idempotence. It does not assume perfection in distributed environments; instead, it guarantees fault detection, controlled compensation, and reconciliation capabilities.

---

## ✨ Main Features
- Professional REST API
- User, account, and transaction management
- Transaction states: `PENDING`, `APPROVED`, `REJECTED`
- Real validations and explicit rules
- Error handling with clear responses
- Authentication and roles
- Relational persistence (PostgreSQL)
- Logging
- Minimal required tests

---

## 🚫 What it ISN'T
- It's not a simple CRUD application
- It's not a trendy microservice
- It's not a tutorial clone

---

## ✅ What it IS
A small but robust backend, designed with professional expertise.

Focused on demonstrating API design, clear domain, maintainable code, and minimum reliability.

---

## 📐 Technical Scope – Phase 1
**Technologies in safe zone:**
- Java 17+
- Spring Boot
- REST API
- PostgreSQL (ideal)
- Git

---

## 🎯 Demonstrated Objectives
- API design with clear endpoints
- Well-defined domain model
- Readable and maintainable code
- Guaranteed minimum reliability
- Ability to explain the design

---
I didn't implement Saga because the current system is monolithic and operates within a single ACID transaction. Introducing a distributed pattern in this context would be overly technical. However, the design respects aggregate boundaries and allows for evolution towards a distributed model if the context requires it.
---

## 🚀 How to execute
1. Clone the repository:
```bash
https://github.com/yeihc/loan-payment-processing-api.git

## Estructure:

com.yeihc
 ├── domain <-- The heart (Zero Spring/JPA dependencies)
 │    ├── model <-- User, Account, Transfer, Money (VO)
 │    ├── repository <-- Ports (Interfaces: AccountRepository)
 │    ├── exception <-- Business exceptions
 │    └── event <-- DomainEvents
 ├── application <-- Orchestration (Use Cases)
 │    ├── usecase <-- TransferFundsUseCase, OpenAccountUseCase
 │    ├── dto <-- Service Request/Response
 │    └── service <-- Use Case Implementation
 ├── infrastructure <-- Technical Details (Tools)
 │    ├── persistence <-- JPA Implementation, DB Entities (if you choose to separate them), Adapters
 │    └── config <-- Spring Beans, Security, etc.
 └── interfaces <-- Entry Points
      ├── rest <-- Controllers
      ├── mapper <-- DTO to Domain Converters
      └── advice<-- GlobalExceptionHandler
