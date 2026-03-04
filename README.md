# 💰 OkRuppe: Customer Ledger & Transaction Manager

**Simple • Fast • Secure • Offline Android Money Manager App**

OkRuppe is a robust and intuitive Android application designed to empower **shop owners and individuals** to seamlessly manage their customer credit/debit transactions and ledger books—all without needing an internet connection.

It simplifies the process of tracking who owes you and who you owe, ensuring accurate, up-to-date balances for every customer.

---

## ✨ Features

OkRuppe provides a comprehensive suite of tools to manage your daily financial interactions.

### 🔐 Core Security & User Management
* **Login & Signup:** Secure user authentication to protect your data.
* **Secure Local Storage:** All your data is stored securely **offline** on your device.
* **Update Profile:** Easily manage and update your personal information.

### 👤 Customer Ledger Management
* **CRUD Operations:** Effortlessly **Add, Edit, and Delete** customer entries.
* **Auto-Calculated Balance:** Instant and accurate calculation of the net balance for each customer.
* **Search & Filter:** Quickly find customers using search and advanced filtering options.
* **Efficient List:** A smooth `RecyclerView` for listing and navigating customers.

### 💳 Transaction Tracking
* **"You Gave" / "You Got":** Clear distinction between money **given** (receivable) and money **got** (payable/paid).
* **Date Tracking:** Record the exact date for every transaction.
* **Auto Total Balance:** Real-time monitoring of the overall total "You Gave" and "You Got" across all customers.
* **Quick Actions:** Swipe or Long-press gestures for quick deletion of transactions.

### 📊 Reporting & Data Integrity
* **Detailed Reports:** Generate reports showing:
    * All transactions sorted chronologically by date.
    * Customer-wise transaction history.
    * Total summary of all "You Gave" and "You Got" amounts.
* **🗑 Recycle Bin:**
    * **Soft Delete:** Transactions and customers are soft-deleted (`is_deleted = 1`) for safety.
    * **Restore/Permanent Delete:** Option to restore data or permanently clear it from the Recycle Bin.

---

## 📱 App Screenshots

A quick look at OkRuppe's clean and functional interface.

### 🏁 Splash & Authentication
| Splash Screen | Signup Screen | Login Screen | My Profile |
| :---: | :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/9c33fccf-dbe0-43c9-abca-6ff05a119d65" width="200"> | <img src="https://github.com/user-attachments/assets/adbfc7cf-997f-4fa0-aa40-ac3b3085cb35" width="200"> | <img src="https://github.com/user-attachments/assets/502cba0f-bcac-4b60-b1da-770a85751478" width="200"> | <img src ="https://github.com/user-attachments/assets/b2ed6fa1-6fa0-44cb-b1ea-41c46084a1f6" width="200"> |


### 🏠 Dashboard, Customers, and Transactions
| Main Dashboard | Navigation Drawer | Customer List | Add Customer Dialog |
| :---: | :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/8f15f53a-688a-4517-ac8c-6e7341aa4cf0" width="200"> | <img src="https://github.com/user-attachments/assets/d0ea6ee7-b461-4762-9383-64dea1633d0b" width="200"> | <img src="https://github.com/user-attachments/assets/8aacfedf-e0eb-4855-990c-6e4c9eb5cb23" width="200"> | <img src="https://github.com/user-attachments/assets/937e571c-0b07-46c0-8229-8d35181d7d5a" width="200"> |

| You Gave Transaction | You Got Transaction | Filter Options |
| :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/f5d6705b-76de-4f4b-8ca9-3c76253b5427" width="200"> | <img src="https://github.com/user-attachments/assets/d8f0fc84-1f10-4f8f-ab4f-1fbe6c4d428b" width="200"> | <img src="https://github.com/user-attachments/assets/86c4a4c4-15cc-4adb-bfa8-228e2f617a54" width="200"> |

### 🗑 Reports and Recycle Bin
| Reports View | Recycle Bin |
| :---: | :---: |
| <img src="https://github.com/user-attachments/assets/0a7b40bf-ad47-4c58-8a42-07db39091f50" width="200"> | <img src="https://github.com/user-attachments/assets/eef49e92-6030-4227-b844-a491da228e51" width="200"> |

---

## 🛠 Tech Stack (Optional Section)

* **Platform:** Android Native (Java)
* **Database:** SQLite with a custom DBHelper for direct data persistence
* **User Interface:** XML Layouts and Material Design Components
* **Architecture:** Standard Android Components

## ❤️ Support the Project

If you find OkRuppe useful for your business or personal needs, please show your support!

1.  Give a ⭐ to this repository on GitHub.
2.  Spread the word!

**Thank you for checking out OkRuppe!**
