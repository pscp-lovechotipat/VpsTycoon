# How Contribute by Nong Claude Kung :
# VPS Tycoon - Game Development Guide

## โครงสร้างโปรเจค

โปรเจคนี้เป็นเกม Tycoon ที่พัฒนาด้วย JavaFX แบ่งเป็นแพ็คเกจหลักๆ ดังนี้:

### 1. com.vpstycoon
- `GameApplication.java` - คลาสหลักที่เริ่มต้นเกม จัดการ navigation ระหว่างหน้าต่างๆ

### 2. com.vpstycoon.config
- จัดการการตั้งค่าเกม เช่น ความละเอียดหน้าจอ, เสียง
- ใช้ Jackson สำหรับ serialize/deserialize ข้อมูล

### 3. com.vpstycoon.game
- ตัวเกมหลัก จัดการ game state และ objects ในเกม
- `GameManager.java` - จัดการสถานะเกม การเซฟ/โหลด
- `GameObject.java` - คลาสพื้นฐานสำหรับวัตถุในเกม

### 4. com.vpstycoon.ui
- ส่วน UI ทั้งหมด แบ่งเป็น:
  - menu/ - หน้าเมนูต่างๆ
  - settings/ - หน้าตั้งค่า
  - game/ - หน้าเล่นเกม

### 5. com.vpstycoon.event
- ระบบ Event Bus สำหรับการสื่อสารระหว่างคอมโพเนนต์

### 6. com.vpstycoon.audio
- จัดการเสียงเพลงและเอฟเฟค

## แนวทางการพัฒนา

1. **Model-View Pattern**
   - แยก logic ออกจาก UI ชัดเจน
   - ใช้ Properties และ Bindings ของ JavaFX

2. **Event-Driven Architecture**
   - ใช้ EventBus ลดการ coupling
   - แยก concerns ด้วย event handlers

3. **Config Management**
   - ใช้ JSON สำหรับบันทึกการตั้งค่า
   - รองรับการ hot-reload

4. **Resource Management**
   - จัดการ assets (ภาพ, เสียง) แบบ lazy loading
   - มีระบบ cache

## Tips สำหรับมือใหม่

1. เริ่มจากการทำความเข้าใจ `GameApplication.java`
2. ศึกษาการทำงานของ UI ใน package ui/
3. ดูตัวอย่างการสร้าง GameObject ใน game/
4. ทดลองเพิ่ม features ใหม่โดยดู pattern จากโค้ดที่มีอยู่

# ลายละเอียดเกม Flow game เจาะลึกมากขึ้น
=== 
## Main menu : New game, Continue , Setting ,Exit
    -New game สร้างเซฟเกมใหม่ ***หากมีเซฟแล้วต้องถาม For sure?***
    -Continue เล่นเกมต่อ ***หากไม่มีเซฟแล้วต้องแจ้งไป New game***
    -Setting Set Resolotion , Music , Credit , Tutorial คลิก
    -Exit ออกเกม
## Game play
===
### Tutorial สอนวิธีการเล่น (สอนรอบเดียวแค่ New game)
***Two Options***
    1.สไลด์ Show รูปภาพวิธีเล่นให้ผู้เล่นเลื่อนเอง
    2.Event คลิกเพื่อไปต่อ
### กดตัวคอมพิวเตอร์ขึ้น Interface Desktops
***Chat***
    -ดู Request ลูกค้า แสดง Rating ของบริษัทมุมบน
    -Request Chat มีเพดาห์ Level ที่ต้องปลดโดยใช้แต้ม Marketing
    -Level Chat ที่ต้องปลดเพื่อ รับลูกค้ามากขึ้น ประเภทลูกค้าดีขึ้น ความยากRequestที่ยาก อัพเกรด




## Intellij Install
---

### กรณี download javaFX มาเองในเครื่อง

- ใส่ใน `VM OPTION` 
- ที่ `Run > Edit Configurations...` 
- กด `Modifly options...`

  ```
  --module-path "C:\javafx-sdk-23.0.2\lib" --add-modules javafx.controls,javafx.fxml
  ```
---

---

## **1) ภาพรวม (Game Overview)**

### **แนวเกม:**

- เกมแนว **Tycoon** ที่ให้ผู้เล่นบริหารธุรกิจ **VPS Hosting** ด้วยรูปแบบที่เข้าใจง่าย

### **เป้าหมายของเกม:**

1. **บริหารเซิร์ฟเวอร์**: ซื้อ, อัปเกรด, ดูแลเซิร์ฟเวอร์เพื่อรองรับลูกค้า VPS
2. **จัดการการเงิน**: รักษาสภาพคล่องทางการเงิน และทำกำไรให้ได้
3. **แก้ปัญหาแบบ Real-time**: เผชิญปัญหาสุ่ม เช่น Data Breach, VM Crash
4. **ขยายธุรกิจ**: อัปเกรดห้อง, ติดตั้ง Firewall, Router เพื่อรองรับลูกค้ารายใหญ่

### **กลไกหลักของเกม:**

- **การบริหารทรัพยากร**: วางแผนซื้อ/อัปเกรดเซิร์ฟเวอร์ และจัดการค่าใช้จ่าย
- **การตั้งค่า VPS Plans**: กำหนดแพ็กเกจ VPS เพื่อให้เหมาะสมกับตลาด
- **เหตุการณ์สุ่ม (Random Events)**: สร้างความท้าทายให้ผู้เล่น
- **การพัฒนาและขยายธุรกิจ**: ลงทุนเพิ่ม Firewall, Router, และอัปเกรดห้อง Server

---

## **2) การตั้งค่าเริ่มต้น (Starting Conditions)**

- **เงินเริ่มต้น**: 100,000 THB
- **เซิร์ฟเวอร์เริ่มต้น**: Dell PowerEdge R210 (4 Cores, 16GB RAM, 500GB HDD)
- **Overhead Cost**: 5,000 THB/เดือน (ค่าดำเนินการคงที่)
- **รอบเวลาในเกม**: 1 เดือน = 15 นาทีในเกม

---

## **3) โครงสร้างของเกม (Game Structure)**

### **3.1) เมนูหลัก (Main Menu)**

- **เริ่มเกมใหม่** (Start New Game)
- **โหลดเกม** (Load Game)
- **ตั้งค่าเกม** (Settings)
- **ออกจากเกม** (Exit)

### **3.2) ส่วนหลักของเกม**

1. **Dashboard**: แสดงข้อมูลภาพรวมของธุรกิจ (รายได้, ค่าใช้จ่าย, ความพึงพอใจของลูกค้า)
2. **Server Room**: จัดการ Rack Server (ซื้อ/อัปเกรด/ดูแล)
3. **Market**: ซื้ออุปกรณ์เพิ่มเติม เช่น Firewall, Router, อัปเกรดห้อง Server
4. **Client Management**: ดูคำสั่งซื้อ VPS จากลูกค้า และกำหนด VPS ให้พวกเขา
5. **Event Handling**: ระบบแจ้งเตือนปัญหาสุ่ม เช่น การโจมตีจาก Hacker, Server ล่ม

---

## **4) ระบบเซิร์ฟเวอร์ (Server System)**

### **ตารางเซิร์ฟเวอร์**

|**ชื่อเซิร์ฟเวอร์**|**ราคาซื้อ (บาท)**|**ค่าใช้จ่ายรายเดือน (บาท)**|**CPU Cores**|**RAM (GB)**|**Storage**|**Network Port**|
|---|---|---|---|---|---|---|
|Dell PowerEdge R210|20,000|2,000|4|16|500GB HDD|1 Gbps (Shared)|
|Dell PowerEdge R310|35,000|3,000|6|32|1TB HDD|1 Gbps (Shared)|
|Dell PowerEdge R410|50,000|4,000|8|64|1TB SSD|1 Gbps (Shared)|

---

## **5) ระบบมัลติเธรด (Multithreading System)**

### **การใช้งาน Multithreading ในเกม**

- **CustomerSimulator (Thread)**: จำลองลูกค้าที่สั่งซื้อ VPS
- **ServerMonitor (Thread)**: ตรวจสอบสถานะเซิร์ฟเวอร์และแจ้งเตือนปัญหา
- **EventProcessor (Thread)**: จัดการเหตุการณ์สุ่ม เช่น DDoS, ไฟดับ

### **ตัวอย่างโค้ด Multithreading**

```java
class CustomerSimulator extends Thread {
    public void run() {
        while (true) {
            System.out.println("ลูกค้าใหม่ขอเช่า VPS...");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
```

---

## **6) แผนผังคลาส (Class Diagram - Java)**

```mermaid
classDiagram
    class Game {
        +startGame()
        +manageResources()
    }
    
    class Server {
        <<abstract>>
        +name: String
        +cpuCores: int
        +ram: int
        +storage: int
        +monthlyCost: int
        +deployVPS()
        +upgrade()
    }
    
    class DellPowerEdgeR210 {
        +upgrade() override
    }
    
    class VPS {
        +vCPU: int
        +ram: int
        +storage: int
        +price: int
    }
    
    class Client {
        +name: String
        +vps: VPS
        +requestVPS()
    }
    
    class Market {
        +purchaseServer(Server server)
        +upgradeRoom()
    }
    
    class EventHandler {
        +triggerEvent()
        +resolveIssue()
    }
    
    class CustomerSimulator {
        +run() override
    }
    
    Game "1" -- "*" Server : manages
    Server <|-- DellPowerEdgeR210
    Server "1" -- "*" VPS : hosts
    Client "1" -- "1" VPS : owns
    Game "1" -- "1" Market : includes
    Game "1" -- "1" EventHandler : handles
    Game "1" -- "1" CustomerSimulator : runs
```

---

## ToDo
- [x] Main Menu
- [x] Pause Menu
    - [x] New Game btn
    - [x] Resume btn
- [x] Setting Screen
    - [x] Resolution
    - [x] Fullscreen 
- [x] Empty Screen


