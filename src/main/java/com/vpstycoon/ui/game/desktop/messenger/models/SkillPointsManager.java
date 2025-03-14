package com.vpstycoon.ui.game.desktop.messenger.models;

import com.vpstycoon.game.customer.enums.CustomerType;
import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.ui.game.desktop.messenger.views.ChatAreaView;
import com.vpstycoon.ui.game.desktop.messenger.MessageType;

import java.util.HashMap;

public class SkillPointsManager {
    private final ChatHistoryManager chatHistoryManager;
    private final ChatAreaView chatAreaView;

    public SkillPointsManager(ChatHistoryManager chatHistoryManager, ChatAreaView chatAreaView) {
        this.chatHistoryManager = chatHistoryManager;
        this.chatAreaView = chatAreaView;
    }

    public void awardSkillPoints(CustomerRequest request, double ratingImpact) {
        int basePoints = request.getCustomerType() == CustomerType.ENTERPRISE ? 15 : 5;
        int points = ratingImpact > 0 ? basePoints + 5 : basePoints;

        try {
            java.lang.reflect.Field skillPointsField = com.vpstycoon.ui.game.status.CircleStatusButton.class
                    .getDeclaredField("skillPointsMap");
            skillPointsField.setAccessible(true);
            HashMap<String, Integer> skillPointsMap = (HashMap<String, Integer>) skillPointsField.get(null);

            skillPointsMap.put("Deploy", skillPointsMap.getOrDefault("Deploy", 0) + points);
            chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM,
                    "Earned " + points + " Deploy points! Total: " + skillPointsMap.get("Deploy")));
            chatAreaView.addSystemMessage("Earned " + points + " Deploy points! Total: " + skillPointsMap.get("Deploy"));
        } catch (Exception e) {
            System.err.println("Error updating skill points: " + e.getMessage());
        }
    }
}