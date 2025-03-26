package com.vpstycoon.ui.game.desktop;

import com.vpstycoon.game.manager.VMProvisioningManager;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.ui.game.GameplayContentPane;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;

public class SimulationDesktopUI {
    private final GameplayContentPane parent;

    public SimulationDesktopUI(GameplayContentPane parent) {
        this.parent = parent;
    }

    public synchronized void openSimulationDesktop() {
        // ก่อนสร้าง Desktop Screen อัพเดต Free VM count ใน Company
        int availableVMs = 0;
        
        // ดึง VMProvisioningManager จาก RequestManager เพื่อใช้ข้อมูลการกำหนด VM
        VMProvisioningManager vmProvisioningManager = parent.getRequestManager().getVmProvisioningManager();
        
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

        parent.getGameArea().getChildren().clear();
        parent.getGameArea().getChildren().add(desktop);
        parent.getRootStack().getChildren().remove(1);
        
        // Hide the menu bars
        parent.getMenuBar().setVisible(false);
        parent.getInGameMarketMenuBar().setVisible(false);

        desktop.addExitButton(parent::returnToRoom);
    }
}