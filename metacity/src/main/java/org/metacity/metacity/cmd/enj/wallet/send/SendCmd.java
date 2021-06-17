package org.metacity.metacity.cmd.enj.wallet.send;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.metacity.commands.SubCommand;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.cmd.MetaCommandWrapper;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.util.TokenUtils;
import org.metacity.metacity.util.server.Translation;
import org.metacity.metacity.wallet.MutableBalance;

public class SendCmd extends SubCommand<Player> implements MetaCommandWrapper {

    public SendCmd() {
        super(Player.class);
        addPermission(Permission.CMD_SEND.node());
        addCondition((p, w) -> w.validateNode(1, s -> s.equalsIgnoreCase("send")));
        addCondition((p, w) -> w.hasNode(2));
        setExecution((p, w) -> {
            String target = w.node(2);

            getValidSenderMetaPlayer(p).ifPresent(m -> {
                getValidTargetPlayer(p, target).ifPresent(t -> {
                    getValidTargetMetaPlayer(p, t).ifPresent(tm -> {
                        ItemStack is = p.getInventory().getItemInMainHand();
                        if (is.getType() == Material.AIR || !is.getType().isItem()) {
                            Translation.COMMAND_SEND_MUSTHOLDITEM.send(p);
                            return;
                        } else if (!TokenUtils.isValidTokenItem(is)) {
                            Translation.COMMAND_SEND_ITEMNOTTOKEN.send(p);
                            return;
                        }

                        String tokenId = TokenUtils.getTokenID(is);
                        String tokenIndex = TokenUtils.getTokenIndex(is);

                        MutableBalance balance = m.getTokenWallet().getBalance(tokenId, tokenIndex);
                        if (balance == null || balance.balance() == 0) {
                            Translation.COMMAND_SEND_DOESNOTHAVETOKEN.send(p);
                            return;
                        }

                        balance.deposit(is.getAmount());
                        p.getInventory().clear(p.getInventory().getHeldItemSlot());

                        if (!m.hasEth()) {
                            Translation.WALLET_NOTENOUGHETH.send(p);
                            return;
                        }

                        if (!m.hasAllowance()) {
                            Translation.WALLET_ALLOWANCENOTSET.send(p);
                            return;
                        }

                        MetaCity.getInstance().chain().sendToken(m, tm, tokenId, tokenIndex, is.getAmount());
                    });
                });
            });
        });
    }

}
