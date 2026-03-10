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

    @task(5)
    def list_pending(self):
        page = random.randint(0, 50)
        self.client.get(
            f"/companies/pending?page={page}&size=20&sort=name",
            headers=self.headers,
            name="/companies/pending"
        )