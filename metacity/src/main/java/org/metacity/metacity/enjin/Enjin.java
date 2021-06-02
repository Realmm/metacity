package org.metacity.metacity.enjin;

import com.enjin.sdk.TrustedPlatformClient;
import com.enjin.sdk.TrustedPlatformClientBuilder;
import com.enjin.sdk.graphql.GraphQLResponse;
import com.enjin.sdk.http.HttpResponse;
import com.enjin.sdk.models.AccessToken;
import com.enjin.sdk.models.balance.Balance;
import com.enjin.sdk.models.identity.Identity;
import com.enjin.sdk.models.request.CreateRequest;
import com.enjin.sdk.models.request.Transaction;
import com.enjin.sdk.models.request.data.AdvancedSendTokenData;
import com.enjin.sdk.models.request.data.ApproveEnjData;
import com.enjin.sdk.models.request.data.SendEnjData;
import com.enjin.sdk.models.request.data.SendTokenData;
import com.enjin.sdk.models.request.data.TransferData;
import com.enjin.sdk.models.user.AuthPlayer;
import com.enjin.sdk.models.user.GetUser;
import com.enjin.sdk.models.user.User;
import com.enjin.sdk.models.wallet.GetWallet;
import com.enjin.sdk.models.wallet.Wallet;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public final class Enjin {

    private final TrustedPlatformClient client;

    public Enjin() {
        this.client = new TrustedPlatformClientBuilder()
                .baseUrl(TrustedPlatformClientBuilder.KOVAN)
                .build();
        if (!authApp()) throw new IllegalStateException("Enjin could not be authenticated");
    }

    private boolean authApp() {
        this.client.authAppSync(Secret.TESTING ? Secret.TEST_ID : Secret.ID, Secret.TESTING ? Secret.TEST_SECRET : Secret.SECRET);
        return client.isAuthenticated();
    }

    public String getDeveloperAddress() {
        return Secret.ADDRESS;
    }

    public void authPlayer(OfflinePlayer p) {
        List<Identity> identities = getIdentities(authPlayer(p.getUniqueId().toString()).getAccessToken());
        Bukkit.broadcastMessage("link:");
        identities.forEach(i -> {
            Bukkit.broadcastMessage(i.getLinkingCodeQr());
        });
    }

    private AccessToken authPlayer(String id) {
        AuthPlayer input = new AuthPlayer().id(id);
        HttpResponse<GraphQLResponse<AccessToken>> httpResponse = client.getUserService().authUserSync(input);
        if (!httpResponse.isEmpty()) {
            GraphQLResponse<AccessToken> graphQLResponse = httpResponse.body();

            if (!graphQLResponse.hasErrors()) {
                return graphQLResponse.getData();
            }
        }
        throw new IllegalStateException("Player not authed");
    }

    public List<Identity> getIdentities(String name) {
        GetUser query = new GetUser().name(name)
                .withUserIdentities(); // Includes the user identities
        HttpResponse<GraphQLResponse<User>> httpResponse = client.getUserService().getUserSync(query);
        if (!httpResponse.isEmpty()) {
            GraphQLResponse<User> graphQLResponse = httpResponse.body();

            if (!graphQLResponse.hasErrors()) {
                User user = graphQLResponse.getData();
                return user.getIdentities();
            }
        }
        throw new IllegalStateException("No user found with " + name);
    }

    public User getUser(String name) {
        GetUser query = new GetUser().name(name);
        HttpResponse<GraphQLResponse<User>> httpResponse = client.getUserService().getUserSync(query);
        if (!httpResponse.isEmpty()) {
            GraphQLResponse<User> graphQLResponse = httpResponse.body();

            if (!graphQLResponse.hasErrors()) {
                return graphQLResponse.getData();
            }
        }
        throw new IllegalStateException("No user found with " + name);
    }

    public void getIdentityCode(String name) {
        GetUser query = new GetUser().name(name)
                .withUserIdentities()
                .withLinkingCode()    // Includes the linking code and
                .withLinkingCodeQr(); // the qr url for the identities
        HttpResponse<GraphQLResponse<User>> httpResponse = client.getUserService().getUserSync(query);
        if (!httpResponse.isEmpty()) {
            GraphQLResponse<User> graphQLResponse = httpResponse.body();

            if (!graphQLResponse.hasErrors()) {
                User user = graphQLResponse.getData();
                List<Identity> identities = user.getIdentities();

                if (identities.size() > 0) {
                    Identity identity = identities.get(0);
                    String linkingCode = identity.getLinkingCode();
                    String qrCodeUrl = identity.getLinkingCodeQr();
                }
            }
        }
    }

    public void getWallet(String ethAddr) {
        GetWallet query = new GetWallet().ethAddress(ethAddr)
                .withBalances(); // Includes the balances
        HttpResponse<GraphQLResponse<Wallet>> httpResponse = client.getWalletService().getWalletSync(query);
        if (!httpResponse.isEmpty()) {
            GraphQLResponse<Wallet> graphQLResponse = httpResponse.body();

            if (!graphQLResponse.hasErrors()) {
                Wallet wallet = graphQLResponse.getData();
                List<Balance> balances = wallet.getBalances();
            }
        }
    }

    public List<Balance> getBalances(String name) {
        GetUser query = new GetUser().name(name)
                .withUserIdentities()
                .withWallet()
                .withBalances();

        HttpResponse<GraphQLResponse<User>> httpResponse = client.getUserService().getUserSync(query);
        if (!httpResponse.isEmpty()) {
            GraphQLResponse<User> graphQLResponse = httpResponse.body();
            if (!graphQLResponse.hasErrors()) {
                User user = graphQLResponse.getData();
                List<Identity> identities = user.getIdentities();
                if (identities.size() > 0) {
                    Identity identity = identities.get(0);
                    Wallet wallet = identity.getWallet();
                    return wallet.getBalances();
                }
            }
        }
        throw new IllegalStateException("No balance");
    }

    public void createRequest(Integer appId, int identityId) {
        ApproveEnjData data = ApproveEnjData.builder()
                .value(-1)
                .build();
        CreateRequest input = new CreateRequest().appId(appId)
                .identityId(identityId)
                .approveEnj(data);
        HttpResponse<GraphQLResponse<Transaction>> httpResponse = client.getRequestService().createRequestSync(input);
        if (!httpResponse.isEmpty()) {
            GraphQLResponse<Transaction> graphQLResponse = httpResponse.body();

            if (!graphQLResponse.hasErrors()) {
                Transaction transaction = graphQLResponse.getData();
            }
        }
    }

    public void sendEnj(Integer appId, int senderId, String to, String value) {
        SendEnjData data = SendEnjData.builder()
                .to(to)
                .value(value).build();
        CreateRequest input = new CreateRequest().appId(appId)
                .identityId(senderId)
                .sendEnj(data);
        HttpResponse<GraphQLResponse<Transaction>> httpResponse = client.getRequestService().createRequestSync(input);

        if (!httpResponse.isEmpty()) {
            GraphQLResponse<Transaction> graphQLResponse = httpResponse.body();

            if (!graphQLResponse.hasErrors()) {
                Transaction transaction = graphQLResponse.getData();
            }
        }
    }

    public void sendToken(Integer appId, int senderId, String to, String tokenId, Integer value) {
        SendTokenData data = SendTokenData.builder()
                .recipientAddress(to)
                .tokenId(tokenId)
                .value(value).build();
        CreateRequest input = new CreateRequest().appId(appId)
                .identityId(senderId)
                .sendToken(data);
        HttpResponse<GraphQLResponse<Transaction>> httpResponse = client.getRequestService().createRequestSync(input);

        if (!httpResponse.isEmpty()) {
            GraphQLResponse<Transaction> graphQLResponse = httpResponse.body();

            if (!graphQLResponse.hasErrors()) {
                Transaction transaction = graphQLResponse.getData();
            }
        }
    }

    public void sendToken(Integer appId, Integer senderId, String[] toIds, String tokenId, String value) {
        List<TransferData> transfers = new ArrayList<>();

        for (String toId : toIds) {
            transfers.add(TransferData.builder()
                    .fromId(senderId)
                    .toId(Integer.valueOf(toId))
                    .tokenId(tokenId)
                    .value(value)
                    .build());
        }

        AdvancedSendTokenData data = AdvancedSendTokenData.builder()
                .transfers(transfers)
                .build();
        CreateRequest input = new CreateRequest().appId(appId)
                .identityId(senderId)
                .advancedSendToken(data);
        HttpResponse<GraphQLResponse<Transaction>> httpResponse = client.getRequestService().createRequestSync(input);

        if (!httpResponse.isEmpty()) {
            GraphQLResponse<Transaction> graphQLResponse = httpResponse.body();

            if (!graphQLResponse.hasErrors()) {
                Transaction transaction = graphQLResponse.getData();
            }
        }
    }

    private static class Secret {

        private final static boolean TESTING = boolVal("test");
        private final static String ADDRESS = val("address");
        private final static String SECRET = val("secret");
        private final static String TEST_SECRET = val("test-secret");
        private final static int ID = intVal("id");
        private final static int TEST_ID = intVal("test-id");

        private static JSONObject json() {
            File file = new File("secrets.json");
            try {
                FileInputStream input = new FileInputStream(file);
                InputStreamReader reader = new InputStreamReader(input, "UTF-8");
                return (JSONObject) new JSONParser().parse(reader);
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
            throw new IllegalStateException("JsonObject not properly parsed");
        }

        private static int intVal(String key) {
            String val = val(key);
            int i;
            try {
                i = Integer.parseInt(val);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Key " + key + " is not of type integer in secrets.json");
            }
            return i;
        }

        private static boolean boolVal(String key) {
            String val = val(key);
            if (!val.equalsIgnoreCase("true") && !val.equalsIgnoreCase("false"))
                throw new IllegalStateException("Key " + key + " is not of boolean type in secrets.json");
            return Boolean.parseBoolean(val(key));
        }

        private static String val(String key) {
            JSONObject object = json();
            if (!object.containsKey(key)) throw new IllegalArgumentException("Unable to get key " + key + " from secrets.json");
            return object.get(key).toString();
        }

    }

}
