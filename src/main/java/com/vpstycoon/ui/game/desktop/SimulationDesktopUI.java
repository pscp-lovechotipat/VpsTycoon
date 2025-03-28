package com.vpstycoon.ui.game.desktop;

import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.desktop.messenger.models.VMProvisioningManagerImpl;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import java.util.ArrayList;
import java.util.List;

public class SimulationDesktopUI {
    private final GameplayContentPane parent;

    public SimulationDesktopUI(GameplayContentPane parent) {
        this.parent = parent;
    }

    public synchronized void openSimulationDesktop() {
        // ก่อนสร้าง Desktop Screen อัพเดต Free VM count ใน Company
        int availableVMs = 0;
        
        // ดึง VMProvisioningManager จาก RequestManager เพื่อใช้ข้อมูลการกำหนด VM
        VMProvisioningManagerImpl vmProvisioningManager = parent.getRequestManager().getVmProvisioningManager();
        
        // ตรวจสอบ VM ทั้งหมดจากทุก VPS
        for (var vps : parent.getVpsManager().getVPSMap().values()) {
            // นับจำนวน VM ที่มีสถานะ Running และไม่ได้อยู่ใน activeRequests
            for (var vm : vps.getVms()) {
                if ("Running".equals(vm.getStatus()) && 
                     vmProvisioningManager.getRequestForVM(vm) == null) {
                    availableVMs++;
                }
            }
        }
        
        // บันทึกค่าลงใน Company เพื่อให้สามารถดึงกลับมาใช้ได้
        parent.getCompany().setAvailableVMs(availableVMs);

        // บันทึกสถานะปัจจุบันของ UI elements
        final boolean menuBarVisible = parent.getMenuBar().isVisible();
        final boolean moneyUIVisible = parent.getMoneyUI().isVisible();
        final boolean dateViewVisible = parent.getDateView().isVisible();
        final boolean marketMenuBarVisible = parent.getInGameMarketMenuBar().isVisible();
        
        // บันทึกจำนวน children ปัจจุบันใน rootStack
        final int rootStackChildCount = parent.getRootStack().getChildren().size();

        DesktopScreen desktop = new DesktopScreen(
                parent.getCompany().getRating(),
                parent.getCompany().getMarketingPoints(),
                parent.getChatSystem(),
                parent.getRequestManager(),
                parent.getVpsManager(),
                parent.getCompany(),
                parent,
                ResourceManager.getInstance().getGameTimeManager()
        );
        StackPane.setAlignment(desktop, Pos.CENTER);
        
        // Make desktop fill more of the available space (95% of parent container)
        desktop.setMaxSize(parent.getGameArea().getWidth() * 0.95, parent.getGameArea().getHeight() * 0.95);
        desktop.setPrefSize(parent.getGameArea().getWidth() * 0.95, parent.getGameArea().getHeight() * 0.95);

        // เก็บ reference ของเนื้อหาเดิมใน gameArea
        final List<Node> originalContent = new ArrayList<>(parent.getGameArea().getChildren());
        
        // ล้าง gameArea แล้วเพิ่ม desktop
        parent.getGameArea().getChildren().clear();
        parent.getGameArea().getChildren().add(desktop);

        // ซ่อน UI elements
        parent.getMenuBar().setVisible(false);
        parent.getMoneyUI().setVisible(false);
        parent.getDateView().setVisible(false);
        parent.getInGameMarketMenuBar().setVisible(false);

        // เพิ่มปุ่ม Exit ที่คืนค่า UI กลับสู่สถานะเดิม
        desktop.addExitButton(() -> {
            // ใช้ Platform.runLater เพื่อให้แน่ใจว่าการอัพเดต UI จะเกิดบน JavaFX thread
            javafx.application.Platform.runLater(() -> {
                try {
                    // ก่อนเรียก returnToRoom ให้ล้าง gameArea และเตรียมคืนค่าเดิม
                    parent.getGameArea().getChildren().clear();
                    
                    // คืน content เดิมกลับเข้า gameArea
                    for (Node node : originalContent) {
                        if (!parent.getGameArea().getChildren().contains(node)) {
                            parent.getGameArea().getChildren().add(node);
                        }
                    }
                    
                    // คืนสถานะการแสดงผลของ UI elements
                    parent.getMenuBar().setVisible(menuBarVisible);
                    parent.getMoneyUI().setVisible(moneyUIVisible);
                    parent.getDateView().setVisible(dateViewVisible);
                    parent.getInGameMarketMenuBar().setVisible(marketMenuBarVisible);
                    
                    // ใส่ fadeIn effect เพื่อให้ UI ค่อยๆ ปรากฏ
                    javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
                        javafx.util.Duration.millis(300), parent.getGameArea());
                    fadeIn.setFromValue(0.8);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                    
                    // แสดงข้อความยืนยันการกลับสู่หน้าหลัก
                    System.out.println("กลับสู่หน้าหลักเรียบร้อย UI ถูกคืนค่าสู่สถานะเดิม");
                    
                } catch (Exception ex) {
                    // ถ้าเกิดข้อผิดพลาด ให้ใช้วิธีเดิมคือ returnToRoom
                    System.err.println("เกิดข้อผิดพลาดในการคืนค่า UI: " + ex.getMessage());
                    ex.printStackTrace();
                    parent.returnToRoom();
                }
            });
        });
    }
}