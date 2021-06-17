package org.metacity.metacity.token.nfts;

import lombok.NonNull;
import org.metacity.metacity.token.TokenModel;
import org.metacity.metacity.token.TokenPermission;
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

    public NonFungibleToken(@NonNull String id, int amount, @NonNull String alternateId, @NonNull String nbt, @NonNull String metadataURI) {
        for (int i = 1; i < amount; i++) {
            TokenModel model = new TokenModel(id,
                    TokenUtils.formatIndex(String.valueOf(i)),
                    true,
                    alternateId,
                    nbt,
                    null,
                    metadataURI,
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
