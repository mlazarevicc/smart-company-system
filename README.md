# Smart Company Management Platform 

## Overview
This project is a comprehensive, distributed cloud platform developed for a "Smart Company" (wholesale manufacturing and distribution) as part of the **Advanced Web Technologies** course (2025/2026).

The system provides a centralized dashboard for enterprise management, allowing administrators to monitor manufacturing plants, warehouses, and delivery fleets in real-time. It also functions as a robust B2B portal where external companies can register and place bulk orders.

---

## Architecture & Tech Stack

The ecosystem is built using a modern, scalable microservices-oriented architecture, separating the core web platform from standalone IoT simulators.

**Frontend**
* **React** – Dynamic, responsive single-page application for the user interface.

**Backend & Data Layer**
* **Spring Boot** – Core backend REST API handling business logic and security.
* **PostgreSQL** – Primary relational database for persistent data (users, products, facilities, orders).
* **Redis** – In-memory data structure store used for high-performance caching.
* **InfluxDB** – Time-series database optimized for storing high-frequency telemetry data from simulators.

**Messaging & IoT Simulation**
* **RabbitMQ** – Message broker handling asynchronous AMQP communication between the platform and IoT devices.
* **Go (Golang)** – High-performance, concurrent standalone simulators acting as IoT controllers for factories, warehouses, and vehicles.
* **Python** – Custom data generator scripts for populating initial states and generating realistic telemetry patterns.

**Infrastructure & Testing**
* **Nginx** – Reverse proxy for routing requests and load balancing.
* **Locust** – Load testing tool used to simulate concurrent users and ensure system reliability under stress.
* **Docker** – Containerization of databases, brokers, and infrastructure components.

---

## Core Features & Modules

### Student 1: Factory & Product Management
* **System Initialization & RBAC:** Automated super-admin provisioning, manager account lifecycle control, and role-based access management.
* **Product Catalog:** Full CRUD operations for company inventory, including dynamic attributes, pricing, images, and factory assignment.
* **Factory Management:** Interactive dashboard for managing manufacturing plants, including mapping integration (e.g., OpenLayers/Leaflet) for precise location pinning.
* **Production Telemetry & Analytics:** Integration with AMQP simulators to process real-time production metrics, visual historical analytics for manufactured quantities, and live WebSocket heartbeat monitoring.

### Student 2: B2B Commerce & Warehouse Management
* **Customer Authentication:** Secure registration and login workflows for B2B buyers, including automated email-based account activation.
* **Order Processing:** System for browsing product catalogs, creating bulk orders with real-time inventory validation, and automated PDF invoice generation via email.
* **Warehouse Management:** Interactive dashboard for managing warehouses, defining climate-controlled sectors, and mapping integration for location pinning.
* **Climate Telemetry & Analytics:** Integration with AMQP simulators to process real-time temperature/humidity data, visual historical analytics for sector climates, and live WebSocket heartbeat monitoring.

### Student 3: Logistics & Company Registration
* **Company Registration Workflow:** Secure process for B2B buyers to request firm registration, including mapping integration and ownership document/image uploads.
* **Registration Approval System:** Managerial portal for reviewing submitted company documents and approving or rejecting requests with automated email notifications.
* **Fleet Management:** Interactive dashboard for managing delivery vehicles, including specifications (weight limits, vehicle models) and asset image uploads.
* **Vehicle Telemetry & Analytics:** Integration with AMQP simulators to track real-time GPS coordinates, visual historical analytics for traveled distances, and live WebSocket heartbeat monitoring.

---

## 🚀 Getting Started

### Prerequisites
* Docker & Docker Compose
* Java 17+ (for Spring Boot)
* Node.js & npm (for React)
* Go 1.20+ (for Simulators)
* Python 3.9+ (for Data Generator & Locust)

### 1. Launch Infrastructure
Start the required databases and message broker using Docker:
```bash
docker compose up -d

```

*(This spins up PostgreSQL, InfluxDB, Redis, RabbitMQ, and Nginx).*

### 2. Start the Backend (Spring Boot)

Navigate to the backend directory and run the application:

```bash
./mvnw spring-boot:run

```

### 3. Start the Frontend (React)

Navigate to the frontend directory, install dependencies, and start the development server:

```bash
npm install
npm start

```

### 4. Run IoT Simulators & Data Generators

```bash
# Generate initial test data
python tests/generate_data.py
python tests/influx_seed.py

# Run a Go simulator (e.g., Factory Simulator)
go run cmd/factory_simulator/main.go --config config/factory_A.json

```

---

## 📈 Performance Testing

To run the load tests and verify system stability:

```bash
locust -f load_tests/locustfile.py --host=http://localhost:8080

```

Navigate to `http://localhost:8089` to view the Locust dashboard and start the swarm.

---

## 👨‍💻 Authors

* **Milan Lazarević** (Student 1) – Architecture, Manager Auth, Factory & Product Modules
* **Marina Ivanović** (Student 2) – Authentication, Order Processing, Warehouse Modules
* **Matijas Levang** (Student 3) – Company Registration, Fleet Management, Logistics Modules

**Course:** Advanced Web Technologies

**Academic Year:** 2025/2026
