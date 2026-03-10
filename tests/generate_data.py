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

# Kategorije iz tvog data.sql
VALID_CATEGORIES = ['SOFT_DRINKS', 'JUICES', 'WATER', 'DAIRY', 'PLANT_BASED', 'SPORTS_DRINKS', 'TEA']

def generate_relational_data():
    try:
        conn = psycopg2.connect(**DB_PARAMS)
        cur = conn.cursor()
        print("Povezan na bazu.")

        # --- 0. UCITAVANJE GRADOVA IZ CSV FAJLA ---
        cities_data = []
        try:
            with open('cities.csv', mode='r', encoding='utf-8') as f:
                reader = csv.DictReader(f)
                for row in reader:
                    cities_data.append({
                        'id': row['id'],
                        'country_id': row['country_id'],
                        'lat': row.get('latitude', 44.0),
                        'lon': row.get('longitude', 20.0)
                    })
            print(f"Učitano {len(cities_data)} gradova iz CSV-a.")
        except Exception as e:
            print(f"Greška pri čitanju cities.csv: {e}")
            return

        # --- 1. GENERISANJE MENADŽERA (1.000.000) ---
        print("Generišem 1.000.000 menadžera u grupama...")
        total_managers = 1000000
        batch_size = 100000
        for i in range(0, total_managers, batch_size):
            f = io.StringIO()
            for j in range(i, min(i + batch_size, total_managers)):
                f.write(f"First{j}\tLast{j}\tuser{j}\temail{j}@factory.com\t$2a$04$qmVy/ONyW2fXpQDMYcHnpOuVQlw/5Hi6xJMCJFCDEoIaS9Zyqnik6\tMANAGER\thttp://localhost:8080/static/defaults/user-avatar.webp\t{datetime.now()}\ttrue\tfalse\tfalse\ttrue\n")
            f.seek(0)
            cur.copy_from(f, 'manager', sep='\t', columns=('first_name', 'last_name', 'username', 'email', 'password', 'role', 'profile_image', 'created_at', 'active', 'is_blocked', 'is_supermanager', 'reset_password'))
            conn.commit()

        cur.execute("SELECT id FROM manager LIMIT 1000")
        manager_ids = [r[0] for r in cur.fetchall()]

        # --- 2. GENERISANJE PROIZVODA (1000) ---
        print("Generišem proizvode...")
        f_products = io.StringIO()
        for i in range(1000):
            sku = f"PROD-{i:04d}"
            f_products.write(f"{sku}\tProduct {i}\tDescription {i}\t{random.choice(VALID_CATEGORIES)}\t{round(random.uniform(1.0, 5.0), 2)}\t0.5\t\\N\ttrue\t{datetime.now()}\t{datetime.now()}\t{random.choice(manager_ids)}\t0\tfalse\n")
        f_products.seek(0)
        cur.copy_from(f_products, 'products', sep='\t', columns=('sku', 'name', 'description', 'category', 'price', 'weight', 'image_url', 'is_available', 'created_at', 'updated_at', 'created_by_manager_id', 'version', 'is_deleted'))
        conn.commit()

        # --- 3. GENERISANJE FABRIKA (20.000) ---
        print("Generišem 20.000 fabrika koristeći gradove iz CSV-a...")
        f_factories = io.StringIO()
        for i in range(20000):
            city = random.choice(cities_data)
            mgr_id = random.choice(manager_ids)
            f_factories.write(f"Factory_{i}\tStreet {i}\t{city['id']}\t{city['country_id']}\t{city['lat']}\t{city['lon']}\tfalse\t\\N\t0\t{datetime.now()}\t{datetime.now()}\t{mgr_id}\n")
        
        f_factories.seek(0)
        cur.copy_from(f_factories, 'factories', sep='\t', columns=('name', 'address', 'city_id', 'country_id', 'latitude', 'longitude', 'is_online', 'last_heartbeat', 'version', 'created_at', 'updated_at', 'created_by_manager_id'))
        conn.commit()

        # --- 4. POVEZIVANJE (PRODUCT_FACTORY) ---
        print("Povezujem 1000 fabrika sa po 10 proizvoda...")
        cur.execute("SELECT id FROM factories ORDER BY id LIMIT 1000")
        factory_ids = [r[0] for r in cur.fetchall()]
        cur.execute("SELECT id FROM products LIMIT 100")
        product_ids = [r[0] for r in cur.fetchall()]

        f_pf = io.StringIO()

        for f_id in factory_ids:
            selected_prods = random.sample(product_ids, 10)
            for p_id in selected_prods:
                f_pf.write(f"{p_id}\t{f_id}\n")
        f_pf.seek(0)
        cur.copy_from(f_pf, 'product_factory', sep='\t', columns=('product_id', 'factory_id'))
        conn.commit()

        # --- 4. POVEZIVANJE (FACTORY_PRODUCT_QUANTITIES) ---
        print("Povezujem 1000 fabrika sa po 10 proizvoda i postavljam količinu na 10000...")
        cur.execute("SELECT id FROM factories ORDER BY id LIMIT 10000")
        factory_ids = [r[0] for r in cur.fetchall()]
        cur.execute("SELECT id FROM products LIMIT 1000")
        product_ids = [r[0] for r in cur.fetchall()]

        f_fpq = io.StringIO()
        for f_id in factory_ids:
            selected_prods = random.sample(product_ids, 10)
            for p_id in selected_prods:
                # Kolone su: factory_id, product_id, quantity, version
                f_fpq.write(f"{f_id}\t{p_id}\t10000\t0\n")
        
        f_fpq.seek(0)
        # Promenjeno ime tabele na 'factory_product_quantities' prema Spring modelu
        cur.copy_from(f_fpq, 'factory_product_quantities', sep='\t', columns=('factory_id', 'product_id', 'quantity', 'version'))
        conn.commit()

        print("Sve je uspešno završeno!")
        cur.close()
        conn.close()

    except Exception as e:
        print(f"Greška: {e}")

if __name__ == "__main__":
    generate_relational_data()
