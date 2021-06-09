package org.metacity.metacity.cmd.enjin.token;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.metacity.metacity.cmd.enjin.CommandContext;
import org.metacity.metacity.cmd.enjin.CommandRequirements;
import org.metacity.metacity.cmd.enjin.MetaCommand;
import org.metacity.metacity.cmd.enjin.SenderType;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.token.TokenManager;
import org.metacity.metacity.token.TokenModel;
import org.metacity.metacity.util.TokenUtils;
import org.metacity.metacity.util.server.Translation;

import java.util.Set;
import java.util.stream.Collectors;

public class CmdList extends MetaCommand {

    public CmdList(MetaCommand parent) {
        super(parent);
        this.aliases.add("list");
        this.optionalArgs.add("id");
        this.requirements = CommandRequirements.builder()
                .withPermission(Permission.CMD_TOKEN_CREATE)
                .withAllowedSenderTypes(SenderType.PLAYER)
                .build();
    }

    @Override
    public void execute(CommandContext context) {
        String id = context.args().size() > 0
                ? context.args().get(0)
                : null;
        if (id == null)
            listBaseTokens(context.sender());
        else
            listNonfungibleInstances(context.sender(), id);
    }

    private void listBaseTokens(CommandSender sender) {
        Set<TokenModel> tokens = bootstrap.getTokenManager().getTokens();
        if (tokens.isEmpty()) {
            Translation.COMMAND_TOKEN_LIST_EMPTY.send(sender);
            return;
        }
        sender.sendMessage(ChatColor.GREEN + Translation.COMMAND_TOKEN_LIST_HEADER_TOKENS.translation());
        int count = 0;
        for (TokenModel token : tokens) {
            if (token.isNonFungibleInstance())
                continue;
            sender.sendMessage(String.format("&a%d: &6%s &7(&6%s&7)", count++, token.getId(), token.getAlternateId()));
//            MessageUtils.sendString(sender, String.format("&a%d: &6%s &7(&6%s&7)",
//                    count++, token.getId(), token.getAlternateId()));
        }
    }

    private void listNonfungibleInstances(CommandSender sender, String id) {
        TokenManager tokenManager = bootstrap.getTokenManager();
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

        sender.sendMessage(ChatColor.GREEN + Translation.COMMAND_TOKEN_LIST_HEADER_NONFUNGIBLE.translation());
        int count = 0;
        for (String fullId : instances) {
            TokenModel instance = tokenManager.getToken(fullId);
            sender.sendMessage(String.format("&a%d: &6%s #%d",
                    count++,
                    instance.getId(),
                    TokenUtils.convertIndexToLong(instance.getIndex())));
        }
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.COMMAND_TOKEN_LIST_DESCRIPTION;
    }

}
