from locust import HttpUser, task, between
import random
import json
import string
from datetime import datetime, timedelta

class CommonBehavior:
    def random_string(self, length=8):
        return ''.join(random.choices(string.ascii_letters, k=length))

# class CustomerUser(HttpUser, CommonBehavior):
#     wait_time = between(1, 3)
#     host = "http://localhost:8080/api"

#     def on_start(self):
#         """Prijava kupca pri pokretanju simulacije"""
#         user_id = random.randint(1, 10000) 
#         self.email = f"user_{user_id}@example.com"
        
#         login_payload = {"email": self.email, "password": "password123"}
        
#         with self.client.post("/auth/login", json=login_payload, catch_response=True) as response:
#             if response.status_code == 200:
#                 self.token = response.json().get("jwt")
#                 self.user_db_id = response.json().get("id", user_id) 
#                 self.headers = {'Authorization': f'Bearer {self.token}'}
#                 self.company_id = random.randint(1, 10000)
                
#             else:
#                 self.token = ""
#                 self.headers = {}
#                 self.company_id = None

    # @task(1)
    # def test_login(self):
    #     user_id = random.randint(1, 10000) 
    #     email = f"user_{user_id}@example.com"
        
    #     payload = {"email": email, "password": "password123"}
        
    #     # payload = {"email": self.email, "password": "password123"}
    #     self.client.post("/auth/login", json=payload, name="1. POST /login")

    # @task(1)
    # def register_customer(self):
    #     username = self.random_string(6)
    #     data = {
    #         "firstName": "Test",
    #         "lastName": "Kupac",
    #         "email": f"{username}@example.com",
    #         "password": "password123",
    #         "username": username
    #     }
    #     files = {
    #         "profileImage": ("profile.jpg", b"dummy_bytes", "image/jpeg")
    #     }
    #     with self.client.post("/auth/register", data=data, files=files, name="2. POST /register", catch_response=True) as response:
    #         if response.status_code not in (200, 201):
    #             response.failure(f"Greška {response.status_code}: {response.text}")
    #             print(f"REGISTER ERROR: {response.text}")

    # @task(1)
    # def create_order(self):

    #     if not self.company_id:
    #         return 

    #     payload = {
    #         "companyId": self.company_id,
    #         "items": [
    #             {"productId": self.user_db_id, "quantity": random.randint(1, 10)}
    #         ]
    #     }
    #     with self.client.post("/orders", json=payload, headers=self.headers, name="3. POST /orders", catch_response=True) as response:
    #         if response.status_code not in (200, 201):
    #             response.failure(f"Greška {response.status_code}: {response.text}")
    #             #print(f"ORDER CREATE ERROR: Status: {response.status_code}, Body: {response.text}")


class ManagerUser(HttpUser, CommonBehavior):
    wait_time = between(1, 4)
    host = "http://localhost:8080/api" 

    def on_start(self):
        self.valid_warehouse_sectors = []
        
        login_payload = {"email": "email1@factory.com", "password": "password123"}
        with self.client.post("/auth/login", json=login_payload, catch_response=True) as response:
            if response.status_code == 200:
                self.token = response.json().get("jwt")
                self.headers = {'Authorization': f'Bearer {self.token}'}
            else:
                self.token = ""
                self.headers = {}
                
        if not self.valid_warehouse_sectors:
            self._fetch_valid_sectors()
            
    def _fetch_valid_sectors(self):
        with self.client.get("/warehouses?size=50", headers=self.headers, name="Setup: Pre-fetch Sectors", catch_response=True) as resp:
            if resp.status_code == 200:
                json_resp = resp.json()
                if isinstance(json_resp, list):
                    warehouses = json_resp
                else:
                    warehouses = json_resp.get("content", [])
                                    
                for w in warehouses:
                    w_id = w.get("id")
                    sectors = w.get("sectors", [])
                    if sectors:
                        for s in sectors:
                            self.valid_warehouse_sectors.append((w_id, s.get("id")))

    @task(1)
    def get_temperature_analytics(self):
        if not self.valid_warehouse_sectors:
            # Ako lista ostane prazna, ne okidamo HTTP zahtev (i zato se ne vidi u locustu)
            return 
            
        warehouse_id, sector_id = random.choice(self.valid_warehouse_sectors)
            
        end_date = datetime.now()
        start_date = end_date - timedelta(days=90) 
        
        start_str = start_date.replace(microsecond=0).isoformat()
        end_str = end_date.replace(microsecond=0).isoformat()
        
        url = f"/warehouses/{warehouse_id}/analytics/temperature?sectorId={sector_id}&startDate={start_str}&endDate={end_str}&granularity=1d"
        
        with self.client.get(url, headers=self.headers, name="10. GET /analytics/temperature", catch_response=True) as response:
            if response.status_code not in (200, 201):
                response.failure(f"Greška {response.status_code}")

    # @task(1)
    # def create_warehouse(self):
    #     warehouse_data = json.dumps({
    #         "name": f"Magacin {self.random_string(5)}",
    #         "address": "Industrijska zona bb",
    #         "countryId": 1,
    #         "cityId": 1,
    #         "latitude": 45.2671,
    #         "longitude": 19.8335,

    #         "sectors": [
    #             {
    #                 "name": "Sektor A (Hladnjača)",
    #                 "description": "Sektor za osetljive proizvode"
    #             },
    #             {
    #                 "name": "Sektor B (Suvi)",
    #                 "description": "Standardni magacinski prostor"
    #             }
    #         ]
    #     })
        
    #     files = {
    #         "data": (None, warehouse_data, "application/json"),
    #         "images": ("product.jpg", b"dummy_image_bytes", "image/jpeg")
    #     }
        
    #     with self.client.post("/warehouses", files=files, headers=self.headers, name="5. POST /warehouses", catch_response=True) as response:
    #         if response.status_code not in (200, 201):
    #             response.failure(f"Greška {response.status_code}: {response.text}")
    #             #print(f"WAREHOUSE CREATE ERROR: Status: {response.status_code}, Body: {response.text}")


    # @task(1)
    # def get_warehouse(self):
    #     warehouse_id = random.randint(1, 40000) 
    #     self.client.get(f"/warehouses/{warehouse_id}", headers=self.headers, name="4. GET /warehouses/{id}")

    # @task(1)
    # def get_sector(self):
    #     # Primer dobavljanja sektora (moraš prilagoditi ID-jeve)
    #     warehouse_id = random.randint(15, 100)
    #     sector_id = warehouse_id * 3
    #     self.client.get(f"/warehouses/{warehouse_id}/sectors/{sector_id}", headers=self.headers, name="5. GET /warehouses/{w_id}/sectors/{s_id}")

    # @task(1)
    # def filter_warehouses(self):
    #     page = random.randint(0, 5)
    #     url = f"/warehouses?page={page}&size=20&status=OFFLINE"

    #     self.client.get(url, headers=self.headers, name="7. GET /warehouses")
        
        # with self.client.get(url, headers=self.headers, name="7. GET /warehouses", catch_response=True) as response:
        #     if response.status_code not in (200, 201):
        #         response.failure(f"Greška {response.status_code}")
        #         print(f"WAREHOUSES FILTER ERROR: Status: {response.status_code}, Body: {response.text}")

    # @task(1)
    # def create_sector(self):
    #     warehouse_id = random.randint(1, 40000)
    #     payload = {
    #         "name": f"Sektor {self.random_string(3)}",
    #         "description": "Novi sektor za skladištenje",
    #     }
    #     self.client.post(f"/warehouses/{warehouse_id}/sectors", json=payload, headers=self.headers, name="8. POST /warehouses/{id}/sectors")

    # @task(2)
    # def get_warehouse_availability(self):
    #     warehouse_id = random.randint(1, 1000)
    #     end_date = datetime.now()
    #     start_date = end_date - timedelta(days=7)
        
    #     start_str = start_date.isoformat()
    #     end_str = end_date.isoformat()
        
    #     self.client.get(
    #         f"/warehouses/{warehouse_id}/analytics/availability?startDate={start_str}&endDate={end_str}&granularity=1d",
    #         headers=self.headers, 
    #         name="9. GET /analytics/availability"
    #     )

    # @task(1)
    # def get_temperature_analytics(self):
    #     # 1. Nasumično izvuci validan (warehouseId, sectorId) iz memorije, BEZ HTTP ZAHETVA!
    #     if not self.valid_warehouse_sectors:
    #         return # Preskače ako slučajno nema podataka
            
    #     warehouse_id, sector_id = random.choice(self.valid_warehouse_sectors)
            
    #     # 2. Podešavanje datuma
    #     end_date = datetime.now()
    #     start_date = end_date - timedelta(days=90) # Period od 3 meseca
        
    #     start_str = start_date.replace(microsecond=0).isoformat()
    #     end_str = end_date.replace(microsecond=0).isoformat()
        
    #     # 3. Direktno gađamo Analitiku!
    #     url = f"/warehouses/{warehouse_id}/analytics/temperature?sectorId={sector_id}&startDate={start_str}&endDate={end_str}&granularity=1d"
        
    #     with self.client.get(url, headers=self.headers, name="10. GET /analytics/temperature", catch_response=True) as response:
    #         if response.status_code not in (200, 201):
    #             response.failure(f"Greška {response.status_code}")