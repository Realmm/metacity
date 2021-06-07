package org.metacity.metacity.cmd.enjin.arg;

import org.bukkit.command.CommandSender;
import org.metacity.metacity.wallet.TokenWalletViewState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WalletViewStateArgumentProcessor extends AbstractArgumentProcessor<TokenWalletViewState> {

    public static final WalletViewStateArgumentProcessor INSTANCE = new WalletViewStateArgumentProcessor();

    @Override
    public List<String> tab(CommandSender sender, String arg) {
        String lowerCaseArg = arg.toLowerCase();

        List<String> results = new ArrayList<>(TokenWalletViewState.values().length);
        for (TokenWalletViewState viewState : TokenWalletViewState.values()) {
            String lowerCaseState = viewState.toString().toLowerCase();
            if (lowerCaseState.startsWith(lowerCaseArg))
                results.add(lowerCaseState);
        }

        return results;
    }

    @Override
    public Optional<TokenWalletViewState> parse(CommandSender sender, String arg) {
        try {
            return Optional.of(TokenWalletViewState.valueOf(arg.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

}
