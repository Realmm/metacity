package org.metacity.metacity.cmd.chain.wallet.trade;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.metacity.commands.SubCommand;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.cmd.MetaCommandWrapper;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.util.server.Translation;

import java.math.BigInteger;
import java.util.Optional;

public class InviteCmd extends SubCommand<Player> implements MetaCommandWrapper {

    public InviteCmd() {
        super(Player.class);
        addPermission(Permission.CMD_TRADE_INVITE.node());
        addCondition((p, w) -> w.validateNode(1, s -> s.equalsIgnoreCase("invite")));
        addCondition((p, w) -> w.hasNode(2));
        setExecution((p, w) -> {
            String target = w.node(2);

            getValidSenderMetaPlayer(p).ifPresent(m -> {
                getValidTargetPlayer(p, target).ifPresent(t -> {
                    getValidTargetMetaPlayer(p, t).ifPresent(tm -> {
                        if (BigInteger.ZERO.equals(m.getEnjAllowance())) {
                            Translation.WALLET_ALLOWANCENOTSET.send(t);
                            return;
                        }

                        invite(m, tm);
                    });
                });
            });
        });
    }

    private void invite(MetaPlayer sender, MetaPlayer target) {
        boolean result = MetaCity.getInstance().getTradeManager().addInvite(sender, target);
        target.player().ifPresent(t -> {
            sender.player().ifPresent(p -> {
                if (!result) {
                    Translation.COMMAND_TRADE_ALREADYINVITED.send(sender, t.getName());
                    return;
                }

                Translation.COMMAND_TRADE_INVITESENT.send(sender, t.getName());
                Translation.COMMAND_TRADE_INVITEDTOTRADE.send(target, p.getName());
                TextComponent.Builder inviteMessageBuilder = Component.text()
                        .append(Component.text("Accept")
                                .color(NamedTextColor.GREEN)
                                .clickEvent(ClickEvent.runCommand(String.format("/enj trade accept %s",
                                        p.getName()))))
                        .append(Component.text(" | ").color(NamedTextColor.GRAY))
                        .append(Component.text("Decline")
                                .color(NamedTextColor.RED)
                                .clickEvent(ClickEvent.runCommand(String.format("/enj trade decline %s",
                                        p.getName()))));

                t.spigot().sendMessage((BaseComponent) inviteMessageBuilder.build());
            });
        });
    }

    @Override
    public Optional<MetaPlayer> getValidTargetMetaPlayer(@NonNull CommandSender sender, @NonNull Player targetPlayer) throws NullPointerException {
        MetaPlayer targetMetaPlayer = MetaCity.getInstance().getPlayerManager()
                .getPlayer(targetPlayer)
                .orElse(null);
        if (targetMetaPlayer == null) {
            Translation.ERRORS_PLAYERNOTREGISTERED.send(sender, targetPlayer.getName());
            return null;
        } else if (!targetMetaPlayer.isLinked()) {
            Translation.WALLET_NOTLINKED_OTHER.send(sender, targetPlayer.getName());
            Translation.COMMAND_TRADE_WANTSTOTRADE.send(targetPlayer, sender.getName());
            Translation.HINT_LINK.send(targetPlayer);
            return null;
        }

        return Optional.ofNullable(targetMetaPlayer);
    }

}
