💰 OkRupee – Customer Ledger & Transaction Manager

OkRupee is a simple and efficient Android app for managing customers, tracking transactions, viewing reports, and maintaining balances.
Built using Java, SQLite, and clean Material UI components.

📸 Screenshots

(Replace these URLs with your own images. These are small preview size — ideal for README.)

Login	Dashboard	Customer Details
<img src="https://github.com/user-attachments/assets/40a63ead-43b6-469b-ab39-ace4f07671ac" width="250">	<img src="https://github.com/user-attachments/assets/abbe8d56-5e54-4d3e-8413-95f9a9bc2cd2" width="250">	<img src="https://github.com/user-attachments/assets/5d49f441-edf1-474c-90f5-455ccb92e2e7" width="250">
Add Transaction	Reports	Recycle Bin
<img src="https://github.com/user-attachments/assets/b11df486-7e3b-4137-b023-e9111b472e3b" width="250">	<img src="https://github.com/user-attachments/assets/41381d50-6274-44b7-afd0-e2ef9efe622e" width="250">	<img src="https://github.com/user-attachments/assets/demo" width="250">
🚀 Features
👤 User Authentication

Login & Signup

Update profile (name & mobile)

🧾 Customer Management

Add customer

Search customer list

Auto-update total balance

Delete customer

💳 Transaction Management

Add You Gave / You Got

Add remarks, date

Long-press delete with dialog

Auto customer balance calculation

🗑 Recycle Bin (Soft Delete)

Deleted transactions saved using is_deleted = 1

View deleted items

Restore or permanently delete

📊 Reports

Full user-wise transaction history

Customer name join

Sorted by date

Clean summary screen

🛠 Tech Stack
Component	Technology
Language	Java
Database	SQLite
UI	XML Layouts, RecyclerView
Auth	SQLite
Other	Navigation Drawer, SearchView
📂 Project Structure
OkRupee/
│
├── java/com.example.okrupee/
│   ├── activities/
│   ├── adapters/
│   ├── database/
│   │   └── DatabaseHelper.java
│   ├── models/
│   └── utils/
│
├── res/
│   ├── layout/
│   ├── drawable/
│   └── values/
│
└── AndroidManifest.xml

⚙️ Database Schema
Users Table
Column	Type
id	INTEGER PRIMARY KEY
username	TEXT
phone	TEXT
password	TEXT
Customers Table
Column	Type
id	INTEGER PRIMARY KEY
user_id	INTEGER
name	TEXT
phone	TEXT
amount	REAL
Transactions Table
Column	Type
id	INTEGER PRIMARY KEY
customer_id	INTEGER
amount	REAL
remarks	TEXT
date	TEXT
is_deleted	INTEGER (0 = active, 1 = deleted)
📥 Installation
Clone Repository
git clone https://github.com/your-username/OkRupee.git

Open in Android Studio

Open Android Studio

Select Open an existing project

Choose the cloned folder

🙌 Contribution

Pull requests are welcome!
For major changes, please open an issue before submitting changes.

📄 License

This project is free to use under MIT License.


