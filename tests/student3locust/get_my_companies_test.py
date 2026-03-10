from locust import HttpUser, task, between
import random
import json

class MyCompaniesUser(HttpUser):
    wait_time = between(0.5, 1)
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

    @task(3)
    def get_my_companies(self):
        self.client.get(
            "/companies/my-companies",
            headers=self.headers,
            name="/companies/my-companies"
        )