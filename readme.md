# Match-Me Web

**Match-Me Web** is a full-stack, real-time recommendation platform built to connect users based on shared interests, proximity, and personal preferences. Whether you're seeking a dog-walking companion, professional contact, or a board game buddy, Match-Me helps you find meaningful connections with people nearby.

---

## 🚀 Features

### ✅ Core Functionalities

- **User Registration & Login**

  - Secure password handling with bcrypt + salt
  - JWT-based authentication
  - Global logout from any page

- **User Profiles**

  - Biographical data (with 7 fields) & “About Me” section
  - Interests, Personality traits, Nationality, Age, Location, and what is the user's purpose when using this platform
  - Profile picture upload (with default picture provided on registration)
  - Editable at any time

- **Location Awareness**

  - Uses browser GPS for accurate proximity or a predetermined Location within Estonia
  - Users set a max connection radius
  - Distance-based filtering for recommendations

- **Smart Recommendations**

  - Ranked, data-driven matching algorithm
  - Max 9 high-quality recommendations at once
  - Users can connect and/or dismiss (no re-recommendation)

- **Connections**

  - Send/receive connection requests
  - Accept/dismiss requests
  - Disconnect at any time

- **Real-Time Chat**

  - One chat per connection
  - Instant messaging with real-time updates (WebSocket)
  - Unread message indicator
  - Typing indicator 💬
  - Message timestamps
  - Paginated chat history
  - Chat list ordered by recent activity

- **Online Presence**

  - Online/offline status indicator for users

- **RESTful API Endpoints**
  - `/users/{id}`, `/users/{id}/profile`, `/users/{id}/bio`
  - `/me`, `/me/profile`, `/me/bio`
  - `/recommendations`, `/connections`
  - All endpoints strictly return permitted, non-auth data

---

## 🛠 Tech Stack

- **Frontend**: React
- **Backend**: Java + Spring Boot
- **Database**: PostgreSQL
- **Authentication**: JWT
- **Password Security**: bcrypt
- **Geospatial Support**: [PostGIS](https://postgis.net/) for efficient proximity queries

---

## 🧪 Data Seeding

- 100+ mock users with diverse biographical data are provided within the init.sql for Docker, or generated on start-up (assuming database is ready).

---

## 📱 Responsive Design

Fully responsive layout. Works on mobile, tablet, and desktop.

---

## 🔐 Security & Privacy

- Email is private and visible only to the authenticated user
- Profiles are visible _only_ via:
  - Recommendations
  - Connection requests
  - Active connections

---

## 📂 Folder Structure (Simplified)

```
match-me-web/
├── backend/              # Spring Boot backend
├── docker/
│   └── postgres/         # SQL schema for Docker
├── frontend/             # React frontend
├── media/                # Stores profile images - this includes the default picture inside the public folder
├── docker-compose.yml    # For starting Docker
└── README.md

```

---

## 📦 Deployment

- Using Docker

- To run the project on docker, first activate Docker and once it is ready, write

```bash
docker run hello-world
```

You should see a message confirming that Docker is working correctly.

- To start the project using Docker, run:

```bash
docker-compose up
```

- To stop the containers, run:

```bash
docker-compose down
```

- To restart the containers, run:

```bash
docker-compose restart
```

- Or alternatively you can install a docker extension for your IDE that can do the command for you. Make sure you enable that extension in your workspace.

⚠️ **Important:** Every time you restart the project inside docker, it wipes the data clean so please keep it in mind when testing, it always reverts to the original init.sql
database file we have inside docker/postgres. There is probably a way to have it save, but for now only launching manually saves data across uses.

---

# Installing Docker

Follow the steps below to install Docker on your system.

---

## 🐧 Linux (Ubuntu, Debian, Fedora, CentOS, and more)

The easiest way to install Docker on most Linux distributions is to use Docker's official convenience script.

**Run this single command in your terminal:**

````bash
curl -fsSL https://get.docker.com | sudo sh

---

## 🍎 macOS (Intel or Apple Silicon)

1. Download Docker Desktop from the official website:
   https://www.docker.com/products/docker-desktop/

2. Open the `.dmg` file and drag Docker to your Applications folder.

3. Launch Docker Desktop and follow the setup instructions.

4. Verify installation:

```bash
docker --version
````

---

## 🪟 Windows 10/11 (Pro, Enterprise, Education)

1. Download Docker Desktop for Windows:
   https://www.docker.com/products/docker-desktop/

2. Run the installer and follow the prompts. WSL 2 is required (installer will guide you if it's missing).

3. Reboot your machine if needed.

4. Start Docker Desktop from the Start menu.

5. Verify installation:

```powershell
docker --version
```

## 🔗 Resources

- [Official Docker Install Docs](https://docs.docker.com/get-docker/)

## ⚙️ Dev Setup for manual activation (without Docker)

- PostgreSQL DB (locally or hosted) - the activation methods for postgresql can vary,
  so find the one that activates the server on your machine.
  (On Ubuntu

  ```bash
   sudo systemctl start postgresql
  ```

  or

  ```bash
  sudo service postgresql start
  ```

  worked for us)

- If you are going to activate it manually, you need to first prepare the database for user generation. Proceed to the EXTRA section to see how to setup postgreSQL then return here.
  After installing dependencies with `mvn clean install` inside backend, run

  ```bash mvn
  spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.arguments="--mockUsers.skip=true"
  ```

  This command skips user generation, allowing you to use our postman to generate each entity's data and after all 6 are filled, you can proceed with the normal launch using

  ```bash
  mvn spring-boot:run -Dspring-boot.run.profiles=dev
  ```

  and a seeder should create 100 users with your created data fields.

  At the moment both dev profile and production profile run identically - that is they both create 100 users on start-up provided their respective databases are ready, which is
  why you need to have a special argument to skip user-generation in dev mode to allow the backend to build the data for postgre before launching.

  ```

  ```

```bash
# Backend (Java + Spring Boot)
cd backend
mvn clean install
mvn spring-boot:run

# Frontend (React)
cd frontend
npm install
npm run dev
```

# EXTRA - Postman collection for endpoint testing

Inside backend/testing - we have provided for anyone interested in testing endpoints a postman collection JSON that contains all the endpoints we used for testing internally.

# EXTRA - How to setup PostgreSQL Database for Backend manually in case you decide not to use Docker

1. Install PostgreSQL with Postgis Extension and remember the username and password you chose
2. Enter in psql with your postgre user via terminal - default user is postgres

` psql -U <yourPostgreSQLusernamehere> -h localhost -p 5432` --> this will prompt your postgre user password, enter it.
(This command starts postgreSQL interactive terminal with your postgreSQL username, connects to localhost(your machine) under default port 5432)

3. When you are succesfully logged in, check what databases are available to you already by using the command `\l `
4. If no `match-me` exists, proceed to create it by writing `CREATE DATABASE "match-me";` it will return `CREATE DATABASE` if successful
5. Now that you have created the database, connect to it by writing `\c match-me`, this will connect you to the database directly enabling data manipulation
6. Once inside match-me database, write `CREATE EXTENSION postgis;` and if successful, this will return `CREATE EXTENSION`, this enables our postgreSQL database to use geospatial data.

That is it, your database is now ready to receive entities, after using postman to fill the required entities data (6 of them), you can launch as normal and it will generate 100 mock users.

If you need to wipe the database clean, first ensure backend is not running, and if you are already connected to match-me database, write <\c postgres> to connect to your user outside
of the database and proceed with the following instructions. If you were not connected, then log into your postgre user again manually.
After entering as postgres user, instead of `CREATE DATABASE`, write `DROP DATABASE "match-me";` -- this will return `DROP DATABASE`
which means it has been successfully deleted.
After this proceed as normal with `CREATE DATABASE "match-me";` and inside it `CREATE EXTENSION postgis;`and prepare it again.
