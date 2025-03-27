# 🎮 VPS TYCOON

## 📋 สารบัญ (Table of Contents)

- [📖 รายละเอียดโปรเจค (Project Overview)](#project-overview)
- [🎮 รายละเอียดเกม (Game Details)](#game-details)
- [⚙️ การติดตั้ง (Installation)](#installation)
- [🤝 วิธีการมีส่วนร่วม (How to Contribute)](#how-to-contribute)
- [✅ สิ่งที่ต้องทำ (Todo)](#todo)

<a id="project-overview"></a>
## 📖 รายละเอียดโปรเจค (Project Overview)

VPS Tycoon เป็นเกมแนว Tycoon ที่พัฒนาด้วย JavaFX ให้ผู้เล่นได้บริหารธุรกิจ VPS Hosting

### โครงสร้างโปรเจค (Project Structure)

```
com.vpstycoon/
├── GameApplication.java    - คลาสหลักที่เริ่มต้นเกม จัดการ navigation
├── config/                 - การตั้งค่าเกม (ความละเอียดหน้าจอ, เสียง)
├── game/                   - ตัวเกมหลัก, GameState, GameManager
├── ui/                     - ส่วน UI และหน้าจอต่างๆ
├── event/                  - ระบบ Event Bus
├── audio/                  - ระบบเสียงเพลงและเอฟเฟค
└── screen/                 - จัดการหน้าจอ และ resolution
```

### เทคโนโลยีที่ใช้ (Technologies)

- **Java 23** - ภาษาหลักที่ใช้พัฒนา
- **JavaFX 23** - สำหรับ UI และกราฟิก
- **Jackson 2.13.4** - สำหรับจัดการ JSON
- **Maven** - เครื่องมือจัดการ dependencies

<a id="game-details"></a>
## 🎮 รายละเอียดเกม (Game Details)

### แนวเกม (Game Genre)
เกมแนว Tycoon ที่ให้ผู้เล่นบริหารธุรกิจ VPS Hosting

### เป้าหมายของเกม (Game Goals)
1. **บริหารเซิร์ฟเวอร์**: ซื้อ, อัปเกรด, ดูแลเซิร์ฟเวอร์เพื่อรองรับลูกค้า VPS
2. **จัดการการเงิน**: รักษาสภาพคล่องทางการเงิน และทำกำไร
3. **แก้ปัญหาแบบ Real-time**: เผชิญกับปัญหาสุ่ม เช่น Data Breach, VM Crash
4. **ขยายธุรกิจ**: อัปเกรดห้อง, ติดตั้ง Firewall, Router เพื่อรองรับลูกค้ารายใหญ่

### กลไกหลักของเกม (Core Mechanics)
- **การบริหารทรัพยากร**: วางแผนซื้อ/อัปเกรดเซิร์ฟเวอร์ และจัดการค่าใช้จ่าย
- **การตั้งค่า VPS Plans**: กำหนดแพ็กเกจ VPS ให้เหมาะสมกับตลาด
- **เหตุการณ์สุ่ม (Random Events)**: สร้างความท้าทายให้ผู้เล่น
- **ระบบพัฒนา (Development Points)**: ใช้แต้มเพื่อพัฒนาธุรกิจใน 4 ด้าน
  - 🔴 Deploy: ลดเวลาการ Deploy VPS, ปลดล็อคระบบปฏิบัติการใหม่
  - 🔵 Network: เพิ่ม Bandwidth, ลด Network Congestion
  - 🟣 Security: ลดโอกาส Data Breach, เพิ่มตัวเลือก Firewall
  - 🟢 Marketing: เพิ่มช่องทางการตลาด, เพิ่มโอกาสได้ Rating สูง

### Game Flow
1. **Main Menu**: New game, Continue, Setting, Exit
2. **Gameplay**:
   - เลือกรับคำขอจากลูกค้า
   - Optimize VPS (เลือก OS, Security, Performance Tuning, Backup, Monitoring)
   - จัดการกับ Events ที่เกิดขึ้นระหว่างการให้บริการ
   - สรุปผลการให้บริการ (ได้รับ Points, Rating)
3. **Market**: อัพเกรดอุปกรณ์และบริษัท

<a id="installation"></a>
## ⚙️ การติดตั้ง (Installation)

### ความต้องการระบบ (Requirements)
- Java 23 หรือสูงกว่า
- JavaFX 23
- Maven

### วิธีติดตั้ง (Setup Instructions)

1. **Clone โปรเจค**
```
git clone https://github.com/yourusername/vps-tycoon.git
cd vps-tycoon
```

2. **Build ด้วย Maven**
```
mvn clean package
```

3. **รันเกม**
```
mvn javafx:run
```

### การตั้งค่า Intellij IDEA

สำหรับการใช้งานกับ IntelliJ IDEA กรณีติดตั้ง JavaFX แยก:

1. ไปที่ `Run > Edit Configurations...`
2. กด `Modify options...`
3. ใส่ใน VM options:
```
--module-path "C:\path\to\javafx-sdk-23\lib" --add-modules javafx.controls,javafx.fxml,javafx.media
```

<a id="how-to-contribute"></a>
## 🤝 วิธีการมีส่วนร่วม (How to Contribute)

1. **Fork โปรเจค**
2. **สร้าง branch ใหม่**
   ```
   git checkout -b feature/your-feature-name
   ```
3. **Commit การเปลี่ยนแปลง**
   ```
   git commit -m "เพิ่มฟีเจอร์: รายละเอียด"
   ```
4. **Push ไปยัง branch ของคุณ**
   ```
   git push origin feature/your-feature-name
   ```
5. **สร้าง Pull Request**

### แนวทางการพัฒนา (Development Guidelines)

1. **Model-View Pattern**: แยก logic ออกจาก UI ชัดเจน
2. **Event-Driven Architecture**: ใช้ EventBus ลดการ coupling
3. **Config Management**: ใช้ JSON สำหรับบันทึกการตั้งค่า
4. **Resource Management**: จัดการ assets (ภาพ, เสียง) แบบ lazy loading

```mermaid
classDiagram
direction BT
class AttackNode {
+ AttackNode(Shape, String)
- boolean neutralized
- Shape shape
- String type
+ stopPulseAnimation() void
+ neutralize() void
  String type
  Shape shape
  boolean neutralized
  }
  class AudioManager {
+ AudioManager()
- double musicVolume
- double sfxVolume
+ playMusic(String) void
- onSettingsChanged(SettingsChangedEvent) void
+ resumeMusic() void
+ playSoundEffect(String) void
+ pauseMusic() void
+ stopSoundEffect(String) void
+ dispose() void
  double musicVolume
  double sfxVolume
  }
  class BackupSystem {
  <<enumeration>>
- BackupSystem(double)
- double score
+ valueOf(String) BackupSystem
+ values() BackupSystem[]
  double score
  }
  class ButtonUtils {
+ ButtonUtils()
+ createModalButton(String) Button
+ createButton(String) Button
  }
  class CalibrationTask {
+ CalibrationTask()
# initializeTaskSpecifics() void
}
class CenterNotificationController {
+ CenterNotificationController(CenterNotificationModel, CenterNotificationView)
- CenterNotificationView view
+ push(String, String) void
+ pushAutoClose(String, String, String, long) void
+ push(String, String, String) void
  CenterNotificationView view
  }
  class CenterNotificationModel {
+ CenterNotificationModel()
+ hasNotifications() boolean
+ addNotification(Notification) void
  Notification nextNotification
  }
  class CenterNotificationView {
+ CenterNotificationView()
- CenterNotificationModel model
- AudioManager audioManager
- playFadeInAnimation(VBox) void
+ addNotificationPane(String, String, String) void
- createAndShowNotification(String, String, String) void
+ addNotificationPane(String, String) void
- showNextNotification() void
- createTaskNotificationPane(String, String, Image, Runnable) VBox
- createNotificationPane(String, String, Image) VBox
+ addNotificationPaneAutoClose(String, String, String, long) void
- fadeOutAndRemove(StackPane) void
+ createAndShowTaskNotification(String, String, String, Runnable) void
- showNextNotificationAutoClose(long) void
  CenterNotificationModel model
  AudioManager audioManager
  }
  class ChatAreaView {
+ ChatAreaView(ChatHistoryManager)
- VBox messagesBox
- Button archiveButton
- Button assignVMButton
- Button sendButton
- TextField messageInput
- addUserMessageFromHistory(String) void
+ addCustomerMessage(CustomerRequest, String) void
+ addUserMessage(String) void
+ addSystemMessage(String) void
+ clearMessages() void
- addSystemMessageFromHistory(String) void
- addCustomerMessageFromHistory(CustomerRequest, String) void
+ updateChatHeader(CustomerRequest) void
+ loadChatHistory(CustomerRequest) void
  Button assignVMButton
  TextField messageInput
  VBox messagesBox
  Button sendButton
  Button archiveButton
  }
  class ChatHistoryManager {
+ ChatHistoryManager()
- ChatHistoryManager instance
- MessengerController messengerController
- loadChatHistoryFromFile() Map~CustomerRequest, List~ChatMessage~~
+ addMessage(CustomerRequest, ChatMessage) void
+ saveChatHistory() void
+ clearChatHistory() void
- saveChatHistoryToFile() void
- loadChatHistoryFromGameState() Map~CustomerRequest, List~ChatMessage~~
- saveChatHistoryToGameState() void
+ resetInstance() void
+ updateCustomerRequestReferences() void
+ deleteChatHistoryFile() boolean
+ getChatHistory(CustomerRequest) List~ChatMessage~
  ChatHistoryManager instance
  MessengerController messengerController
  }
  class ChatMessage {
+ ChatMessage(MessageType, String, Map~String, Object~)
- Map~String, Object~ metadata
- String content
- MessageType type
- long timestamp
  long timestamp
  String content
  MessageType type
  Map~String, Object~ metadata
  }
  class ChatSystem {
+ ChatSystem()
- int chatLevel
- List~CustomerType~ unlockedCustomerTypes
+ upgradeChatLevel() void
+ unlockCustomerType(CustomerType, int) void
+ getPointsForCustomerType(CustomerType) int
- initializeCustomerTypes() void
  List~CustomerType~ unlockedCustomerTypes
  int chatLevel
  }
  class Circle {
+ Circle(double)
  }
  class CircleStatusButton {
+ CircleStatusButton(String, Color, Color, GameplayContentPane)
- Runnable onClickAction
- int skillLevel
- int skillPoints
- SkillType skillType
- VBox container
- getColorForSkill(String) Color
- resolveSkillType(String) SkillType
- createCyberButton(Color, Color) StackPane
- createContainer(String, Color, Color) VBox
- createCyberLabel(String, Color) Label
- styleButton(Button, Color, boolean) void
- openUpgradePanel() void
- toRgbString(Color) String
  SkillType skillType
  VBox container
  int skillPoints
  Runnable onClickAction
  int skillLevel
  }
  class Company {
+ Company()
- long money
- int marketingPoints
- int failedRequests
- int skillPointsAvailable
- int availableVMs
- int completedRequests
- double rating
- String name
- long totalRevenue
- int customerSatisfaction
- long totalExpenses
+ addRatingObserver(RatingObserver) void
- notifyRatingObservers() void
+ recordCompletedRequest(int) void
- updateRating() void
+ calculateVMAssignmentRatingChange(int, int, int, int, int, int) double
- readObject(ObjectInputStream) void
- findTierIndex(int, int[]) int
+ addSkillPoints(int) void
+ removeRatingObserver(RatingObserver) void
+ addMoney(double) void
+ recordFailedRequest() void
- calculateSpecRatingImpact(int, int, double) double
- calculateSpecTierImpact(int, int, int[]) double
  int marketingPoints
  int failedRequests
  int starRating
  int skillPointsAvailable
  int availableVMs
  int completedRequests
  double rating
  String name
  long totalExpenses
  long totalRevenue
  long profit
  int customerSatisfaction
  long money
  }
  class ConfigSerializer {
+ ConfigSerializer()
+ loadFromFile() GameConfig
+ saveToFile(GameConfig) void
  }
  class Customer {
+ Customer(String, CustomerType, double)
- String name
# double budget
- int id
# CustomerType customerType
String name
int id
CustomerType customerType
double budget
}
class CustomerRequest {
+ CustomerRequest(CustomerType, RequestType, double, int)
+ CustomerRequest(CustomerType, RequestType, double, int, int, int, int, RentalPeriodType)
- int requiredVCPUs
- int duration
- long creationTime
- long lastPaymentTime
- RentalPeriodType rentalPeriodType
- RequestType requestType
- boolean isExpired
- int requiredDiskGB
- double monthlyPayment
- boolean isActive
- int requiredRamGB
+ hashCode() int
+ recordPayment(long) void
+ isPaymentDue(long) boolean
+ markAsExpired() void
+ assignToVM(String) void
+ equals(Object) boolean
+ unassignFromVM() void
- calculateMonthlyPayment() double
+ activate(long) void
+ deactivate() void
  RentalPeriodType rentalPeriodType
  int requiredVCPUs
  long lastPaymentTime
  double monthlyPayment
  int duration
  boolean isExpired
  String assignedVmId
  int requiredRamGB
  int requiredDiskGB
  RequestType requestType
  long creationTime
  int rentalPeriod
  String title
  long rentalStartTimeMs
  CustomerType customerType
  String requiredRam
  boolean isActive
  String requiredDisk
  double paymentAmount
  boolean assignedToVM
  double budget
  }
  class CustomerType {
  <<enumeration>>
- CustomerType(int, String)
- int requiredPoints
- String displayName
+ valueOf(String) CustomerType
+ values() CustomerType[]
  String displayName
  int requiredPoints
  }
  class CutsceneScreen {
+ CutsceneScreen(GameConfig, ScreenManager, Navigator)
- setupUI() void
- skipCutscene() void
- playCutscene() void
- setupSkipButton() void
  }
  class DashboardView {
+ DashboardView()
+ updateDashboard(double, int, int, int) void
- createCard(String) VBox
  }
  class DashboardWindow {
+ DashboardWindow(Company, VPSManager, RequestManager, Runnable)
- setupUI() void
- createStatCard(String, String, String) VBox
- applyCyberPerformanceColors(LineChart~String, Number~) void
- createRevenueChart() LineChart~String, Number~
- startDataUpdates() void
- calculateTotalVMs() int
- calculateAverageRating() double
- updateChartData(double) void
- calculateMonthlyRevenue() double
+ updateDashboard() void
- styleWindow() void
- calculateUptime() double
- applyCyberSeriesColors(LineChart~String, Number~) void
- createPerformanceChart() LineChart~String, Number~
  LineChart~String, Number~ seriesColors
  }
  class DataDecryptionTask {
+ DataDecryptionTask(int)
+ DataDecryptionTask()
- generateClues(GridPane) void
- generateMathClue(GridPane, int) void
# initializeTaskSpecifics() void
- getIndexOfSymbol(int) int
- checkSolution() void
- generateSymbolMapping() void
  }
  class DataPacket {
+ DataPacket(int)
- int priority
- StackPane node
- getPriorityColor(int) Color
  int priority
  StackPane node
  }
  class DataSortingTask {
+ DataSortingTask()
# initializeTaskSpecifics() void
- checkTaskCompletion() void
  }
  class DateController {
+ DateController(DateModel, DateView)
+ onTimeChanged(LocalDateTime, long) void
+ onRentalPeriodCheck(CustomerRequest, RentalPeriodType) void
  }
  class DateModel {
+ DateModel(LocalDateTime, GameplayContentPane)
- ObjectProperty~LocalDateTime~ date
+ dateProperty() ObjectProperty~LocalDateTime~
+ timeRemainingProperty() StringProperty
- updateTimeRemaining() void
  LocalDateTime date
  }
  class DateView {
+ DateView(GameplayContentPane, DateModel)
+ initializeUI() void
  }
  class DebugOverlayManager {
+ DebugOverlayManager()
- VBox debugOverlay
+ toggleDebug() void
+ updateGameInfo(StackPane) void
+ startTimer() void
- updateFPS(long) void
+ updateMousePosition(double, double) void
+ stopTimer() void
  VBox debugOverlay
  }
  class DefaultGameConfig {
- DefaultGameConfig()
- double musicVolume
- DefaultGameConfig instance
- double sfxVolume
- boolean isFullscreen
- boolean vsyncEnabled
+ save() void
+ load() void
  DefaultGameConfig instance
  boolean isFullscreen
  ScreenResolution resolution
  double sfxVolume
  boolean vsyncEnabled
  double musicVolume
  }
  class DesktopIcon {
+ DesktopIcon(String, String, Runnable)
  }
  class DesktopScreen {
+ DesktopScreen(double, int, ChatSystem, RequestManager, VPSManager, Company, GameplayContentPane, GameTimeManager)
- openChatWindow() void
- openMarketWindow() void
+ addExitButton(Runnable) void
- openDashboardWindow() void
- setupUI() void
- closeChatWindow() void
  }
  class EventEffect {
  <<Interface>>
+ apply(Company) String
  }
  class EventListener~T~ {
  <<Interface>>
+ onEvent(T) void
  }
  class EventType {
  <<enumeration>>
- EventType(String, String, long, double)
- double scaleFactor
- String solution
- String displayName
- long baseCost
+ values() EventType[]
+ valueOf(String) EventType
+ calculateCost(Random) long
  double scaleFactor
  String displayName
  String solution
  long baseCost
  }
  class EventType {
  <<enumeration>>
+ EventType()
+ values() EventType[]
+ valueOf(String) EventType
  }
  class FileRecoveryTask {
+ FileRecoveryTask()
# initializeTaskSpecifics() void
}
class FirewallDefenseTask {
+ FirewallDefenseTask()
# initializeTaskSpecifics() void
- stopAllTimelines() void
- createRandomAttack(Pane) AttackNode
- decreaseFirewallIntegrity(Rectangle, Text) void
  }
  class FontLoader {
+ FontLoader()
+ loadFont(double) Font
  }
  class GameApplication {
+ GameApplication()
- initializeGame() void
- startGame(GameState) void
- shutdown() void
- showAlert(String, String) void
+ start(Stage) void
+ continueGame() void
+ showInGameSettings() void
- createGameConfig() GameConfig
- loadGameState(GameState) void
- showCutscene() void
- createScreens() void
+ showMainMenu() void
+ showSettings() void
+ showPlayMenu() void
+ showLoadGame() void
+ startNewGame() void
+ main(String[]) void
  }
  class GameConfig {
  <<Interface>>
+ save() void
+ load() void
  ScreenResolution resolution
  double sfxVolume
  boolean vsyncEnabled
  double musicVolume
  boolean fullscreen
  }
  class GameEvent {
+ GameEvent(GameplayContentPane, GameState)
- int completedTaskCount
- int failedTaskCount
- boolean debugMode
- boolean isRunning
- StackPane taskOverlay
- showTask(GameTask) void
+ debugTriggerTask() void
- initializeTaskOverlay() void
- formatTime(long) String
- triggerRandomTask() void
+ debugTriggerSpecificTask(int) void
+ run() void
+ stopEvent() void
  long timeUntilNextTask
  int failedTaskCount
  int completedTaskCount
  boolean debugMode
  StackPane taskOverlay
  boolean isRunning
  }
  class GameEventBus {
- GameEventBus()
- GameEventBus instance
+ publish(T) void
+ unsubscribe(Class~T~, EventListener~T~) void
+ subscribe(Class~T~, EventListener~T~) void
  GameEventBus instance
  }
  class GameFlowManager {
+ GameFlowManager(GameSaveManager, List~GameObject~)
+ saveGame() void
+ stopAllGameObjects() void
  }
  class GameManager {
- GameManager()
- RequestGenerator requestGenerator
- List~VPSOptimization~ installedServers
- VPSInventory vpsInventory
- boolean gameRunning
- RequestManager requestManager
- GameManager instance
- GameTimeManager timeManager
+ saveState() void
+ installServer(String) boolean
+ deleteSavedGame() void
+ initializeNewGame(Company) void
+ buyServer(int, int, int, long) boolean
+ loadState() void
+ hasSavedGame() boolean
+ uninstallServer(VPSOptimization) boolean
  GameTimeManager timeManager
  GameState currentState
  VPSInventory vpsInventory
  RequestManager requestManager
  boolean gameRunning
  RequestGenerator requestGenerator
  List~VPSOptimization~ installedServers
  GameManager instance
  }
  class GameMenuBar {
+ GameMenuBar(GameplayContentPane)
- createStatusButton(String, Color, Color) VBox
- initializeStatusButtons() void
  }
  class GameObject {
+ GameObject(String, int, int)
+ GameObject(String, String, int, int)
+ GameObject()
+ GameObject(String, int)
- Company company
- boolean active
- Map~String, Object~ properties
- String status
- int gridX
- String id
- int gridY
- String name
- String type
- int level
+ stop() void
+ upgrade(GameState) void
+ setGridPosition(int, int) void
+ setProperty(String, Object) void
+ getProperty(String) Object
  String name
  Company company
  int gridX
  String type
  int gridY
  boolean active
  int level
  String status
  Map~String, Object~ properties
  String id
  double x
  double y
  }
  class GameObjectDetailsModal {
+ GameObjectDetailsModal()
+ show(StackPane, GameObject, GameFlowManager) void
  }
  class GameObjectView {
+ GameObjectView(GameObject)
  }
  class GameSaveManager {
+ GameSaveManager()
+ deleteGame() void
+ saveExists() boolean
- createBackupDirectory() void
- createCorruptedFileBackup(File) void
+ saveGame(GameState) void
- createBackup() void
+ loadGame() GameState
  }
  class GameScreen {
+ GameScreen(Navigator, GameConfig, GameState)
- initializeUI() void
  }
  class GameScreen {
+ GameScreen(GameConfig, ScreenManager)
# createContent() Region
# enforceResolution(Region) void
+ show() void
+ hide() void
  }
  class GameState {
+ GameState(Company, List~GameObject~)
+ GameState()
+ GameState(ArrayList~GameObject~)
+ GameState(Company)
- Map~CustomerRequest, List~ChatMessage~~ chatHistory
- List~CustomerRequest~ completedRequests
- int freeVmCount
- List~GameObject~ gameObjects
- Map~String, Object~ vpsInventoryData
- Company company
- long gameTimeMs
- Map~String, Integer~ resources
- ObjectProperty~LocalDateTime~ localDateTime
- Map~String, Boolean~ upgrades
- Map~String, Object~ rackConfiguration
- List~CustomerRequest~ pendingRequests
- long lastSaveTime
- Map~String, String~ vmAssignments
+ removeGameObject(GameObject) void
+ addGameObject(GameObject) void
- readObject(ObjectInputStream) void
- writeObject(ObjectOutputStream) void
+ localDateTimeProperty() ObjectProperty~LocalDateTime~
  Company company
  List~CustomerRequest~ completedRequests
  Map~String, Object~ rackConfiguration
  Map~CustomerRequest, List~ChatMessage~~ chatHistory
  int freeVmCount
  long lastSaveTime
  List~GameObject~ gameObjects
  Map~String, Object~ vpsInventoryData
  LocalDateTime localDateTime
  Map~String, Integer~ resources
  Map~String, String~ vmAssignments
  long gameTimeMs
  List~CustomerRequest~ pendingRequests
  Map~String, Boolean~ upgrades
  }
  class GameTask {
+ GameTask(String, String, String, int, int, int, int)
- int difficultyLevel
# boolean failed
# boolean completed
# StackPane taskContainer
- int timeLimit
- String taskName
- int rewardAmount
- int penaltyRating
- String taskDescription
# applyReward() void
# applyPenalty() void
- cleanupTask() void
# completeTask() void
+ showTask(Runnable) void
# initializeTaskSpecifics() void
# log(String) void
# initializeUI() void
# debugEvent(String, int) void
# startTimer() void
# failTask() void
boolean failed
boolean completed
int difficultyLevel
StackPane taskContainer
boolean taskActive
double penaltyRating
String taskDescription
String taskImage
String taskName
long rewardAmount
int timeLimit
}
class GameTimeController {
+ GameTimeController(Company, RequestManager, Rack, LocalDateTime)
+ addTimeListener(GameTimeListener) void
+ startTime() void
+ stopTime() void
+ removeTimeListener(GameTimeListener) void
  long gameTimeMs
  GameTimeManager gameTimeManager
  LocalDateTime gameDateTime
  }
  class GameTimeListener {
  <<Interface>>
+ onTimeChanged(LocalDateTime, long) void
+ onRentalPeriodCheck(CustomerRequest, RentalPeriodType) void
  }
  class GameTimeManager {
+ GameTimeManager(Company, RequestManager, Rack, LocalDateTime)
- LocalDateTime gameDateTime
+ addVPSServer(VPSOptimization) void
+ removeTimeListener(GameTimeListener) void
- processMonthlyKeepUp() void
+ start() void
- checkRentalExpirations(long) void
- notifyTimeListeners() void
+ removeVPSServer(VPSOptimization) void
+ stop() void
+ addTimeListener(GameTimeListener) void
  long gameTimeMs
  LocalDateTime gameDateTime
  }
  class GameplayContentPane {
+ GameplayContentPane(List~GameObject~, Navigator, ChatSystem, RequestManager, VPSManager, GameFlowManager, DebugOverlayManager, Company, Rack)
- DateView dateView
- Company company
- VPSManager vpsManager
- Navigator navigator
- RequestManager requestManager
- boolean showDebug
- VPSInventory vpsInventory
- StackPane gameArea
- SkillPointsSystem skillPointsSystem
- Rack rack
- InGameMarketMenuBar inGameMarketMenuBar
- ChatSystem chatSystem
- MoneyUI moneyUI
- GameMenuBar menuBar
- GameTimeController gameTimeController
- StackPane rootStack
+ uninstallVPSToInventory(VPSOptimization) boolean
+ showVMSelectionDialog() void
+ openCreateVPSPage() void
+ openSkillPointsWindow() void
+ openRackInfo() void
+ openMarket() void
+ openCreateVMPage(VPSOptimization) void
+ openMusicBox() void
- setupDebugFeatures() void
+ openVPSInventory() void
- createBackgroundLayer() Pane
- setupUI() void
+ pushCenterNotification(String, String, String) void
- syncWithGameManager() void
+ installVPSFromInventory(String) boolean
+ hideMenus() void
+ openSimulationDesktop() void
+ pushMouseNotification(String) void
+ openVPSInfoPage(VPSOptimization) void
+ pushNotification(String, String) void
+ openMusicStop() void
+ openKeroro() void
+ openEditVMPage(VM, VPSOptimization) void
- initializeBasicRack() void
+ pushCenterNotification(String, String) void
+ openVMInfoPage(VM, VPSOptimization) void
+ returnToRoom() void
  Company company
  List~VPSOptimization~ vpsList
  StackPane gameArea
  int occupiedSlots
  int allAvailableSlots
  VPSManager vpsManager
  InGameMarketMenuBar inGameMarketMenuBar
  SkillPointsSystem skillPointsSystem
  int availableSlot
  GameTimeController gameTimeController
  ChatSystem chatSystem
  VPSInventory vpsInventory
  Navigator navigator
  RequestManager requestManager
  MoneyUI moneyUI
  int totalSlots
  boolean showDebug
  Rack rack
  StackPane rootStack
  DateView dateView
  GameMenuBar menuBar
  }
  class GameplayScreen {
+ GameplayScreen(GameConfig, ScreenManager, Navigator, GameState)
+ GameplayScreen(GameConfig, ScreenManager, Navigator)
- loadGame() void
- loadGame(GameState) void
- initializeGameObjects() void
# createContent() Region
}
class InGameMarketMenuBar {
+ InGameMarketMenuBar(GameplayContentPane, VPSManager)
- openMarketWindow() void
  }
  class JavaFXScreenManager {
+ JavaFXScreenManager(GameConfig, Stage)
+ switchScreen(Node) void
+ applySettings(Stage, Scene) void
  boolean fullscreen
  ScreenResolution resolution
  }
  class KeyEventHandler {
+ KeyEventHandler(GameplayContentPane, DebugOverlayManager)
- boolean resumeScreenShowing
- boolean settingsScreenShowing
+ setup() void
+ showSettingsScreen() void
- showResumeScreen() void
- hideSettingsScreen() void
- hideResumeScreen() void
- toggleDebug() void
  boolean settingsScreenShowing
  boolean resumeScreenShowing
  }
  class MainMenuScreen {
+ MainMenuScreen(GameConfig, ScreenManager, Navigator)
- SettingsScreen settingsScreen
# createContent() Region
SettingsScreen settingsScreen
}
class MarketWindow {
+ MarketWindow(Runnable, Runnable, VPSManager, GameplayContentPane)
- createRackProductCard(RackProduct) VBox
- createVPSProductCard(VPSProduct) VBox
- highlightActiveButton(Button, Button[]) void
- updateMoneyDisplay() void
- styleButton(Button) void
- createLeftMenu() VBox
- updateProductDisplay() void
- createRackSection() VBox
- createVPSSection() VBox
  }
  class MenuButton {
+ MenuButton(MenuButtonType)
- render() void
- neon() Effect
  }
  class MenuButtonType {
  <<enumeration>>
- MenuButtonType(String)
- String value
+ valueOf(String) MenuButtonType
+ values() MenuButtonType[]
  String value
  }
  class MessageType {
  <<enumeration>>
+ MessageType()
+ values() MessageType[]
+ valueOf(String) MessageType
  }
  class MessengerController {
+ MessengerController(RequestManager, VPSManager, Company, ChatHistoryManager, StackPane, GameTimeManager, Runnable)
- RequestManager requestManager
- MessengerWindow messengerWindow
- Runnable onClose
- validateVMConsistency() void
- isRequestAssigned(CustomerRequest) boolean
- generateRandomIp() String
- loadSkillLevels() void
- cleanup() void
- createVirtualMachines(int, int) void
- loadVPSFromGameState() void
+ releaseVM(VM, boolean) void
- checkAndUpdateFromRack() boolean
- updateRequestList() void
+ findMatchingCustomerRequest(CustomerRequest) CustomerRequest
- updateChatWithRequestDetails(CustomerRequest) void
- completeVMProvisioning(CustomerRequest, VM) void
- updateDashboard() void
- findAssignedCustomerFromGameState(String) CustomerRequest?
- archiveRequest(CustomerRequest) void
- setupListeners() void
- releaseExpiredVMs() void
+ close() void
  Runnable onClose
  MessengerWindow messengerWindow
  RequestManager requestManager
  }
  class MessengerWindow {
+ MessengerWindow(ChatHistoryManager)
- DashboardView dashboardView
- RequestListView requestListView
- ChatAreaView chatAreaView
- createTitleBar() HBox
  RequestListView requestListView
  Button closeButton
  ChatAreaView chatAreaView
  DashboardView dashboardView
  }
  class MoneyController {
+ MoneyController(MoneyModel, MoneyUI)
- update() void
  }
  class MoneyModel {
+ MoneyModel(long, double)
- LongProperty money
- DoubleProperty rating
+ moneyProperty() LongProperty
+ ratingProperty() DoubleProperty
  double rating
  long money
  }
  class MoneyUI {
+ MoneyUI(GameplayContentPane, MoneyModel)
  }
  class MonitoringSystem {
  <<enumeration>>
- MonitoringSystem(double)
- double score
+ valueOf(String) MonitoringSystem
+ values() MonitoringSystem[]
  double score
  }
  class MouseNotificationController {
+ MouseNotificationController(MouseNotificationModel, MouseNotificationView)
- MouseNotificationView view
+ addNotification(String) void
  MouseNotificationView view
  }
  class MouseNotificationModel {
+ MouseNotificationModel()
- List~Notification~ notifications
+ addNotification(Notification) void
  List~Notification~ notifications
  }
  class MouseNotificationView {
+ MouseNotificationView()
- AudioManager audioManager
- createNotificationPane(String) Pane
+ addNotificationPane(String, double, double) void
  AudioManager audioManager
  }
  class Navigator {
  <<Interface>>
+ continueGame() void
+ showLoadGame() void
+ showMainMenu() void
+ showSettings() void
+ startNewGame() void
+ showInGameSettings() void
  }
  class NetworkNode {
+ NetworkNode(Color, double, double)
- double x
- Color color
- double y
- boolean connected
- Circle circle
  Color color
  boolean connected
  Circle circle
  double x
  double y
  }
  class NetworkRoutingTask {
+ NetworkRoutingTask()
- colorToString(Color) String
- checkTaskCompletion(List~NetworkNode~) void
# initializeTaskSpecifics() void
}
class Notification {
+ Notification(String, String)
- String title
- String content
  String content
  String title
  }
  class Notification {
+ Notification(String, String)
+ Notification(String, String, String)
- String title
- String content
- String image
  String content
  Image image
  String title
  }
  class Notification {
+ Notification(String)
- String content
  String content
  }
  class NotificationController {
+ NotificationController(NotificationModel, NotificationView)
- NotificationView view
+ push(String, String) void
  NotificationView view
  }
  class NotificationModel {
+ NotificationModel()
- List~Notification~ notifications
+ addNotification(Notification) void
  List~Notification~ notifications
  }
  class NotificationView {
+ NotificationView()
- AudioManager audioManager
- createNotificationPane(String, String) Pane
+ addNotificationPane(String, String) void
  AudioManager audioManager
  }
  class OperatingSystem {
  <<enumeration>>
- OperatingSystem(String, double)
- double performanceMultiplier
- String name
+ valueOf(String) OperatingSystem
+ values() OperatingSystem[]
  String name
  double performanceMultiplier
  }
  class PasswordCrackingTask {
+ PasswordCrackingTask()
# initializeTaskSpecifics() void
}
class PerformanceLevel {
<<enumeration>>
- PerformanceLevel(double)
- double score
+ valueOf(String) PerformanceLevel
+ values() PerformanceLevel[]
  double score
  }
  class PlayMenuScreen {
+ PlayMenuScreen(Navigator)
# createContent() Region
- showNewGameConfirmation() void
- startNewGame() void
+ refreshContinueButton() void
- saveGameExists() boolean
- continueGame() void
  }
  class Rack {
+ Rack()
- int currentRackIndex
- int unlockedSlotUnits
- List~Integer~ unlockedSlotUnitsList
- int maxSlotUnits
- int occupiedSlotUnits
- createSlot() VBox
+ loadFromGameState(GameState, VPSInventory) boolean
+ removeVPSFromSlot(int) void
- navigateToNextRack() void
- readObject(ObjectInputStream) void
+ upgrade() boolean
- showCurrentRack() void
+ nextRack() boolean
- writeObject(ObjectOutputStream) void
+ uninstallVPS(VPSOptimization) boolean
- initializeTransientFields() void
- updateVPSDisplay(VBox) void
+ addRack(int) void
- navigateToPreviousRack() void
+ addVPSToSlot(VPSOptimization, int) void
+ prevRack() boolean
+ goToLatestRack() boolean
+ installVPS(VPSOptimization) boolean
+ syncRackWithGameState(GameState) void
- createRackUI(int) VBox
  List~Integer~ uninstalledSlots
  Map~Integer, Map~Integer, Integer~~ allRacksSlotStatus
  int rackIndex
  int availableSlotUnits
  Map~Integer, VPSOptimization~ installedSlotsWithVPS
  List~Integer~ unlockedSlotUnitsList
  List~VPSOptimization~ allInstalledVPS
  int maxRacks
  VBox currentRack
  List~VPSOptimization~ installedVPS
  Map~Integer, Integer~ allSlotStatus
  int occupiedSlotUnits
  int maxSlotUnits
  int currentRackIndex
  int unlockedSlotUnits
  }
  class RackManagementUI {
+ RackManagementUI(GameplayContentPane)
- int MAX_SLOTS
- createEnhancedRackSlot(int, VPSOptimization, boolean, int) Pane
+ openRackInfo() void
- createCyberLabel(String) Label
- animateRackUpgrade() void
- updateUI() void
- calculateUpgradeCost() int
+ syncWithGameState() void
- animateRackTransition(boolean) void
- createStatusLabel(String) Label
- createPixelButton(String, String) Button
- createCyberButton(String) Button
- createEnhancedRackSlots(GridPane, int) void
  int MAX_SLOTS
  String cyberButtonStyle
  }
  class RackObject {
+ RackObject()
+ RackObject(String, String, int, int)
+ RackObject(String, int)
+ RackObject(String, int, int)
+ upgrade(GameState) void
- calculateUpgradeCost() long
+ click() void
  Runnable onClick
  }
  class RackProduct {
  <<enumeration>>
- RackProduct(int, int, int)
- int maintenanceCost
- int slots
- int price
+ values() RackProduct[]
+ valueOf(String) RackProduct
  String keepUpDisplay
  String name
  String description
  int price
  int maintenanceCost
  int slots
  String priceDisplay
  }
  class RackProduct {
  <<enumeration>>
- RackProduct(String, int, int, int)
- int price
- String name
- int slots
- int keepUp
+ valueOf(String) RackProduct
+ values() RackProduct[]
  String keepUpDisplay
  String name
  String description
  int price
  int slots
  int keepUp
  String priceDisplay
  }
  class RackStatus {
+ RackStatus(Rack)
- updateUI() void
- calculateUpgradeCost() int
  }
  class RackUpgradeWindow {
+ RackUpgradeWindow(GameplayContentPane, Runnable)
- setupUI() void
+ updateUI() void
+ increaseMaxSlotUnits(int) void
- styleWindow() void
- upgradeRoom() void
- createUpgradeOption(String, String, String, boolean) BorderPane
- buyNewRack() void
  }
  class RandomEvent {
+ RandomEvent(String, String, EventType, EventEffect)
- String title
- EventType type
- EventEffect effect
- String description
  String description
  String title
  EventEffect effect
  EventType type
  }
  class RandomEventSystem {
+ RandomEventSystem(GameplayContentPane, GameState)
+ start() void
- triggerRandomEvent() void
+ stop() void
- initializeEvents() void
  long initialDelay
  long eventInterval
  }
  class RandomGenerateName {
+ RandomGenerateName()
+ generateRandomName() String
- generateRealisticName(int) String
  }
  class RatingObserver {
  <<Interface>>
+ onRatingChanged(double) void
  }
  class RentalManager {
+ RentalManager(ChatHistoryManager, ChatAreaView, Company, GameTimeManager)
- Runnable onArchiveRequest
- Runnable onUpdateDashboard
- setupTimeListener() void
+ detachFromTimeManager() void
+ setupRentalPeriod(CustomerRequest, VM) void
- getAssignedVM(CustomerRequest) VM?
- handleRentalExpiration(CustomerRequest, RentalPeriodType) void
- calculateRenewalProbability() double
  Runnable onArchiveRequest
  Runnable onUpdateDashboard
  Map~VM, CustomerRequest~ VMAssignment
  RentalPeriodType randomPeriod
  }
  class RentalPeriodType {
  <<enumeration>>
- RentalPeriodType(int, String)
- int days
- String displayName
+ values() RentalPeriodType[]
+ valueOf(String) RentalPeriodType
  String displayName
  int days
  }
  class RequestGenerator {
+ RequestGenerator(RequestManager)
- int maxPendingRequests
+ stopGenerator() void
- updateRequestRateMultiplier() void
+ run() void
  int maxPendingRequests
  }
  class RequestListView {
+ RequestListView()
- ListView~CustomerRequest~ requestView
+ updateRequestList(List~CustomerRequest~) void
  CustomerRequest selectedRequest
  ListView~CustomerRequest~ requestView
  }
  class RequestManager {
+ RequestManager(Company)
- VMProvisioningManager vmProvisioningManager
- List~CustomerRequest~ completedRequests
+ generateRandomRequest() CustomerRequest
+ acceptRequest(CustomerRequest, VPSOptimization, int, int, int) CompletableFuture~VM~
+ completeRequest(CustomerRequest, VPSOptimization) boolean
+ processPayments(long) void
- initializeSampleRequests() void
- getRandomBudget(CustomerType) double
+ realTimeToGameDays(long) int
+ gameDaysToRealTime(int) long
+ addRequest(CustomerRequest) void
+ addCompletedRequests(List~CustomerRequest~) void
  VMProvisioningManager vmProvisioningManager
  CustomerType randomCustomerType
  ObservableList~CustomerRequest~ requests
  Map~CustomerRequest, VM~ activeRequests
  List~CustomerRequest~ completedRequests
  }
  class RequestType {
  <<enumeration>>
+ RequestType()
+ valueOf(String) RequestType
+ values() RequestType[]
  }
  class ResourceManager {
- ResourceManager()
- MouseNotificationView mouseNotificationView
- SkillPointsSystem skillPointsSystem
- CenterNotificationController centerNotificationController
- Rack rack
- NotificationController notificationController
- GameTimeController gameTimeController
- NotificationModel notificationModel
- MouseNotificationModel mouseNotificationModel
- GameState currentState
- NotificationView notificationView
- CenterNotificationView centerNotificationView
- GameEvent gameEvent
- boolean musicRunning
- RequestManager requestManager
- AudioManager audioManager
- ResourceManager instance
- MouseNotificationController mouseNotificationController
- Company company
- CenterNotificationModel centerNotificationModel
+ pushNotification(String, String) void
+ getText(String) String
- createBackupDirectory() void
+ createGameObject(String, String, int, int) GameObject
+ getResource(String) URL
+ loadRackDataFromGameState(GameState) boolean
- createCorruptedFileBackup(File) void
- initiaizeGameTimeController() void
+ deleteSaveFile() void
+ getMusicPath(String) String
- initializeNotifications() void
+ pushCenterNotification(String, String, String) void
+ pushCenterNotification(String, String) void
+ initializeGameEvent(GameplayContentPane) void
+ saveGameState(GameState) void
+ clearCache() void
- initiaizeSkillPointsSystem() void
+ loadImage(String) Image
+ hasSaveFile() boolean
- initiaizeRequestManager() void
+ loadGameState() GameState
+ getTextPath(String) String
+ pushCenterNotificationAutoClose(String, String, String, long) void
+ getResourceAsStream(String) InputStream
+ pushMouseNotification(String) void
+ getImagePath(String) String
+ getSoundPath(String) String
  Company company
  GameState currentState
  boolean musicRunning
  NotificationController notificationController
  CenterNotificationController centerNotificationController
  GameEvent gameEvent
  GameTimeManager gameTimeManager
  MouseNotificationView mouseNotificationView
  NotificationModel notificationModel
  MouseNotificationController mouseNotificationController
  AudioManager audioManager
  SkillPointsSystem skillPointsSystem
  GameTimeController gameTimeController
  RequestManager requestManager
  CenterNotificationModel centerNotificationModel
  Rack rack
  ResourceManager instance
  CenterNotificationView centerNotificationView
  MouseNotificationModel mouseNotificationModel
  NotificationView notificationView
  }
  class ResourceOptimizationTask {
+ ResourceOptimizationTask()
# initializeTaskSpecifics() void
}
class ResumeScreen {
+ ResumeScreen(Navigator, Runnable, Runnable)
+ ResumeScreen(Navigator, Runnable)
- styleButton(MenuButton) void
- setupUI() void
  }
  class RoomObjectsLayer {
+ RoomObjectsLayer(Runnable, Runnable, Runnable, Runnable, Runnable)
- Pane serverLayer
- Pane musicStopLayer
- Pane monitorLayer
- Pane tableLayer
- boolean run
- Pane musicBoxLayer
- Pane keroroLayer
- createMonitorLayer() Pane
- createKeroroLayer() Pane
- createMusicBoxLayer() Pane
- createTableLayer() Pane
- createMusicStopLayer() Pane
- createServerLayer() Pane
  Pane serverLayer
  Pane keroroLayer
  Pane monitorLayer
  Pane tableLayer
  Pane musicBoxLayer
  Pane musicStopLayer
  boolean run
  }
  class SceneController {
- SceneController(Stage, GameConfig, ScreenManager)
- SceneController instance
+ initialize(Stage, GameConfig, ScreenManager) void
+ updateResolution() void
- initializeScene() void
  SceneController instance
  Parent content
  }
  class ScreenManager {
  <<Interface>>
+ switchScreen(Node) void
+ applySettings(Stage, Scene) void
  boolean fullscreen
  ScreenResolution resolution
  }
  class ScreenResolution {
  <<enumeration>>
- ScreenResolution(int, int, String)
- int width
- int height
+ values() ScreenResolution[]
+ valueOf(String) ScreenResolution
+ toString() String
  ScreenResolution maxSupportedResolution
  int width
  int height
  }
  class SecurityLevel {
  <<enumeration>>
- SecurityLevel(double)
- double score
+ valueOf(String) SecurityLevel
+ values() SecurityLevel[]
  double score
  }
  class ServerCoolingTask {
+ ServerCoolingTask()
# initializeTaskSpecifics() void
- checkTaskCompletion() void
  }
  class SettingsChangedEvent {
+ SettingsChangedEvent(GameConfig)
- GameConfig newConfig
  GameConfig newConfig
  }
  class SettingsScreen {
+ SettingsScreen(GameConfig, Navigator, Runnable)
+ SettingsScreen(GameConfig, ScreenManager, Navigator, Runnable)
- createVolumeControls() VBox
- createApplyButton() MenuButton
- createSettingsContainer() VBox
- createSliderControl(String, double, SliderInitializer) HBox
- setupUI() void
- createDisplayControls() VBox
+ show() void
- createBackButton() MenuButton
- styleMenuButton(MenuButton) void
- createLabeledControl(String, Control) HBox
- createTitleLabel(String) Label
- createStyledCheckBox(CheckBox) CheckBox
- createSectionLabel(String) Label
  }
  class SettingsViewModel {
+ SettingsViewModel(GameConfig)
+ musicVolumeProperty() DoubleProperty
- showErrorDialog(String) void
+ fullscreenProperty() BooleanProperty
+ sfxVolumeProperty() DoubleProperty
+ saveSettings() void
+ vsyncProperty() BooleanProperty
+ resolutionProperty() ObjectProperty~ScreenResolution~
- setupBindings() void
  }
  class SimulationDesktopUI {
+ SimulationDesktopUI(GameplayContentPane)
+ openSimulationDesktop() void
  }
  class SkillPointsManager {
+ SkillPointsManager(ChatHistoryManager, ChatAreaView, SkillPointsSystem)
+ awardSkillPoints(CustomerRequest, double) void
  }
  class SkillPointsSystem {
+ SkillPointsSystem(Company)
+ calculateUpgradeCost(int) int
+ canUnlockVPS(VPSProduct) boolean
- readObject(ObjectInputStream) void
- initializeSkillLevels() void
+ upgradeSkill(SkillType) boolean
+ getSkillLevelDescription(SkillType, int) String
+ addPoints(int) void
+ getSkillLevel(SkillType) int
  double managementEfficiency
  int availablePoints
  double marketingBonus
  double securityLevel
  int availableRackSlots
  double serverEfficiencyMultiplier
  double networkSpeedMultiplier
  boolean firewallManagementUnlocked
  }
  class SkillPointsWindow {
+ SkillPointsWindow(SkillPointsSystem, Runnable)
- styleWindow() void
+ updateUI() void
- createSkillCard(SkillType) BorderPane
- setupUI() void
  }
  class SkillType {
  <<enumeration>>
- SkillType(String, String, int)
- int maxLevel
- String description
- String name
+ values() SkillType[]
+ valueOf(String) SkillType
  String name
  String description
  int maxLevel
  }
  class SliderInitializer {
  <<Interface>>
+ initialize(Slider) void
  }
  class TaskManager {
+ TaskManager(StackPane)
- int completedTaskCount
- boolean isRunning
- int failedTaskCount
+ debugTriggerSpecificTask(int) void
- taskLoop() void
+ start() void
- triggerRandomTask() void
- showTask(GameTask) void
+ stop() void
+ debugTriggerTask() void
  String formattedTimeUntilNextTask
  long timeUntilNextTask
  int failedTaskCount
  int completedTaskCount
  boolean isRunning
  }
  class UIUtils {
+ UIUtils()
+ createCard() HBox
+ createModernButton(String, String) Button
+ createSection(String) VBox
  }
  class VM {
+ VM(String, int, int, int)
- String id
- boolean assignedToCustomer
- String status
- long assignedTime
- String name
- String customerId
- String customerName
+ assignToCustomer(String, String, long) void
- generateRandomIp() String
+ releaseFromCustomer() void
  String name
  String ip
  String customerId
  String customerName
  long assignedTime
  String disk
  String status
  boolean assignedToCustomer
  String ram
  String id
  int vcpu
  }
  class VMCreationUI {
+ VMCreationUI(GameplayContentPane)
- createCyberButton(String, String) Button
- createResourceBar(String, String, int) VBox
+ openCreateVMPage(VPSOptimization) void
- createCyberSection(String) VBox
- getNextAvailableIP(VPSOptimization) String
- createCyberCard(String) HBox
- createCyberComboBox(ObservableList~T~) ComboBox~T~
- createCyberTextField(String) TextField
- showValidationError(String, String) void
  }
  class VMEditUI {
+ VMEditUI(GameplayContentPane)
+ openEditVMPage(VM, VPSOptimization) void
- createCyberTextField(String) TextField
- createCyberSection(String) VBox
- createCyberButton(String, String, String) Button
  }
  class VMInfoUI {
+ VMInfoUI(GameplayContentPane)
+ openVMInfoPage(VM, VPSOptimization) void
- showActionMessage(String) void
- getProgressBarStyle(double) String
- generateRandomValue(int, int) double
- createCyberButton(String, String) Button
- generateRandomUptime() String
- addResourceMonitor(GridPane, String, double, int) void
- addInfoField(GridPane, String, String, int) void
  }
  class VMProvisioningManager {
+ VMProvisioningManager(Company)
- Company company
- Map~CustomerRequest, VM~ activeRequests
+ provisionVM(CustomerRequest, VPSOptimization, int, int, int) CompletableFuture~VM~
+ getRequestForVM(VM) CustomerRequest
- generateRandomIp() String
+ processPayments(long) double
+ terminateVM(VM, VPSOptimization) boolean
- calculateRatingChange(CustomerRequest, int, int, int) double
+ getVMForRequest(CustomerRequest) VM
  Company company
  Map~CustomerRequest, VM~ activeRequests
  }
  class VMProvisioningManager {
+ VMProvisioningManager(ChatHistoryManager, ChatAreaView, Map~CustomerRequest, ProgressBar~)
- int deployLevel
- calculateProvisioningDelay() int
- sendVMDetails(CustomerRequest, VM) void
- parseDiskValue(String) int
- generateRandomPassword() String
- findVPSForVM(VM) VPSOptimization?
- sendInitialMessages(CustomerRequest) void
+ startVMProvisioning(CustomerRequest, VM, Runnable) void
- parseRAMValue(String) int
  int deployLevel
  }
  class VMSelectionDialog {
+ VMSelectionDialog(List~VM~, StackPane)
- VM selectedVM
- Runnable onConfirm
- createDigitalLine() Line
- closeDialog() void
  Runnable onConfirm
  VM selectedVM
  }
  class VPSCreationUI {
+ VPSCreationUI(GameplayContentPane)
+ openCreateVPSPage() void
  }
  class VPSInfoUI {
+ VPSInfoUI(GameplayContentPane)
+ openVPSInfoPage(VPSOptimization) void
- uninstallVPS(VPSOptimization) void
- createHardwareInfoRow(String, int, int) HBox
- createStatusLED(String, boolean) Circle
- calculateUtilization(VPSOptimization, int, int, int) double
- createPhysicalServerView(VPSOptimization, int, int, int) HBox
- createVMEntry(VM, VPSOptimization) HBox
  }
  class VPSInventory {
+ VPSInventory()
+ addVPS(String, VPSOptimization) void
+ removeVPS(String) VPSOptimization
+ getVPS(String) VPSOptimization
+ clear() void
  List~VPSOptimization~ allVPS
  int size
  boolean empty
  List~String~ allVPSIds
  Map~String, VPSOptimization~ inventoryMap
  }
  class VPSInventoryUI {
+ VPSInventoryUI(GameplayContentPane)
- installVPS(String, VPSOptimization) void
- createInventoryItemRow(String, VPSOptimization, boolean) HBox
+ openInventory() void
- showVPSDetails(String, VPSOptimization) void
- createInventoryList() VBox
  }
  class VPSManager {
+ VPSManager()
+ createVPS(String) void
+ getVPS(String) VPSOptimization
+ addVPS(String, VPSOptimization) void
  List~VPSOptimization~ VPSList
  Map~String, VPSOptimization~ VPSMap
  }
  class VPSOptimization {
+ VPSOptimization()
+ VPSOptimization(String, int, int, VPSSize)
+ VPSOptimization(int, int, VPSSize)
- int diskInGB
- List~VM~ vms
- double cpuUsage
- String vpsId
- VPSSize size
- double ramUsage
- VPSStatus status
- double diskUsage
- int ramInGB
- int optimizationLevel
- double networkUsage
- int maxVMs
- boolean installed
+ hasAutoScaling() boolean
+ calculateOptimizationScore() double
+ hasBackupSystem() boolean
- updateResourceUsage() void
+ equals(Object) boolean
+ optimize() boolean
+ hasMonitoringSystem() boolean
+ removeVM(VM) void
+ addVM(VM) void
+ hashCode() int
  double cpuUsage
  int optimizationLevel
  VPSSize size
  double diskUsage
  double networkUsage
  String vpsId
  VPSStatus VPSStatus
  int VCPUs
  int slotsRequired
  int maxVMs
  int ramInGB
  int diskInGB
  boolean installed
  double ramUsage
  boolean monitoringSystem
  boolean backupSystem
  List~VM~ vms
  String status
  boolean autoScaling
  }
  class VPSProduct {
  <<enumeration>>
- VPSProduct(String, int, int, int, int, int, VPSSize)
- int ram
- int cpu
- String name
- int storage
- VPSSize size
- int keepUp
- int price
+ isUnlocked(int) boolean
+ valueOf(String) VPSProduct
+ values() VPSProduct[]
  String keepUpDisplay
  String name
  String description
  int price
  VPSSize size
  int cpu
  int keepUp
  int ram
  int storage
  String priceDisplay
  }
  class VPSSize {
  <<enumeration>>
- VPSSize(String, int, double)
- VPSSize(String, int)
- int slotsRequired
- double performanceMultiplier
- String displayName
+ valueOf(String) VPSSize
+ values() VPSSize[]
  String displayName
  int slotsRequired
  double performanceMultiplier
  }
  class VPSStatus {
  <<enumeration>>
- VPSStatus(String)
- String displayName
+ valueOf(String) VPSStatus
+ values() VPSStatus[]
+ toString() String
  String displayName
  }
  class WireTask {
+ WireTask()
+ WireTask(int)
# initializeTaskSpecifics() void
- shuffleArray(String[]) void
- updateConnection(MouseEvent, int, Line[]) void
- getRandomColors(int) String[]
- finishConnection(MouseEvent, int, Circle[], Line[], String[]) void
- startConnection(int, Circle[], Line[]) void
  }
  class ZoomPanHandler {
+ ZoomPanHandler(Group, StackPane, DebugOverlayManager, boolean)
- boolean showDebug
- setupZoomHandlers() void
- updateDebugInfoIfNeeded() void
- setupPanHandlers() void
+ setup() void
- setupResetHandler() void
  boolean showDebug
  }

FirewallDefenseTask  -->  AttackNode
CalibrationTask  -->  GameTask
VMSelectionDialog  -->  Circle
CustomerRequest  -->  Customer
DataDecryptionTask  -->  GameTask
DataSortingTask  -->  DataPacket
DataSortingTask  -->  GameTask
DateController  ..>  GameTimeListener
DebugOverlayManager  -->  GameState
DefaultGameConfig  ..>  GameConfig
RandomEventSystem  -->  EventEffect
RandomEventSystem  -->  EventType
FileRecoveryTask  -->  GameTask
FirewallDefenseTask  -->  GameTask
GameApplication  ..>  Navigator
GameTimeManager  -->  GameTimeListener
GameplayScreen  -->  GameScreen
JavaFXScreenManager  ..>  ScreenManager
MainMenuScreen  -->  GameScreen
NetworkRoutingTask  -->  NetworkNode
NetworkRoutingTask  -->  GameTask
CenterNotificationModel  -->  Notification
MouseNotificationModel  -->  Notification
NotificationModel  -->  Notification
PasswordCrackingTask  -->  GameTask
RackObject  -->  GameObject
RandomEventSystem  -->  RandomEvent
Company  -->  RatingObserver
CustomerRequest  -->  RentalPeriodType
ResourceOptimizationTask  -->  GameTask
ServerCoolingTask  -->  GameTask
SkillPointsSystem  -->  SkillType
SettingsScreen  -->  SliderInitializer
VPSOptimization  -->  VM
VPSOptimization  -->  GameObject
WireTask  -->  GameTask 
```