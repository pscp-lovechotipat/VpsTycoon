package com.vpstycoon.view.interfaces;

/**
 * Interface พื้นฐานสำหรับ view ทั้งหมดในระบบ
 */
public interface IView {
    
    /**
     * เริ่มต้นการทำงานของ view
     */
    default void initialize() {
        // ค่าเริ่มต้นไม่ทำอะไร สามารถ override ได้
    }
    
    /**
     * ทำความสะอาดทรัพยากรที่ใช้โดย view
     */
    default void cleanup() {
        // ค่าเริ่มต้นไม่ทำอะไร สามารถ override ได้
    }
} 