package org.metacity.metacity.cmd.enj.token;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.metacity.commands.SubCommand;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.token.TokenManager;
import org.metacity.metacity.token.TokenModel;
import org.metacity.metacity.util.TokenUtils;
import org.metacity.metacity.util.server.Translation;
import org.metacity.util.CC;
import org.metacity.util.Util;

import java.util.Set;
import java.util.stream.Collectors;

public class ListCmd extends SubCommand<Player> {

    public ListCmd() {
        super(Player.class);
        addPermission(Permission.CMD_TOKEN_CREATE.node());
        addCondition((p, w) -> w.validateNode(1, s -> s.equalsIgnoreCase("list")));
        setExecution((p, w) -> {
            String id = w.hasNode(2) ? w.node(2) : null;
            if (id == null) listBaseTokens(p);
            else listNonfungibleInstances(p, id);
        });
    }

    private void listBaseTokens(CommandSender sender) {
        Set<TokenModel> tokens = MetaCity.getInstance().getTokenManager().getTokens();
        if (tokens.isEmpty()) {
            Translation.COMMAND_TOKEN_LIST_EMPTY.send(sender);
            return;
        }
        sender.sendMessage(CC.GREEN + Translation.COMMAND_TOKEN_LIST_HEADER_TOKENS.translation());
        int count = 0;
        for (TokenModel token : tokens) {
            if (token.isNonFungibleInstance())
                continue;
            sender.sendMessage(Util.color(String.format("&a%d: &6%s &7(&6%s&7)", count++, token.getId(), token.getAlternateId())));
        }
    }

    private void listNonfungibleInstances(CommandSender sender, String id) {
        TokenManager tokenManager = MetaCity.getInstance().getTokenManager();
        TokenModel baseModel = tokenManager.getToken(id);
        if (baseModel == null) {
            Translation.COMMAND_TOKEN_NOSUCHTOKEN.send(sender);
            return;
        }

        Set<String> instances = tokenManager.getFullIds()
                .stream()
                .filter(fullId -> {
                    String tokenId = TokenUtils.getTokenID(fullId);
                    String tokenIndex = TokenUtils.getTokenIndex(fullId);
                    return tokenId.equals(baseModel.getId()) && !tokenIndex.equals(TokenUtils.BASE_INDEX);
                })
                .collect(Collectors.toSet());
        if (instances.isEmpty()) {
            Translation.COMMAND_TOKEN_LIST_EMPTY.send(sender);
            return;
        }

        sender.sendMessage(CC.GREEN + Translation.COMMAND_TOKEN_LIST_HEADER_NONFUNGIBLE.translation());
        int count = 0;
        for (String fullId : instances) {
            TokenModel instance = tokenManager.getToken(fullId);
            sender.sendMessage(Util.color(String.format("&a%d: &6%s #%d",
                    count++,
                    instance.getId(),
                    TokenUtils.convertIndexToLong(instance.getIndex()))));
        }
    }

}
