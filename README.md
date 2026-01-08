# ⚡ Tic-Tac-Toe Server Core

<div align="center">

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-Dashboard-4285F4?style=for-the-badge&logo=java&logoColor=white)
![Derby](https://img.shields.io/badge/Database-Apache%20Derby-red?style=for-the-badge&logo=apache&logoColor=white)
![Threads](https://img.shields.io/badge/Concurrency-Multi--Threaded-00e676?style=for-the-badge)
![JSON](https://img.shields.io/badge/Protocol-JSON-lightgrey?style=for-the-badge&logo=json&logoColor=white)

<br />

### 🧠 The Brain Behind the Ultimate Multiplayer Tic-Tac-Toe Experience  
**Real-time matchmaking · Live dashboard · Persistent game history**

</div>

---

## ✨ Overview

The **Tic-Tac-Toe Server Core** is the **central nervous system** of a real-time multiplayer Tic-Tac-Toe platform.  
It handles **player authentication**, **live matchmaking**, **game session orchestration**, and **data persistence**, all while providing a **JavaFX-powered dashboard** for live monitoring.

> Designed for **clarity**, **performance**, and **scalability**.

---

## 📊 Server Dashboard (JavaFX)

Monitor everything visually — no terminal required.

<table align="center">
  <tr>
    <td width="50%" align="center">
      <img src="https://github.com/user-attachments/assets/9614f71a-4a0f-488f-9b84-29af0212aefa" width="100%" style="border-radius:12px; box-shadow:0 0 20px rgba(0,255,240,0.4);" />
      
  </tr>
</table>

---

## 🏗️ Architecture

> **Blocking I/O – Thread-Per-Client Model**
```
Client ──▶ ServerSocket (8888)
├── ClientHandler (Thread)
├── ClientHandler (Thread)
└── ClientHandler (Thread)
```
Each connected client is handled independently to ensure:
- 🚀 Low latency
- 🧵 High responsiveness
- 🛡️ Fault isolation

---

## 🧠 Core Features

### 🧵 Multi-Threaded Networking
- Uses `ServerSocket` on **Port 8888**
- Each client runs in its own `ClientHandler` thread

### 🔐 Authentication & Profiles
- Secure Registration & Login
- Password hashing using **SHA-256**
- Player status tracking (Offline / Online / In-Game)

### 🤝 Matchmaking & Sessions
- Real-time invitations routing
- Active game session tracking
- Win / Loss / Draw resolution

### 💾 Persistent Storage
- **Apache Derby** database
- Stores:
  - Player statistics
  - Full match replays as JSON

### 📡 JSON Communication Protocol
- All communication uses structured JSON
- Powered by `org.json`
- Supports:
  - Moves
  - Invitations
  - Status updates

---

## 🗄️ Database Schema

**Database URL**
jdbc:derby://localhost:1527/TEAM1


---

### 🧑 Player Table

| Column | Type | Description |
|------|------|------------|
| `id` | int | **Primary Key** |
| `username` | VARCHAR(50) | name |
| `email` | VARCHAR(100) | Player email |
| `password_hash` | VARCHAR(255) | SHA-256 encrypted password |
| `points` | INT | Total score |
| `status` | INT | `0` Offline · `1` Online · `2` In-Game |
| `wins` | INT | Games won |
| `losses` | INT | Games lost |
| `draws` | INT | Games drawn |

---

## 🚀 Setup & Execution

### ✅ Prerequisites
Before running the server, ensure you have the following environment set up:
* **Java JDK 17+** installed.
* **Apache Derby** installed and running on port `1527`.
* **Build Tool:** Maven.
* **Required Libraries:**
    * `org.json`
    * `derbyclient.jar`
      
 ### 📥 Installation

**1️⃣ Clone Repository**
```bash
git clone [https://github.com/YourUsername/TicTacToe-Server.git](https://github.com/YourUsername/TicTacToe-Server.git)
```
### 2️⃣ Start Derby Database
Ensure your Derby server is running, then configure your connection:
* **Database Name:** `TEAM1`
* **User:** `Team1`
* **Password:** `team1`

### 3️⃣ Run Application
Navigate to the source package:
`com.mycompany.server_xo_game`

Run the main class:
`App.java` *(JavaFX Entry Point)*

---

### ▶️ Running the Server
1. Launch the **JavaFX Dashboard**.
2. Click the **Start Server** button.
3. Status indicator changes to **Server Online**.
4. Server begins listening on **Port 8888**.
5. Watch players appear live on the charts! 📊

---

## 📂 Project Structure

```
com.mycompany.server_xo_game
│
├── App.java                  # JavaFX Entry Point
├── Server.java               # ServerSocket & Thread Control
├── ServerPageController.java # Dashboard UI Logic
├── ClientHandler.java        # Per-Client Thread
├── ServerController.java     # Request Routing
├── GameSession.java          # Game Logic
├── GameSessionManager.java   # Active Matches
├── DAO.java                  # Database Access Layer
├── PlayerStatus.java         #Enum
└── PlayerModel.java          # Data Transfer Object (DTO)
```
## 👥 Contributors

Built by the **Group 1 (MAD Intake 46)**:

* **Ahmed Tayseer** 
* **Alaa Ayman** 
* **Mahmoud Tarek**  
* **Omar Amer** 


