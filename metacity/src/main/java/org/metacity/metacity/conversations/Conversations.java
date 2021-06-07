package org.metacity.metacity.conversations;

import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.Prompt;
import org.bukkit.plugin.Plugin;
import org.metacity.metacity.conversations.factories.TokenCreationConversationFactory;
import org.metacity.metacity.conversations.prompts.TokenIndexPrompt;
import org.metacity.metacity.conversations.prompts.TokenNicknamePrompt;

public class Conversations {

    private final TokenCreationConversationFactory tokenCreationConversationFactory;

    public Conversations(Plugin plugin, boolean nft, boolean baseExists) {
        this.tokenCreationConversationFactory = new TokenCreationConversationFactory(plugin, getStartPrompt(nft, baseExists));
    }

    public Prompt getStartPrompt(boolean nft, boolean baseExists) {
        return nft ? baseExists ? new TokenIndexPrompt() : new TokenNicknamePrompt() : new TokenNicknamePrompt();
    }

    public Conversation startTokenCreationConversation(Conversable withWhom) {
        return tokenCreationConversationFactory.buildConversation(withWhom);
    }

}
