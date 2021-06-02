package org.metacity.metacity.player;

import org.metacity.metacity.MetaCity;

public class Wallet {

    private final MetaPlayer metaPlayer;

    protected Wallet(MetaPlayer metaPlayer) {
        this.metaPlayer = metaPlayer;
    }

    public MetaPlayer getMetaPlayer() {
        return metaPlayer;
    }

//    public void getBalance() {
//        return MetaCity.getInstance().getEnjin().getIdentityCode("");
//    }

}
