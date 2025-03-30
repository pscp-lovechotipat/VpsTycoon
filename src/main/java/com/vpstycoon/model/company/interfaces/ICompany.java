package com.vpstycoon.model.company.interfaces;

/**
 * อินเตอร์เฟซสำหรับบริษัทในเกม
 */
public interface ICompany {
    
    /**
     * อินเตอร์เฟซสำหรับติดตามการเปลี่ยนแปลงคะแนนบริษัท
     */
    interface RatingObserver {
        /**
         * เรียกเมื่อคะแนนมีการเปลี่ยนแปลง
         */
        void onRatingChanged(double newRating);
    }
    
    /**
     * อินเตอร์เฟซสำหรับติดตามธุรกรรมเงิน
     */
    interface MoneyTransactionObserver {
        /**
         * เรียกเมื่อมีการทำธุรกรรมเงิน
         */
        void onMoneyTransaction(long amount, long newBalance, boolean isIncome);
    }
    
    /**
     * รับชื่อบริษัท
     */
    String getName();
    
    /**
     * ตั้งชื่อบริษัท
     */
    void setName(String name);
    
    /**
     * รับคะแนนบริษัท
     */
    double getRating();
    
    /**
     * ตั้งคะแนนบริษัท
     */
    void setRating(double rating);
    
    /**
     * รับคะแนนการตลาด
     */
    int getMarketingPoints();
    
    /**
     * ตั้งคะแนนการตลาด
     */
    void setMarketingPoints(int marketingPoints);
    
    /**
     * รับจำนวนเงินปัจจุบัน
     */
    long getMoney();
    
    /**
     * ตั้งจำนวนเงินปัจจุบัน
     */
    void setMoney(long money);
    
    /**
     * เพิ่มเงิน (รายได้)
     */
    void addMoney(long amount);
    
    /**
     * ลดเงิน (ค่าใช้จ่าย)
     */
    boolean spendMoney(long amount);
    
    /**
     * รับค่าความพึงพอใจของลูกค้า
     */
    int getCustomerSatisfaction();
    
    /**
     * ตั้งค่าความพึงพอใจของลูกค้า
     */
    void setCustomerSatisfaction(int customerSatisfaction);
    
    /**
     * รับจำนวนคำขอที่สำเร็จ
     */
    int getCompletedRequests();
    
    /**
     * รับจำนวนคำขอที่ล้มเหลว
     */
    int getFailedRequests();
    
    /**
     * บันทึกคำขอที่สำเร็จ
     */
    void recordCompletedRequest(int satisfactionChange);
    
    /**
     * บันทึกคำขอที่ล้มเหลว
     */
    void recordFailedRequest();
    
    /**
     * รับคะแนนดาว (1-5)
     */
    int getStarRating();
    
    /**
     * รับจำนวน VM ที่ใช้ได้
     */
    int getAvailableVMs();
    
    /**
     * ตั้งจำนวน VM ที่ใช้ได้
     */
    void setAvailableVMs(int availableVMs);
    
    /**
     * รับจำนวนคะแนนทักษะที่ใช้ได้
     */
    int getSkillPointsAvailable();
    
    /**
     * ตั้งจำนวนคะแนนทักษะที่ใช้ได้
     */
    void setSkillPointsAvailable(int skillPointsAvailable);
    
    /**
     * เพิ่มคะแนนทักษะ
     */
    void addSkillPoints(int points);
    
    /**
     * ลดคะแนนบริษัท
     */
    void reduceRating(double amount);
    
    /**
     * เพิ่มผู้ติดตามการเปลี่ยนแปลงคะแนน
     */
    void addRatingObserver(RatingObserver observer);
    
    /**
     * ลบผู้ติดตามการเปลี่ยนแปลงคะแนน
     */
    void removeRatingObserver(RatingObserver observer);
    
    /**
     * เพิ่มผู้ติดตามธุรกรรมเงิน
     */
    void addMoneyObserver(MoneyTransactionObserver observer);
    
    /**
     * ลบผู้ติดตามธุรกรรมเงิน
     */
    void removeMoneyObserver(MoneyTransactionObserver observer);
} 