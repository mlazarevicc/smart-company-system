import psycopg2
import io
import random
import csv
from datetime import datetime

DB_PARAMS = {
    "dbname": "nvt_db",
    "user": "nvt_user",
    "password": "nvt_pass",
    "host": "localhost",
    "port": 5433
}

STATIC_PASSWORD_HASH = "$2a$04$qmVy/ONyW2fXpQDMYcHnpOuVQlw/5Hi6xJMCJFCDEoIaS9Zyqnik6"

def generate_relational_data():
    try:
        conn = psycopg2.connect(**DB_PARAMS)
        cur = conn.cursor()

        cities_data = []
        try:
            with open('cities.csv', mode='r', encoding='utf-8') as f:
                reader = csv.DictReader(f)
                for row in reader:
                    cities_data.append({
                        'id': int(row['id']),
                        'country_id': int(row['country_id']),
                        'lat': float(row.get('latitude', 44.0)),
                        'lon': float(row.get('longitude', 20.0))
                    })
            print(f"Učitano {len(cities_data)} gradova iz CSV-a.")
        except Exception as e:
            print(f"Greška pri čitanju cities.csv: {e}")
            return

        print("Generišem 2.000.000 kupaca u grupama od 100k...")
        total_customers = 2000000
        batch_size = 100000
        
        for i in range(0, total_customers, batch_size):
            f = io.StringIO()
            for j in range(i + 1, min(i + batch_size + 1, total_customers + 1)):
                first_name = f"CustomerName_{j}"
                last_name = f"CustomerLast_{j}"
                username = f"user_{j}"
                email = f"user_{j}@example.com"
                role = "CUSTOMER"
                profile_img = "http://localhost:8080/static/user-avatar.webp"
                created_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
                active = "true"
                
                f.write(f"{first_name}\t{last_name}\t{username}\t{email}\t{STATIC_PASSWORD_HASH}\t{role}\t{profile_img}\t{created_at}\t{active}\n")
            
            f.seek(0)
            cur.copy_from(f, 'customer', sep='\t', columns=('first_name', 'last_name', 'username', 'email', 'password', 'role', 'profile_image', 'created_at', 'active'))
            conn.commit()
            print(f"  -> Ubačeno {min(i + batch_size, total_customers)} / {total_customers} kupaca.")

        print("Generišem 2.000.000 kompanija...")
    
        cur.execute("SELECT id FROM customer ORDER BY id ASC LIMIT 1;")
        first_customer_id = cur.fetchone()[0]
        
        total_companies = 2000000
        f_companies = io.StringIO()
        
        for i in range(1, total_companies + 1):
            cust_id = first_customer_id if i <= 100 else (first_customer_id + (i - 100))

            status = 'APPROVED'
            name = f"Firma {i} DOO"
            address = f"Adresa {i}"
            
            city = random.choice(cities_data)
            city_id = city['id']
            country_id = city['country_id']
            lat = city['lat'] + random.uniform(-0.05, 0.05)
            lon = city['lon'] + random.uniform(-0.05, 0.05)
            
            f_companies.write(f"{status}\t{name}\t{address}\t{city_id}\t{country_id}\t{cust_id}\t{lat}\t{lon}\n")
            
            if i % 100000 == 0:
                f_companies.seek(0)
                cur.copy_from(f_companies, 'companies', sep='\t', columns=('status', 'name', 'address', 'city_id', 'country_id', 'customer_id', 'latitude', 'longitude'))
                conn.commit()
                print(f" - Ubačeno {i}/{total_companies} kompanija.")
                f_companies = io.StringIO()


        print("Generišem 40.000 magacina...")
        f_warehouses = io.StringIO()
        total_warehouses = 40000
        
        for i in range(1, total_warehouses + 1):
            name = f"Warehouse NVT-{i}"
            address = f"Industrial Zone {i}"
            
            city = random.choice(cities_data)
            
            lat = city['lat'] + random.uniform(-0.1, 0.1)
            lon = city['lon'] + random.uniform(-0.1, 0.1)
            
            is_online = "false"
            version = "0"
            created_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            updated_at = created_at
            
            f_warehouses.write(f"{name}\t{address}\t{city['id']}\t{city['country_id']}\t{lat}\t{lon}\t{is_online}\t{version}\t{created_at}\t{updated_at}\n")
        
        f_warehouses.seek(0)
        cur.copy_from(f_warehouses, 'warehouses', sep='\t', columns=('name', 'address', 'city_id', 'country_id', 'latitude', 'longitude', 'is_online', 'version', 'created_at', 'updated_at'))
        conn.commit()
        print("Ubačeno 40.000 magacina.")


        print("Pripremam kreiranje sektora...")
        
        cur.execute("SELECT id, name FROM warehouses WHERE name LIKE 'Warehouse NVT-%'")
        warehouse_records = cur.fetchall()
        
        print("Generišem 3 sektora po magacinu...")
        f_sectors = io.StringIO()
        
        for w_id, w_name in warehouse_records:
            for sector_num in range(1, 4): 
                name = f"Sector {sector_num}"
                description = f"Storage Area {sector_num} for {w_name}"
                last_temp = round(random.uniform(-5.0, 30.0), 2)
                last_temp_reading_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
                version = "0"
                created_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
                updated_at = created_at
                
                f_sectors.write(f"{name}\t{description}\t{w_id}\t{last_temp}\t{last_temp_reading_at}\t{version}\t{created_at}\t{updated_at}\n")
        
        f_sectors.seek(0)
        cur.copy_from(f_sectors, 'sectors', sep='\t', columns=('name', 'description', 'warehouse_id', 'last_temperature', 'last_temperature_reading_at', 'version', 'created_at', 'updated_at'))
        conn.commit()
        
        print(f"Uspešno generisano i ubačeno {len(warehouse_records) * 3} sektora!")

        cur.close()
        conn.close()
        print("Celi proces seedovanja je uspešno završen!")

    except Exception as e:
        print(f"Došlo je do greške u izvršavanju: {e}")

if __name__ == "__main__":
    generate_relational_data()
