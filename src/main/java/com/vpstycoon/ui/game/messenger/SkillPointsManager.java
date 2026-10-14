package com.vpstycoon.ui.game.messenger;

import com.vpstycoon.model.chat.ChatMessage;
import com.vpstycoon.model.chat.MessageType;
import com.vpstycoon.model.company.SkillPointsSystem;
import com.vpstycoon.model.customer.CustomerRequest;
import com.vpstycoon.model.customer.CustomerType;
import com.vpstycoon.persistence.ChatHistoryManager;

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
