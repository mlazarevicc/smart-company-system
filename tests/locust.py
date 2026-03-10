from locust import HttpUser, task, between
import random
import json
import string

class SmartManufacturingUser(HttpUser):
    wait_time = between(1, 3)
    host = "http://localhost:8080/api"

    def on_start(self):
        login_payload = {
            "email": "admin@smartmanufacturing.com",
            "password": "Be4@:q)!p@Aszyp0y>CY:qWPI$P=xT(="
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

    def _random_string(self, length=8):
        return ''.join(random.choices(string.ascii_letters, k=length))

    # ---------------------------------------------------------
    # 1. MANAGERS ENDPOINTS
    # ---------------------------------------------------------
    
    @task(3)
    def search_managers(self):
        queries = ["a", "m", "986", "last1111"]
        q = random.choice(queries)
        self.client.get(f"/managers?q={q}&page=0&size=20", headers=self.headers, name="/managers/search")

    @task(1)
    def register_manager(self):
        username = self._random_string(6)
        data = {
            "firstName": "TestIme",
            "lastName": "TestPrezime",
            "email": f"manager_{username}@smartmanufacturing.com",
            "username": f"user_{username}",
            "password": "Password123!"
        }
        
        files = {
            'profile_image': ('profile.jpg', b'dummy_bytes', 'image/jpeg')
        }
        
        self.client.post("/managers/register", data=data, files=files, headers=self.headers)

    # ---------------------------------------------------------
    # 2. PRODUCTS ENDPOINTS
    # ---------------------------------------------------------

    @task(1)
    def create_product(self):
        categories = ["SOFT_DRINKS", "JUICES", "WATER", "SPORTS_DRINKS", "COFFEE", "TEA", "PLANT_BASED", "DAIRY"]
        
        product_data = json.dumps({
            "name": f"Product {self._random_string(5)}", 
            "description": "Test description long enough to pass validation.",
            "category": random.choice(categories),
            "price": round(random.uniform(10.0, 500.0), 2),
            "weight": round(random.uniform(0.1, 5.0), 2),
            "is_available": True,
            "factoryIds": [1, 2]
        })
        
        files = {
            'data': (None, product_data, 'application/json'),
            'image': ('product.jpg', b'dummy_image_bytes', 'image/jpeg')
        }
        self.client.post("/products", files=files, headers=self.headers, name="/products (POST)")
        

    @task(3)
    def get_products(self):
        page = random.randint(0, 5)
        self.client.get(
            f"/products?page={page}&size=20&sortBy=name&direction=asc&available=true", 
            headers=self.headers, 
            name="/products (GET)"
        )

    @task(1)
    def update_product(self):
        product_id = random.randint(1, 1000) 
        update_data = json.dumps({
            "version": 0, # Dodato obavezno polje za optimistic locking
            "name": f"Updated Product {product_id}", 
            "description": "Updated description",
            "category": "COFFEE",
            "price": 150.0,
            "weight": 1.5,
            "is_available": True,
            "factoryIds": [1]
        })
        files = {
            'data': (None, update_data, 'application/json'),
            'image': ('updated.jpg', b'dummy_bytes', 'image/jpeg')
        }
        self.client.put(f"/products/{product_id}", files=files, headers=self.headers, name="/products/{id} (PUT)")

    @task(4)
    def get_product_by_id(self):
        product_id = random.randint(1, 1000)
        self.client.get(f"/products/{product_id}", headers=self.headers, name="/products/{id} (GET)")

    # ---------------------------------------------------------
    # 3. FACTORIES ENDPOINTS
    # ---------------------------------------------------------

    @task(1)
    def create_factory(self):
        factory_data = json.dumps({
            "name": f"Factory {self._random_string(5)}", 
            "address": "Bulevar Oslobodjenja 12",
            "cityId": 1,
            "countryId": 1,
            "latitude": 45.2671,
            "longitude": 19.8335,
            "productIds": [1, 2]
        })
        files = [
            ('data', (None, factory_data, 'application/json')),
            ('images', ('img1.jpg', b'dummy 1', 'image/jpeg')),
            ('images', ('img2.jpg', b'dummy 2', 'image/jpeg'))
        ]
        self.client.post("/factories", files=files, headers=self.headers, name="/factories (POST)")

    @task(3)
    def filter_factories(self):
        page = random.randint(0, 5)
        self.client.get(f"/factories/filter?page={page}&size=10&online=true", headers=self.headers, name="/factories/filter")

    @task(1)
    def update_factory(self):
        factory_id = random.randint(1, 1000)
        update_data = json.dumps({
            "version": 0, # Za optimistic locking
            "name": f"Updated Factory {factory_id}",
            "address": "Nova adresa bb",
            "cityId": 1,
            "countryId": 1,
            "latitude": 45.0,
            "longitude": 20.0,
            "productIds": [1]
        })
        files = [('data', (None, update_data, 'application/json'))]
        self.client.put(f"/factories/{factory_id}", files=files, headers=self.headers, name="/factories/{id} (PUT)")

    @task(2)
    def production_analytics(self):
        factory_id = random.randint(1, 50)
        product_id = random.randint(1, 20)
        
        periods = ["LAST_24_HOURS", "LAST_WEEK", "LAST_MONTH"]
        period = random.choice(periods)

        self.client.get(
            f"/factories/{factory_id}/production-analytics?productId={product_id}&period={period}", 
            headers=self.headers, 
            name="/factories/{id}/production-analytics"
        )