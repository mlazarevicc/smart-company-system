from locust import HttpUser, task, between
import random
import json

class VehicleCrudUser(HttpUser):
    wait_time = between(1, 2)
    host = "http://localhost:8080/api"

    def on_start(self):
        login_payload = {
            "email": "email1@factory.com",
            "password": "password123"
        }
        
        with self.client.post("/auth/login", json=login_payload, catch_response=True) as response:
            if response.status_code == 200:
                data = response.json()
                self.token = data.get("jwt")
                response.success()
            else:
                print(f"Login failed! Status: {response.status_code}")
                self.token = ""
                response.failure(f"Login failed: {response.status_code}")
                
        self.headers = {"Authorization": f"Bearer {self.token}"}

    @task(2)
    def search_vehicles(self):
        search_term = random.choice(["NS", "BG", "A", "123"])

        self.client.get(
            f"/vehicles?search={search_term}&page=0&size=20",
            headers=self.headers
        )