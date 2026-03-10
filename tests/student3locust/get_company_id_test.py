from locust import HttpUser, task, between
import random
import json

class VehicleCrudUser(HttpUser):
    wait_time = between(1, 2)
    host = "http://localhost:8080/api"

    def on_start(self):
        login_payload = {
            "email": "user_1@example.com",
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
    def reject_company(self):
        company_id = random.randint(1, 50000)

        self.client.get(
            f"/companies/{company_id}",
            headers=self.headers,
        )