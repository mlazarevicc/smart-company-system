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

COMPANY_STATUS = "APPROVED"

def generate_data():
    try:
        conn = psycopg2.connect(**DB_PARAMS)
        cur = conn.cursor()
        print("Povezan na bazu.")

        # -------------------------------------------------
        # 1️⃣ UČITAVANJE POTREBNIH FK PODATAKA
        # -------------------------------------------------

        cur.execute("SELECT id FROM customer LIMIT 2000000")
        customer_ids = [r[0] for r in cur.fetchall()]

        if len(customer_ids) < 2000000:
            print("Pokreni generate_data2.py prvo.")
            return

        cur.execute("SELECT id, country_id, latitude, longitude FROM cities")
        cities = cur.fetchall()

        cur.execute("SELECT id FROM manager LIMIT 1000")
        manager_ids = [r[0] for r in cur.fetchall()]

        # Ako nema make/model podataka – kreiramo osnovne
        cur.execute("SELECT COUNT(*) FROM vehicle_makes")
        if cur.fetchone()[0] == 0:
            print("Kreiram osnovne vehicle_makes i vehicle_models...")
            cur.execute("INSERT INTO vehicle_makes (name) VALUES ('Mercedes'), ('Ford'), ('Iveco')")
            conn.commit()

            cur.execute("SELECT id, name FROM vehicle_makes")
            makes = cur.fetchall()

            for make_id, name in makes:
                cur.execute(
                    "INSERT INTO vehicle_models (name, make_id) VALUES (%s, %s)",
                    (f"{name} Model A", make_id)
                )
            conn.commit()

        cur.execute("SELECT id FROM vehicle_makes")
        make_ids = [r[0] for r in cur.fetchall()]

        cur.execute("SELECT id, make_id FROM vehicle_models")
        models = cur.fetchall()

        print("FK podaci učitani.")

        # -------------------------------------------------
        # 2️⃣ GENERISANJE 2.000.000 KOMPANIJA
        # -------------------------------------------------

        print("Generišem 2.000.000 kompanija...")
        total_companies = 2000000
        batch_size = 100000

        special_customer = customer_ids[0]

        for i in range(0, total_companies, batch_size):
            f = io.StringIO()

            for j in range(i + 1, min(i + batch_size + 1, total_companies + 1)):

                city = random.choice(cities)
                owner_id = random.choice(customer_ids)

                # prvih 100 firmi idu istom kupcu
                if j <= 100:
                    owner_id = special_customer

                name = f"Company_{j}"
                address = f"Address {j}"
                created_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

                f.write(
                    f"{COMPANY_STATUS}\t{name}\t{address}\t"
                    f"{city[0]}\t{city[1]}\t"
                    f"{city[2]}\t{city[3]}\t"
                    f"{owner_id}\n"
                )

            f.seek(0)
            cur.copy_from(
                f,
                "companies",
                sep="\t",
                columns=(
                    "status",
                    "name",
                    "address",
                    "city_id",
                    "country_id",
                    "latitude",
                    "longitude",
                    "customer_id",
                ),
            )
            conn.commit()

            print(f"  -> {min(i + batch_size, total_companies)} / {total_companies}")

        print("Kompanije generisane.")

        # -------------------------------------------------
        # 3️⃣ GENERISANJE 100.000 VOZILA
        # -------------------------------------------------

        print("Generišem 100.000 delivery vozila...")
        total_vehicles = 100000
        batch_size = 10000

        for i in range(0, total_vehicles, batch_size):
            f = io.StringIO()

            for j in range(i + 1, min(i + batch_size + 1, total_vehicles + 1)):

                make_id = random.choice(make_ids)
                model_candidates = [m for m in models if m[1] == make_id]
                model_id = random.choice(model_candidates)[0]

                registration = f"BG{j:06d}"
                weight_limit = round(random.uniform(1000, 5000), 2)
                manager_id = random.choice(manager_ids)
                created_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

                f.write(
                    f"{registration}\t{weight_limit}\t"
                    f"{make_id}\t{model_id}\t"
                    f"0\t{created_at}\t{created_at}\t"
                    f"{manager_id}\tfalse\t\\N\t\\N\t\\N\n"
                )

            f.seek(0)
            cur.copy_from(
                f,
                "vehicles",
                sep="\t",
                columns=(
                    "registration_number",
                    "weight_limit",
                    "make_id",
                    "model_id",
                    "version",
                    "created_at",
                    "updated_at",
                    "created_by_manager_id",
                    "is_online",
                    "last_heartbeat",
                    "last_latitude",
                    "last_longitude",
                ),
            )
            conn.commit()

            print(f"  -> {min(i + batch_size, total_vehicles)} / {total_vehicles}")

        print("Vozila generisana.")

        cur.close()
        conn.close()
        print("✅ generate_data3.py uspešno završen.")

    except Exception as e:
        print("Greška:", e)


if __name__ == "__main__":
    generate_data()