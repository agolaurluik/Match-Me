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

  - Biographical data (min. 5 fields) & “About Me” section
  - Interests, preferences, and what the user is looking for
  - Profile picture upload (with default picture provided on registration)
  - Editable at any time

- **Location Awareness**

  - Uses browser GPS for accurate proximity
  - Users set a max connection radius
  - Distance-based filtering for recommendations
  - Users can also choose from a list of predetermined locations

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
  - Unread message indicators
  - Typing indicators 💬
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

- Load 100+ mock users with diverse bios and interests

---

## 📱 Responsive Design

Fully responsive layout. Works on mobile, tablet, and desktop.

---

## 🧭 Bonus Features

- Real-time typing indicators in chat 💬
- Online/offline presence indicators
- Proximity matching using GPS + radius filtering
- Optional: PostGIS spatial indexing for performance at scale

---

## ⚙️ Dev Setup

```bash
# Backend (Java + Spring Boot)
cd backend
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Frontend (React)
cd frontend
npm install
npm run dev
```

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
├── media/                # Stores profile images
├── docker-compose.yml    # For starting Docker
└── README.md

```

---

## 📦 Deployment

- PostgreSQL DB (locally or hosted)
- Using Docker

  To run the project on docker, first activate Docker and once it is ready, write `docker info` and verify.
  Once docker is running, `docker-compose up` will activate docker to use our compose file to successfully start the project.

---

# EXTRA - Installing Docker

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

---

## 🧪 Test Docker with Hello World

Once installed, you can test Docker with:

```bash
docker run hello-world
```

You should see a message confirming that Docker is working correctly.

---

## 📦 Additional Setup (Optional)

- To run Docker without `sudo` on Linux:

```bash
sudo usermod -aG docker $USER
newgrp docker
```

---

## 🔗 Resources

- [Official Docker Install Docs](https://docs.docker.com/get-docker/)

# EXTRA - Postman colletion for endpoint testing

Inside backendfolder/testing - we have provided for anyone interested in testing endpoints a postman collection JSON that contains all the endpoints we used for testing internally.

# EXTRA - How to setup PostgreSQL Database for Backend manually in case you decide not to use Docker

1. Install PostgreSQL with Postgis Extension and remember the username and password you chose
2. Enter in psql with your postgre user via terminal - default user is postgres

< psql -U <yourPostgreSQLusernamehere> -h localhost -p 5432> --> this will prompt your postgre user password, enter it

3. When you are succesfully logged in, check what databases are available to you already by using the command < \l >
4. If no <match-me> exists, proceed to create it by writing <CREATE DATABASE "match-me";> it will return <CREATE DATABASE> if successful
5. Now that you have created the database, connect to it by writing <\c match-me>, this will connect you to the database directly enabling data manipulation
6. Once inside match-me database, write <CREATE EXTENSION postgis;> and if successful, this will return <CREATE EXTENSION>

That is it, your database is now prepared for backend.

Before running backend make sure your database server is running by either using linux command <sudo servuce postgresql start> or <sudo systemctl start postgresql>.
Once that is done you can start backend in skip users mode with <mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.arguments="--mockUsers.skip=true">,
this will allow you to create the necessary entities via Postman and after the database is prepared - run <mvn spring-boot:run -Dspring-boot.run.profiles=dev> and it will
generate users until it has 100 in the repository.

If you need to wipe the database clean, first ensure backend is not running, and if you are already connected to match-me database, write <\c postgres> to connect to your user outside
of the database and proceed with the following instructions. If you were not connected, then log into your postgre user manually.
After entering as postgres user, instead of <CREATE DATABASE>, write <DROP DATABASE "match-me";> -- this will return <DROP DATABASE>
which means it has been successfully deleted.
After this proceed as normal with <CREATE DATABASE "match-me";> and inside it <CREATE EXTENSION postgis;> and prepare it again.
