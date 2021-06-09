package org.metacity.metacity.cmd.enjin.arg;

import org.bukkit.command.CommandSender;
import org.metacity.metacity.EnjinCraft;
import org.metacity.metacity.token.TokenManager;
import org.metacity.metacity.token.TokenModel;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TokenDefinitionArgumentProcessor extends AbstractArgumentProcessor<TokenModel> {

    public static final TokenDefinitionArgumentProcessor INSTANCE = new TokenDefinitionArgumentProcessor();

    @Override
    public List<String> tab(CommandSender sender, String arg) {
        String lowerCaseArg = arg.toLowerCase();
        TokenManager tokenManager = EnjinCraft.bootstrap().get().getTokenManager();
        return tokenManager.getTokenIds().stream()
                .filter(id -> id.toLowerCase().startsWith(lowerCaseArg))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TokenModel> parse(CommandSender sender, String arg) {
        TokenManager tokenManager = EnjinCraft.bootstrap().get().getTokenManager();

        return Optional.ofNullable(tokenManager.getToken(arg));
    }

}
