# Spring Boot EPAM Training Application

This is a Spring Boot application developed as part of an EPAM training module. It provides a RESTful API for managing trainees, trainers, trainings, and training types, with support for multiple environments, authentication, monitoring via Spring Actuator and Prometheus, and comprehensive unit testing.

## Table of Contents
- [Features](#features)
- [Technologies](#technologies)
- [Project Structure](#project-structure)
- [Setup](#setup)
  - [Prerequisites](#prerequisites)
  - [Configuration](#configuration)
  - [Running the Application](#running-the-application)
- [Profiles](#profiles)
- [Endpoints](#endpoints)
- [Authentication](#authentication)
- [Monitoring](#monitoring)
  - [Actuator](#actuator)
  - [Prometheus](#prometheus)
- [Testing](#testing)
- [Logging](#logging)
- [Contributing](#contributing)
- [License](#license)

## Features
- **Trainee/Trainer Management**: Create, update, delete, and retrieve trainee and trainer profiles.
- **Training Management**: Create trainings and filter trainings for trainees and trainers.
- **Training Types**: Retrieve available training types.
- **Authentication**: Secure endpoints with username/password validation (except profile creation).
- **Multi-Environment Support**: Profiles for `dev`, `pre-prod`, and `prod` with distinct database configurations.
- **Monitoring**: Spring Actuator with custom health indicators and Prometheus metrics.
- **Unit Testing**: Comprehensive unit tests for controllers and services using Mockito and MockMvc.

## Technologies
- **Spring Boot**: 3.x (latest stable version)
- **Spring Data JPA**: For database interactions with PostgreSQL
- **Spring Actuator**: For application monitoring
- **Micrometer Prometheus**: For exporting metrics
- **SLF4J**: For logging
- **JUnit 5 & Mockito**: For unit testing
- **Jackson**: For JSON serialization/deserialization
- **PostgreSQL**: Database
- **Maven**: Build tool

## Project Structure
spring-boot-epam/
├── src/
│   ├── main/
│   │   ├── java/com/epam/training/spring_boot_epam/
│   │   │   ├── controller/       # REST controllers (e.g., TraineeController, TrainingController)
│   │   │   ├── service/          # Service interfaces and implementations
│   │   │   ├── repository/       # DAO interfaces for JPA
│   │   │   ├── domain/           # Entity classes (e.g., Trainee, Trainer, Training)
│   │   │   ├── dto/              # Data Transfer Objects (e.g., TrainingDTO, TraineeDTO)
│   │   │   ├── exception/        # Custom exceptions (e.g., DomainException)
│   │   │   ├── mapper/           # Mappers for converting entities to DTOs
│   │   │   ├── health/           # Custom health indicators
│   │   │   ├── metrics/          # Custom Prometheus metrics
│   │   │   └── SpringBootEpamApplication.java  # Main application class
│   │   └── resources/
│   │       ├── application.yml            # Base configuration
│   │       ├── application-dev.yml        # Dev profile config
│   │       ├── application-pre-prod.yml   # Pre-prod profile config
│   │       ├── application-prod.yml       # Prod profile config
│   │       └── import/initial.sql         # Initial DB setup script
│   └── test/
│       └── java/com/epam/training/spring_boot_epam/  # Unit tests
├── pom.xml
└── README.md


## Setup

### Prerequisites
- **Java**: 17 or higher
- **Maven**: 3.6+
- **PostgreSQL**: 13+ (running locally or in a container)
- **Prometheus**: For metrics monitoring (optional)
- **Docker**: Optional for running PostgreSQL or Prometheus

### Configuration
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/ibragimovanvar/spring-boot
   cd spring-boot
