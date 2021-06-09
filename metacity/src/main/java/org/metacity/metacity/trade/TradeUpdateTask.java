package org.metacity.metacity.trade;

import com.enjin.sdk.graphql.GraphQLResponse;
import com.enjin.sdk.http.HttpResponse;
import com.enjin.sdk.models.request.GetRequests;
import com.enjin.sdk.models.request.Transaction;
import com.enjin.sdk.models.request.TransactionState;
import com.enjin.sdk.models.token.event.TokenEvent;
import com.enjin.sdk.models.token.event.TokenEventType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Consumer;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.exceptions.GraphQLException;
import org.metacity.metacity.exceptions.NetworkException;

import java.sql.SQLException;
import java.util.List;

public class TradeUpdateTask extends BukkitRunnable {

    private final MetaCity plugin;
    private final List<TradeSession> tradeSessions;

    public TradeUpdateTask() throws SQLException {
        this.plugin = MetaCity.getInstance();
        this.tradeSessions = plugin.db().getPendingTrades();
    }

    @Override
    public void run() {
        try {
            if (tradeSessions.isEmpty()) {
                if (!isCancelled())
                    cancel();
                return;
            }

            TradeSession session = tradeSessions.remove(0);
            if (session.isExpired()) {
                plugin.getTradeManager().cancelTrade(session.getMostRecentRequestId());
                return;
            }

            getMostRecentTransaction(session, data -> {
                if (data.isEmpty()) return;

                processTransaction(session, data.get(0));
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getMostRecentTransaction(TradeSession session, Consumer<List<Transaction>> consumer) {
        plugin.chain().getMostRecentTransaction(session, consumer);
    }

    private TokenEvent getTokenEvent(Transaction transaction) {
        if (transaction == null)
            return null;

        List<TokenEvent> events = transaction.getEvents();
        if (events == null)
            return null;

        return events.stream()
                .filter(e -> e.getEvent() == TokenEventType.CREATE_TRADE
                        || e.getEvent() == TokenEventType.COMPLETE_TRADE)
                .findFirst()
                .orElse(null);
    }

    private void processTransaction(TradeSession session, Transaction transaction) {
        TransactionState state = transaction.getState();
        if (state == TransactionState.CANCELED_USER || state == TransactionState.CANCELED_PLATFORM) {
            plugin.getTradeManager().cancelTrade(transaction.getId());
            return;
        } else if (state != TransactionState.EXECUTED) {
            return;
        }

        TokenEvent     event =  getTokenEvent(transaction);
        TokenEventType type  = event == null
                ? TokenEventType.UNKNOWN_EVENT
                : event.getEvent();
        switch (type) {
            case CREATE_TRADE:
                plugin.getTradeManager().sendCompleteRequest(session, event.getParam1());
                break;
            case COMPLETE_TRADE:
                plugin.getTradeManager().completeTrade(session);
                break;
            default:
                break;
        }
    }
}
