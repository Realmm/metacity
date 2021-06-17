package org.metacity.metacity.exceptions;

import org.metacity.metacity.player.MetaPlayer;

public class UnregisterTradeInviteException extends RuntimeException {

    public UnregisterTradeInviteException(MetaPlayer inviter, MetaPlayer invitee) {
        super(String.format("Failed to remove trade invites for either inviter (%s) or invitee (%s) where one was suppose to exist",
                inviter.uuid(),
                invitee.uuid()));
    }

}
