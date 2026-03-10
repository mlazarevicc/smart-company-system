from influxdb_client import InfluxDBClient, Point, WritePrecision
from influxdb_client.client.write_api import SYNCHRONOUS
from datetime import datetime, timedelta, timezone
import random

# Konfiguracija preuzeta iz tvog fajla
token = "nvt_token"
org = "nvt_org"
bucket = "nvt_bucket"
url = "http://localhost:8086"

client = InfluxDBClient(url=url, token=token, org=org)
write_api = client.write_api(write_options=SYNCHRONOUS) 

num_factories = 100
num_products = 20
prods_per_factory = 10
years = 4
days_in_year = 365.25
total_days = int(years * days_in_year)

factory_ids = list(range(1, num_factories + 1))
product_ids = list(range(1, num_products + 1)) # Imamo oko 40 proizvoda u data.sql

now = datetime.now(timezone.utc)

print(f"Početak generisanja {num_factories * prods_per_factory * total_days * 2} zapisa...")

for factory_id in factory_ids:
    print(f"Obrađujem fabriku: {factory_id}")
    # Svaka fabrika pravi 10 nasumičnih proizvoda
    my_products = random.sample(product_ids, prods_per_factory)
    
    batch = []
    for day in range(total_days):
        timestamp_base = now - timedelta(days=day)
        
        # 2 javljanja dnevno (npr. 08:00 i 20:00)
        for hour in [8, 20]:
            ts = timestamp_base.replace(hour=hour, minute=0, second=0, microsecond=0)
            
            for prod_id in my_products:
                qty = random.randint(100, 1000)
                point = Point("factory_production") \
                    .tag("factory_id", str(factory_id)) \
                    .tag("product_id", str(prod_id)) \
                    .field("quantity", qty) \
                    .time(ts, WritePrecision.NS)
                batch.append(point)
        
        # Upisuj u grupama od 5000 tačaka radi stabilnosti
        if len(batch) >= 5000:
            write_api.write(bucket=bucket, org=org, record=batch)
            batch = []
            
    # Upis preostalih tačaka za tu fabriku
    if batch:
        write_api.write(bucket=bucket, org=org, record=batch)


print("Generisanje preciznih heartbeat-ova (na 30s) za 'Online' status...")
# # Generišemo heartbeats za poslednjih 5 minuta da bi sistem video fabrike kao aktivne
# for factory_id in factory_ids:
#     for i in range(10): # 10 heartbeata po 30s = poslednjih 5 minuta
#         timestamp = now - timedelta(seconds=i*30)
#         p = Point("factory_availability") \
#             .tag("factory_id", str(factory_id)) \
#             .field("online", False) \
#             .field("reason", "Regular heartbeat") \
#             .time(timestamp, WritePrecision.NS)
#         write_api.write(bucket=bucket, org=org, record=p)

# client.close()