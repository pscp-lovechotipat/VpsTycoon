package com.vpstycoon.ui.game.desktop.messenger.models;

import com.vpstycoon.game.company.SkillPointsSystem;
import com.vpstycoon.game.customer.enums.CustomerType;
import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.ui.game.desktop.messenger.MessageType;
import com.vpstycoon.ui.game.desktop.messenger.views.ChatAreaView;

import java.util.HashMap;

public class SkillPointsManager {
    private final ChatHistoryManager chatHistoryManager;
    private final ChatAreaView chatAreaView;
    private final SkillPointsSystem skillPointsSystem;

    public SkillPointsManager(ChatHistoryManager chatHistoryManager, ChatAreaView chatAreaView, SkillPointsSystem skillPointsSystem) {
        this.chatHistoryManager = chatHistoryManager;
        this.chatAreaView = chatAreaView;
        this.skillPointsSystem = skillPointsSystem;
    }

    public void awardSkillPoints(CustomerRequest request, double ratingImpact) {
        int basePoints = request.getCustomerType() == CustomerType.ENTERPRISE ? 15 : 5;
        int points = ratingImpact > 0 ? basePoints + 5 : basePoints;

        skillPointsSystem.addPoints(points); 
        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM,
                "Earned " + points + " skill points! Total: " + skillPointsSystem.getAvailablePoints(), new HashMap<>()));
        chatAreaView.addSystemMessage("Earned " + points + " skill points! Total: " + skillPointsSystem.getAvailablePoints());
    }
}
