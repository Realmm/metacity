package org.metacity.metacity;

import com.enjin.sdk.TrustedPlatformClient;
import com.enjin.sdk.services.notification.NotificationsService;
import org.metacity.metacity.player.PlayerManagerApi;
import org.metacity.metacity.token.TokenManager;

public interface Bootstrap {

    TrustedPlatformClient getTrustedPlatformClient();

    NotificationsService getNotificationsService();

    PlayerManagerApi getPlayerManager();

    TokenManager getTokenManager();

}
