package org.metacity.metacity.wallet;

import com.enjin.sdk.models.balance.Balance;
import org.metacity.metacity.util.TokenUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenWallet {

    private final Map<String, MutableBalance> balances;

    public TokenWallet() {
        this.balances = new ConcurrentHashMap<>();
    }

    public void addBalances(List<Balance> balances) {
        balances.forEach(balance -> setBalance(new MutableBalance(balance)));
    }

    public MutableBalance removeBalance(String id) {
        try {
            return balances.remove(TokenUtils.toFullId(id));
        } catch (IllegalArgumentException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public MutableBalance removeBalance(String tokenId, String tokenIndex) {
        try {
            return removeBalance(TokenUtils.createFullId(tokenId, tokenIndex));
        } catch (IllegalArgumentException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public MutableBalance getBalance(String id) {
        try {
            return balances.get(TokenUtils.toFullId(id));
        } catch (IllegalArgumentException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public MutableBalance getBalance(String tokenId, String tokenIndex) {
        try {
            return getBalance(TokenUtils.createFullId(tokenId, tokenIndex));
        } catch (IllegalArgumentException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<MutableBalance> getBalances() {
        return new ArrayList<>(balances.values());
    }

    public void setBalance(MutableBalance balance) {
        if (balance == null)
            return;

        try {
            balances.put(TokenUtils.createFullId(balance.id(), balance.index()), balance);
        } catch (IllegalArgumentException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, MutableBalance> getBalancesMap() {
        return new HashMap<>(balances);
    }

    public boolean isEmpty() {
        return balances.isEmpty();
    }

    public void clear() {
        this.balances.clear();
    }

}
