package org.metacity.metacity.cmd.enj.wallet.send;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.metacity.commands.SubCommand;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.cmd.MetaCommandWrapper;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.token.TokenModel;
import org.metacity.metacity.util.PlayerUtils;
import org.metacity.metacity.util.TokenUtils;
import org.metacity.metacity.util.server.Translation;

public class DevSendCmd extends SubCommand<ConsoleCommandSender> implements MetaCommandWrapper {

    private static final int ETH_ADDRESS_LENGTH = 42;
    private static final String ETH_ADDRESS_PREFIX = "0x";

    public DevSendCmd() {
        super(ConsoleCommandSender.class);
        addCondition((sender, w) -> w.validateNode(1, s -> s.equalsIgnoreCase("devsend")));
        addCondition((sender, w) -> w.hasNode(3));
        setExecution((sender, w) -> {
            String target = w.node(2);
            String id = w.node(3);

            // Process target address
            String[] targetAddr = {""};
            if (target.startsWith(ETH_ADDRESS_PREFIX) && target.length() == ETH_ADDRESS_LENGTH) {
                targetAddr[0] = target;
            } else if (PlayerUtils.isValidUserName(target)) {
                getValidTargetPlayer(sender, target).ifPresent(t -> {
                    getValidTargetMetaPlayer(sender, t).ifPresent(tm -> {
                        targetAddr[0] = tm.getEthereumAddress();
                    });
                });
            } else {
                Translation.ERRORS_INVALIDPLAYERNAME.send(sender, target);
                return;
            }

            TokenModel tokenModel = MetaCity.getInstance().getTokenManager().getToken(id);
            if (tokenModel == null) {
                Translation.COMMAND_DEVSEND_INVALIDTOKEN.send(sender);
                return;
            }

            // Process send data
            String index = null;
            Integer amount = 1;
            String tokenId = tokenModel.getId();

            if (tokenModel.isNonfungible()) { // Non-fungible token
                try {
                    index = TokenUtils.parseIndex(w.node(4));
                } catch (IllegalArgumentException e) {
                    Translation.COMMAND_TOKEN_INVALIDFULLID.send(sender);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            } else { // Fungible token
                try {
                    amount = Integer.valueOf(w.node(4));
                    if (amount == null || amount <= 0)
                        throw new IllegalArgumentException("Invalid amount to send");
                } catch (IllegalArgumentException e) {
                    Translation.COMMAND_DEVSEND_INVALIDAMOUNT.send(sender);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }

            MetaCity.getInstance().chain().sendToken(null, targetAddr[0], tokenId, index, amount);
        });
    }

}
