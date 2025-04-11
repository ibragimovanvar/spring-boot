# Spring Boot EPAM Training Application

This is a Spring Boot application developed for the EPAM training program. It provides REST APIs, integrates with a PostgreSQL database, and includes monitoring via Spring Boot Actuator and Prometheus metrics. API documentation is available through Springdoc OpenAPI (Swagger UI).

## Table of Contents
- [Prerequisites](#prerequisites)
- [Setup](#setup)
  - [Database Configuration](#database-configuration)
  - [Spring Profile](#spring-profile)
  - [Actuator and Prometheus Configuration](#actuator-and-prometheus-configuration)
- [Running the Application](#running-the-application)
- [Accessing the Application](#accessing-the-application)
  - [API Documentation](#api-documentation)
  - [Actuator Endpoints](#actuator-endpoints)
  - [Prometheus Metrics](#prometheus-metrics)
- [Testing](#testing)
- [Building the Project](#building-the-project)

## Prerequisites

Ensure you have the following installed:
- **Java 17**: The application uses Java 17 (e.g., OpenJDK or Oracle JDK).
- **Gradle**: Version 8.10 or later (the project uses Gradle as the build tool).
- **PostgreSQL**: Version 15 or later for the database.
- **Prometheus**: For scraping metrics (optional for monitoring).
- **Git**: To clone the repository.
- **cURL or Postman**: For testing API endpoints (optional).

## Setup

### Database Configuration

The application uses a PostgreSQL database. Follow these steps to configure it:

1. **Install PostgreSQL** (if not already installed):
   - macOS: `brew install postgresql`
   - Ubuntu: `sudo apt-get install postgresql`
   - Windows: Download from [postgresql.org](https://www.postgresql.org/download/windows/).

2. **Create a Database**:
   - Connect to PostgreSQL: `psql -U postgres`
   - Create a database: `CREATE DATABASE epam_training;`

3. **Configure Database Credentials**:
   - Open `src/main/resources/application.yml` (or `application.properties`).
   - Update the datasource properties:
     ```yaml
     spring:
       datasource:
         url: jdbc:postgresql://localhost:5432/epam_training
         username: your_username
         password: your_password
