package org.metacity.metacity.cmd.enj.token;

import org.bukkit.entity.Player;
import org.metacity.commands.SubCommand;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.token.TokenManager;
import org.metacity.metacity.util.server.Translation;
import org.metacity.metacity.wallet.TokenWalletViewState;
import org.metacity.util.Logger;

public class SetWalletViewCmd extends SubCommand<Player> {

    public SetWalletViewCmd() {
        super(Player.class);
        addPermission(Permission.CMD_TOKEN_CREATE.node());
        addCondition((p, w) -> w.validateNode(1, s -> s.equalsIgnoreCase("setwalletview")));
        addCondition((p, w) -> w.hasNode(3));
        setExecution((p, w) -> {
            String id = w.node(2);
            String view = w.node(3);

            TokenWalletViewState viewState;
            try {
                viewState = TokenWalletViewState.valueOf(view.toUpperCase());
            } catch (IllegalArgumentException e) {
                Translation.COMMAND_TOKEN_SETWALLETVIEW_INVALIDVIEW.send(p);
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            int result = MetaCity.getInstance().getTokenManager().updateWalletViewState(id, viewState);
            switch (result) {
                case TokenManager.TOKEN_UPDATE_SUCCESS:
                    Translation.COMMAND_TOKEN_UPDATE_SUCCESS.send(p);
                    break;
                case TokenManager.TOKEN_UPDATE_FAILED:
                    Translation.COMMAND_TOKEN_UPDATE_FAILED.send(p);
                    break;
                case TokenManager.TOKEN_NOSUCHTOKEN:
                    Translation.COMMAND_TOKEN_NOSUCHTOKEN.send(p);
                    break;
                case TokenManager.TOKEN_ISNOTBASE:
                    Translation.COMMAND_TOKEN_ISNONFUNGIBLEINSTANCE.send(p);
                    break;
                case TokenManager.TOKEN_HASWALLETVIEWSTATE:
                    Translation.COMMAND_TOKEN_SETWALLETVIEW_HAS.send(p);
                    break;
                default:
                    Logger.debug(String.format("Unhandled result when setting the wallet view state (status: %d)", result));
                    break;
            }
        });
    }
}
