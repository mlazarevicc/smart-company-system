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

Follow this exact sequence to ensure the distributed system initializes correctly.

### Prerequisites
* **Docker & Docker Compose**
* **Java 17+** (Backend)
* **Node.js & npm** (Frontend)
* **Go 1.20+** (Simulators)
* **Python 3.9+** (Data Generation & Seeding)

### 1. Launch Infrastructure
Start the core ecosystem (PostgreSQL, InfluxDB, Redis, RabbitMQ, Nginx):
```bash
docker compose up -d

```

### 2. Start the Backend (Spring Boot)

The backend must be running before seeding data to ensure database schemas are initialized:

```bash
cd backend
./mvnw spring-boot:run

```

### 3. Initialize Data & Seeding

Once the backend is healthy, populate the system with initial states and telemetry:

**A. Generate Base Data (All Students):**

```bash
pip install -r tests/requirements.txt
python tests/generate_data.py   # Student 1
python tests/generate_data2.py  # Student 2
python tests/generate_data3.py  # Student 3

```

**B. Run Specific Seeders:**

```bash
# Student 1: InfluxDB Telemetry
python tests/influx_seed.py

# Student 2: Warehouse Seeder (Go)
cd simulators/warehouse-simulator && go run cmd/seeder/main.go

# Student 3: Vehicle Seeder (Go)
cd ../vehicle-simulator && go run cmd/seeder/main.go

```

### 4. Start the Frontend (React)

Launch the dashboard interface:

```bash
cd frontend
npm install
npm run dev

```

### 5. Start IoT Simulators (Go)

Finally, start the real-time telemetry streams:

```bash
# Run each in a separate terminal or as background processes
go run simulators/factory-simulator/cmd/simulator/main.go
go run simulators/warehouse-simulator/cmd/simulator/main.go
go run simulators/vehicle-simulator/cmd/simulator/main.go

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
