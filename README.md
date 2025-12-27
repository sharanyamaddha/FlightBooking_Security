# Flight Booking Microservices with Spring Security & JWT
This project is a microservices-based **Flight Booking System** built with **Spring Boot**, **Spring Cloud**, and **Spring Security** using **JWT**  authentication.

## Microservices Overview

The system consists of the following services:

- **API-Gateway**
  - Single entry point for all clients
  - Validates JWT tokens
  - Applies **role-based access control**
  - Routes requests to downstream services

- **Auth-Service**
  - Handles **user registration** and **login**
  - Generates **JWT tokens** with user roles (`ROLE_ADMIN`, `ROLE_USER`)

- **FlightService**
  - Manages flight data (add, search, view, reserve, release seats)
  - Relies on API-Gateway for authentication and authorization

- **BookingService**
  - Handles booking creation and cancellation
  - Publishes booking events to Kafka

- **NotificationService**
  - Listens to Kafka events (`booking-created`, `booking-cancelled`)
  - Sends booking confirmation / cancellation notifications (email)

- **Eureka Server**
  - Service discovery for all microservices

- **Config Server**
  - Centralized external configuration
    
 
## Security Design Overview
- **Authentication — Auth-Service + JWT**

  All authentication is handled by Auth-Service.

  -  Endpoints
    
      POST /api/auth/signup    # Register new user
     
      POST /api/auth/signin    # Login and receive JWT

  - Successful Login Returns:

    JWT Token
    
    User Details

    Assigned Roles (e.g., ["ROLE_ADMIN"], ["ROLE_USER"])

  - JWT Structure Contains:

    sub → Username
    
    roles → User's granted authorities
    
    iat → Issued time
    
    exp → Expiration time

    The client uses this token for all further requests.

- **Authorization — API-Gateway (Spring Cloud Gateway MVC)**

    All secured requests must pass through the API-Gateway
    (only /api/auth/** is publicly accessible).

  - The client sends the token in every request:
    Authorization: Bearer <jwt-token>

- **What Happens Inside API-Gateway**
  JwtAuthFilter
  
  This filter executes before requests reach any microservice.
  
  It:

  - Reads the Authorization header
  
  - Extracts and decodes the JWT
  
  - Validates the signature

  Retrieves:

  - Username (sub)

  - User roles

  Builds:

  - UsernamePasswordAuthenticationToken

  Stores authentication inside:

  - SecurityContextHolder


    This makes the user “logged in” for the downstream services.

-  **WebSecurityConfig (API-Gateway)**
  
    Defines who can access which endpoint based on roles
    
    The downstream microservices do not enforce authentication -
    the gateway completely controls access.



