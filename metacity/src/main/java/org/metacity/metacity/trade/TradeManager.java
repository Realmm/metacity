package org.metacity.metacity.trade;

import com.enjin.sdk.models.request.data.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.enums.Trader;
import org.metacity.metacity.events.MetaPlayerQuitEvent;
import org.metacity.metacity.exceptions.UnregisterTradeInviteException;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.util.TokenUtils;
import org.metacity.metacity.util.server.Translation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TradeManager implements Listener {

    private final MetaCity plugin;

    public TradeManager() {
        this.plugin = MetaCity.getInstance();
    }

    public boolean inviteExists(MetaPlayer sender, MetaPlayer target) {
        return sender.getSentTradeInvites().contains(target);
    }

    public boolean addInvite(MetaPlayer inviter, MetaPlayer invitee) {
        if (!inviteExists(inviter, invitee)) {
            inviter.getSentTradeInvites().add(invitee);
            invitee.getReceivedTradeInvites().add(inviter);
            return true;
        }

        return false;
    }

    public boolean acceptInvite(MetaPlayer inviter, MetaPlayer invitee) throws UnregisterTradeInviteException {
        if (inviteExists(inviter, invitee)) {
            boolean removedFromInviter = inviter.getSentTradeInvites().remove(invitee);
            boolean removedFromInvitee = invitee.getReceivedTradeInvites().remove(inviter);

            if (removedFromInviter && removedFromInvitee) {
                inviter.setActiveTradeView(new TradeView(inviter, invitee, Trader.INVITER));
                invitee.setActiveTradeView(new TradeView(invitee, inviter, Trader.INVITED));
                inviter.getActiveTradeView().open();
                invitee.getActiveTradeView().open();
                return true;
            }

            throw new UnregisterTradeInviteException(inviter, invitee);
        }

        return false;
    }

    public boolean declineInvite(MetaPlayer inviter, MetaPlayer invitee) throws UnregisterTradeInviteException {
        if (inviteExists(inviter, invitee)) {
            boolean removedFromInviter = inviter.getSentTradeInvites().remove(invitee);
            boolean removedFromInvitee = invitee.getReceivedTradeInvites().remove(inviter);
            if (removedFromInviter && removedFromInvitee)
                return true;

            throw new UnregisterTradeInviteException(inviter, invitee);
        }

        return false;
    }

    public void completeTrade(Integer requestId) {
        try {
            TradeSession session = plugin.db().getSessionFromRequestId(requestId);
            completeTrade(session);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void completeTrade(TradeSession session) {
        if (session == null)
            return;

        Optional<Player> inviter = Optional.ofNullable(Bukkit.getPlayer(session.getInviterUuid()));
        Optional<Player> invitee = Optional.ofNullable(Bukkit.getPlayer(session.getInvitedUuid()));

        inviter.ifPresent(Translation.COMMAND_TRADE_COMPLETE::send);
        invitee.ifPresent(Translation.COMMAND_TRADE_COMPLETE::send);

        try {
            plugin.db().tradeExecuted(session.getCompleteRequestId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendCompleteRequest(Integer requestId, String tradeId) {
        try {
            TradeSession session = plugin.db().getSessionFromRequestId(requestId);
            sendCompleteRequest(session, tradeId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendCompleteRequest(TradeSession session, String tradeId) {
        plugin.chain().sendCompleteRequest(session, tradeId);
    }

    public void createTrade(MetaPlayer inviter,
                            MetaPlayer invitee,
                            List<ItemStack> inviterOffer,
                            List<ItemStack> invitedOffer) throws IllegalArgumentException, NullPointerException {
        if (inviter == null || invitee == null)
            throw new NullPointerException("Inviter or invited EnjPlayer is null");
        else if (!inviter.isLinked() || !invitee.isLinked())
            throw new IllegalArgumentException("Inviter or invited EnjPlayer is not linked");

        if (inviterOffer.isEmpty() && invitedOffer.isEmpty())
            return;
        else if (inviterOffer.isEmpty())
            send(invitee, inviter, invitedOffer);
        else if (invitedOffer.isEmpty())
            send(inviter, invitee, inviterOffer);
        else
            createTradeRequest(inviter, invitee, extractOffers(inviterOffer), extractOffers(invitedOffer));
    }

    private void send(MetaPlayer inviter, MetaPlayer invitee, List<ItemStack> tokens) {
        plugin.chain().sendTrade(inviter, invitee, tokens);
    }

    private void createTradeRequest(MetaPlayer inviter, MetaPlayer invitee, List<TokenValueData> playerOneTokens, List<TokenValueData> playerTwoTokens) {
        plugin.chain().createTradeRequest(inviter, invitee, playerOneTokens, playerTwoTokens);
    }

    public void cancelTrade(Integer requestId) {
        try {
            plugin.db().cancelTrade(requestId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<TokenValueData> extractOffers(List<ItemStack> offers) {
        List<TokenValueData> extractedOffers = new ArrayList<>();

        for (ItemStack is : offers) {
            if (!TokenUtils.isValidTokenItem(is))
                continue;

            int value = is.getAmount();
            String id = TokenUtils.getTokenID(is);

            if (TokenUtils.isNonFungible(is)) {
                String index = TokenUtils.getTokenIndex(is);
                Integer intIndex = TokenUtils.convertIndexToLong(index).intValue();

                extractedOffers.add(TokenValueData.builder()
                        .id(id)
                        .index(intIndex)
                        .value(value)
                        .build());
            } else {
                extractedOffers.add(TokenValueData.builder()
                        .id(id)
                        .value(value)
                        .build());
            }
        }

        return extractedOffers;
    }

    @EventHandler
    public void on(MetaPlayerQuitEvent event) {
        MetaPlayer player = event.getPlayer();

        player.getSentTradeInvites().forEach(other -> other.getReceivedTradeInvites().remove(player));
        player.getReceivedTradeInvites().forEach(other -> other.getSentTradeInvites().remove(player));

        player.player().ifPresent(Player::closeInventory);
    }

}
