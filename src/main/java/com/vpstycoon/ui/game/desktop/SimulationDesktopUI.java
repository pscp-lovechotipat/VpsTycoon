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
        
        int availableVMs = 0;
        
        if (parent.getRequestManager() == null) {
            System.err.println("ไม่สามารถเปิดหน้าเดสก์ท็อปได้: RequestManager ยังคงเป็น null หลังจากพยายามสร้างใหม่");
            parent.pushCenterNotification("ข้อผิดพลาด", "ไม่สามารถเปิดหน้าเดสก์ท็อปได้: RequestManager ไม่สามารถสร้างได้", "/images/icon/error.png");
            return;
        }
        
        VMProvisioningManagerImpl vmProvisioningManager = parent.getRequestManager().getVmProvisioningManager();
        
        if (vmProvisioningManager == null) {
            System.err.println("ไม่สามารถเปิดหน้าเดสก์ท็อปได้: VMProvisioningManager เป็น null");
            parent.pushCenterNotification("ข้อผิดพลาด", "ไม่สามารถเปิดหน้าเดสก์ท็อปได้: ระบบจัดการ VM ไม่พร้อมใช้งาน", "/images/icon/error.png");
            return;
        }
        
        if (parent.getVpsManager() == null || parent.getVpsManager().getVPSMap() == null) {
            System.err.println("ไม่สามารถเปิดหน้าเดสก์ท็อปได้: VPSManager หรือ VPSMap เป็น null");
            parent.pushCenterNotification("ข้อผิดพลาด", "ไม่สามารถเปิดหน้าเดสก์ท็อปได้: ระบบจัดการ VPS ไม่พร้อมใช้งาน", "/images/icon/error.png");
            return;
        }
        
        for (var vps : parent.getVpsManager().getVPSMap().values()) {
            
            for (var vm : vps.getVms()) {
                if ("Running".equals(vm.getStatus()) && 
                     vmProvisioningManager.getRequestForVM(vm) == null) {
                    availableVMs++;
                }
            }
        }
        
        if (parent.getCompany() == null) {
            System.err.println("ไม่สามารถเปิดหน้าเดสก์ท็อปได้: Company เป็น null");
            parent.pushCenterNotification("ข้อผิดพลาด", "ไม่สามารถเปิดหน้าเดสก์ท็อปได้: ข้อมูลบริษัทไม่พร้อมใช้งาน", "/images/icon/error.png");
            return;
        }
        
        parent.getCompany().setAvailableVMs(availableVMs);

        
        final boolean menuBarVisible = parent.getMenuBar().isVisible();
        final boolean moneyUIVisible = parent.getMoneyUI().isVisible();
        final boolean dateViewVisible = parent.getDateView().isVisible();
        final boolean marketMenuBarVisible = parent.getInGameMarketMenuBar().isVisible();
        
        
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
        
        
        desktop.setMaxSize(parent.getGameArea().getWidth() * 0.95, parent.getGameArea().getHeight() * 0.95);
        desktop.setPrefSize(parent.getGameArea().getWidth() * 0.95, parent.getGameArea().getHeight() * 0.95);

        
        final List<Node> originalContent = new ArrayList<>(parent.getGameArea().getChildren());
        
        
        parent.getGameArea().getChildren().clear();
        parent.getGameArea().getChildren().add(desktop);

        
        parent.getMenuBar().setVisible(false);
        parent.getMoneyUI().setVisible(false);
        parent.getDateView().setVisible(false);
        parent.getInGameMarketMenuBar().setVisible(false);

        
        desktop.addExitButton(() -> {
            
            javafx.application.Platform.runLater(() -> {
                try {
                    
                    parent.getGameArea().getChildren().clear();
                    
                    
                    for (Node node : originalContent) {
                        if (!parent.getGameArea().getChildren().contains(node)) {
                            parent.getGameArea().getChildren().add(node);
                        }
                    }
                    
                    
                    parent.getMenuBar().setVisible(menuBarVisible);
                    parent.getMoneyUI().setVisible(moneyUIVisible);
                    parent.getDateView().setVisible(dateViewVisible);
                    parent.getInGameMarketMenuBar().setVisible(marketMenuBarVisible);
                    
                    
                    javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
                        javafx.util.Duration.millis(300), parent.getGameArea());
                    fadeIn.setFromValue(0.8);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                    
                    
                    System.out.println("กลับสู่หน้าหลักเรียบร้อย UI ถูกคืนค่าสู่สถานะเดิม");
                    
                } catch (Exception ex) {
                    
                    System.err.println("เกิดข้อผิดพลาดในการคืนค่า UI: " + ex.getMessage());
                    ex.printStackTrace();
                    parent.returnToRoom();
                }
            });
        });
    }
}
