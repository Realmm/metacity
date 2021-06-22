package org.metacity.metacity.token.nfts;

import lombok.NonNull;
import org.bukkit.inventory.ItemStack;
import org.metacity.metacity.token.TokenModel;
import org.metacity.metacity.token.TokenPermission;
import org.metacity.metacity.token.TokenType;
import org.metacity.metacity.util.TokenUtils;
import org.metacity.metacity.wallet.TokenWalletViewState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class NonFungibleToken {

    private final List<TokenModel> models = new ArrayList<>();

    public NonFungibleToken(TokenType type) {
        for (int i = 0; i < type.getMaxAmount(); i++) {
            TokenModel model = new TokenModel(type.getId(),
                    TokenUtils.formatIndex(String.valueOf(i)),
                    true,
                    type.getAlternateId(),
                    type.data(),
                    null,
                    type.getMetadataURI(),
                    TokenWalletViewState.WITHDRAWABLE);
            models.add(model);
        }
    }

    public List<TokenModel> models() {
        List<TokenModel> m = new ArrayList<>(models);
        m.sort(Comparator.comparing(t -> TokenUtils.convertIndexToLong(t.getIndex())));
        return m;
    }

}
