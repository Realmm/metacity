package org.metacity.metacity.player;

import com.enjin.sdk.graphql.GraphQLResponse;
import com.enjin.sdk.http.HttpResponse;
import com.enjin.sdk.models.balance.Balance;
import com.enjin.sdk.models.balance.GetBalances;
import com.enjin.sdk.models.identity.GetIdentities;
import com.enjin.sdk.models.identity.Identity;
import com.enjin.sdk.models.identity.UnlinkIdentity;
import com.enjin.sdk.models.user.User;
import com.enjin.sdk.models.wallet.Wallet;
import com.enjin.sdk.services.notification.NotificationsService;
import de.tr7zw.changeme.nbtapi.NBTItem;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.*;
import org.metacity.metacity.Chain;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.exceptions.GraphQLException;
import org.metacity.metacity.exceptions.NetworkException;
import org.metacity.metacity.mmo.MMOPlayer;
import org.metacity.metacity.player.scoreboard.MetaTemplate;
import org.metacity.metacity.token.TokenManager;
import org.metacity.metacity.token.TokenModel;
import org.metacity.metacity.trade.TradeView;
import org.metacity.metacity.util.QrUtils;
import org.metacity.metacity.util.TokenUtils;
import org.metacity.metacity.util.server.MetaConfig;
import org.metacity.metacity.util.server.Translation;
import org.metacity.metacity.wallet.MutableBalance;
import org.metacity.metacity.wallet.TokenWallet;
import org.metacity.metacity.wallet.TokenWalletView;
import org.metacity.metacity.wallet.TokenWalletViewState;
import org.metacity.scoreboard.UberBoard;
import org.metacity.util.CC;
import org.metacity.util.Logger;

import javax.annotation.Nullable;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.*;

public class MetaPlayer extends MMOPlayer {

    private final UberBoard uberBoard;

    // Bukkit Fields
    private final UUID uuid;
    private final QuestionablePlayer questionablePlayer;

    // User Data
    private User user;
    private Integer userId;

    // Identity Data
    private Identity identity;
    private Integer identityId;
    private Wallet wallet;
    private String linkingCode;
    private Image linkingCodeQr;
    private final TokenWallet tokenWallet;

    // State Fields
    private boolean userLoaded;
    private boolean identityLoaded;
    private MetaPermissionAttachment globalAttachment;
    private MetaPermissionAttachment worldAttachment;
    private final Map<String, Set<String>> worldPermissionMap = new HashMap<>();

    // Trade Fields
    private final List<MetaPlayer> sentTradeInvites = new ArrayList<>();
    private final List<MetaPlayer> receivedTradeInvites = new ArrayList<>();
    private TradeView activeTradeView;

    // Wallet Fields
    private TokenWalletView activeWalletView;

    // Mutexes
    protected final Object linkingCodeQrLock = new Object();

    private final PlayerListener listener;

    public MetaPlayer(OfflinePlayer p) {
        super(p);
        this.uuid = p.getUniqueId();
        this.questionablePlayer = new QuestionablePlayer(uuid);
        this.tokenWallet = new TokenWallet();

        this.listener = PlayerListener.of(this);
        MetaCity.getInstance().getServer().getPluginManager().registerEvents(listener, MetaCity.getInstance());

        this.uberBoard = UberBoard.of(p, new MetaTemplate());
        uberBoard.setColor(ChatColor.BLUE);
    }

    public World world() {
        return player().orElseThrow(() -> new IllegalStateException("Unable to get world from offline player")).getWorld();
    }

    void onJoin() {
        questionablePlayer.getPlayer().ifPresent(p -> {
            this.globalAttachment = new MetaPermissionAttachment(p);
            this.worldAttachment = new MetaPermissionAttachment(p);

            if (identityLoaded) {
                if (isLinked()) {
                    Bukkit.getScheduler().runTask(MetaCity.getInstance(), this::addLinkPermissions);
                } else {
                    Bukkit.getScheduler().runTask(MetaCity.getInstance(), this::removeTokenizedItems);
                    Bukkit.getScheduler().runTask(MetaCity.getInstance(), this::removeLinkPermissions);
                    return;
                }

                removeQrMap();

                if (identity.getWallet().getEnjAllowance() == null || identity.getWallet()
                        .getEnjAllowance()
                        .doubleValue() <= 0.0) {
                    Translation.WALLET_ALLOWANCENOTSET.send(p);
                }
            }

            MetaCity.getInstance().chain().updateWallet(this, w -> this.wallet = w);
            validateInventory();
            initPermissions();
            board().update();
        });
    }

    private String formatTime(long time) {
        boolean am = time < 12000;
        int hr = (int) Math.floor(time / 1000D);
        int mins = Math.min(Math.round((60F / (time - (hr * 1000))) * 1000), 59);
        return hr + ":" + mins + " " + (am ? "AM" : "PM");
    }

    public UberBoard board() {
        return uberBoard;
    }

    public UUID uuid() {
        return uuid;
    }

    public Optional<Player> player() {
        return questionablePlayer.getPlayer();
    }

    public Optional<OfflinePlayer> offlinePlayer() {
        return questionablePlayer.getOfflinePlayer();
    }

    public Optional<User> user() {
        return Optional.ofNullable(user);
    }

    public Optional<Identity> identity() {
        return Optional.ofNullable(identity);
    }

    public Optional<Wallet> wallet() {
        return Optional.ofNullable(wallet);
    }

    protected void setUser(User user) {
        if (user == null) {
            userId = null;
            userLoaded = false;
        } else {
            userId = user.getId();
            userLoaded = true;
            this.user = user;

            Optional<Identity> optionalIdentity = user.getIdentities().stream()
                    .filter(identity -> identity.getAppId() == MetaConfig.getAppId())
                    .findFirst();
            optionalIdentity.ifPresent(identity -> identityId = identity.getId());
        }
    }

    protected void setIdentity(Identity identity, @Nullable Runnable callback) {
//        identityId = null;  // Assume player has no identity
//        wallet = null;  //
//        linkingCode = null;  //
//        setLinkingCodeQr(null); //
//        identityLoaded = false; //
        tokenWallet.clear();
        Bukkit.getScheduler().runTask(MetaCity.getInstance(), () -> {
            if (globalAttachment != null) globalAttachment.clear();   // Clears all permissions
            if (worldAttachment != null) worldAttachment.clear();    //
            worldPermissionMap.clear(); //

            this.identity = identity;
            identityId = identity == null ? identityId : identity.getId();
            wallet = identity == null ? wallet : identity.getWallet();
            linkingCode = identity == null ? linkingCode : identity.getLinkingCode();
            if (identity == null) setLinkingCodeQr(null); //

            if (identity == null) return;

            FetchQrImageTask.fetch(this, identity.getLinkingCodeQr());

            identityLoaded = true;

            Chain chain = MetaCity.getInstance().chain();
            chain.listenToIdentity(identityId);

            chain.updateWallet(this, w -> this.wallet = w);
            if (player().isPresent()) initPermissions();
            if (callback != null) callback.run();
        });
    }

    /**
     * Validate the players inventory, ensuring it is up to date with the players wallet
     */
    public void validateInventory() {
        player().ifPresent(p -> {
            tokenWallet.getBalances().forEach(MutableBalance::reset);

            validatePlayerInventory();
            validatePlayerEquipment();
            validatePlayerCursor();

            if (activeTradeView != null)
                activeTradeView.validateInventory();
            if (activeWalletView != null)
                activeWalletView.validateInventory();
        });
    }

    /**
     * Update the token with the given id, so the players inventory matches the new token id
     *
     * @param id The token id
     */
    public void updateToken(String id) {
        player().ifPresent(p -> {
            if (tokenWallet == null) return;

            TokenModel tokenModel = MetaCity.getInstance().getTokenManager().getToken(id);
            if (tokenModel == null)
                return;

            MutableBalance balance = tokenWallet.getBalance(tokenModel.getFullId());
            if (balance == null || balance.withdrawn() == 0)
                return;

            updatePlayerInventory(tokenModel, balance);
            updatePlayerEquipment(tokenModel, balance);
            updatePlayerCursor(tokenModel, balance);

            if (activeTradeView != null)
                activeTradeView.updateInventory();
            if (activeWalletView != null)
                activeWalletView.updateInventory();
        });
    }

    private void validatePlayerInventory() {
        player().ifPresent(p -> {
            TokenManager tokenManager = MetaCity.getInstance().getTokenManager();

            PlayerInventory inventory = p.getInventory();
            for (int i = inventory.getSize() - 1; i >= 0; i--) {
                ItemStack is = inventory.getItem(i);
                if (!TokenUtils.hasTokenData(is)) {
                    continue;
                } else if (!TokenUtils.isValidTokenItem(is)) {
                    inventory.clear(i);
                    Logger.debug(String.format("Removed corrupted token from %s's inventory", p.getDisplayName()));
                    continue;
                }

                String fullId = TokenUtils.createFullId(TokenUtils.getTokenID(is), TokenUtils.getTokenIndex(is));
                TokenModel tokenModel = tokenManager.getToken(fullId);
                MutableBalance balance = tokenWallet.getBalance(fullId);

                if (TokenUtils.isNonFungible(is) && tokenModel == null) {
                    tokenModel = tokenManager.getToken(TokenUtils.getTokenID(is));
                }

                if (tokenModel == null
                        || balance == null
                        || balance.amountAvailableForWithdrawal() == 0) {
                    inventory.clear(i);
                } else if (tokenModel.getWalletViewState() != TokenWalletViewState.WITHDRAWABLE) {
                    balance.deposit(is.getAmount());
                    inventory.clear(i);
                } else {
                    if (balance.amountAvailableForWithdrawal() < is.getAmount())
                        is.setAmount(balance.amountAvailableForWithdrawal());

                    balance.withdraw(is.getAmount());

                    updateTokenInInventoryCheck(tokenModel, balance, is, inventory, i);
                }
            }
        });

    }

    private void updateTokenInInventoryCheck(TokenModel tokenModel,
                                             MutableBalance balance,
                                             ItemStack is,
                                             Inventory inventory,
                                             int idx) {
        ItemStack newStack = tokenModel.getItemStack();
        if (newStack == null) {
            balance.deposit(is.getAmount());
            inventory.clear(idx);
            return;
        }

        newStack.setAmount(is.getAmount());

        String newNBT = NBTItem.convertItemtoNBT(newStack).toString();
        String itemNBT = NBTItem.convertItemtoNBT(is).toString();
        if (itemNBT.equals(newNBT)) {
            return;
        } else if (is.getAmount() > newStack.getMaxStackSize()) {
            balance.deposit(is.getAmount() - newStack.getMaxStackSize());
            newStack.setAmount(newStack.getMaxStackSize());
        }

        inventory.setItem(idx, newStack);
    }

    private void validatePlayerEquipment() {
        player().ifPresent(p -> {
            TokenManager tokenManager = MetaCity.getInstance().getTokenManager();

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack is = getEquipment(slot);
                if (!TokenUtils.hasTokenData(is)) {
                    continue;
                } else if (!TokenUtils.isValidTokenItem(is)) {
                    setEquipment(slot, null);
                    Logger.debug(String.format("Removed corrupted token from %s's equipment", p.getDisplayName()));
                    continue;
                }

                String fullId = TokenUtils.createFullId(TokenUtils.getTokenID(is),
                        TokenUtils.getTokenIndex(is));
                TokenModel tokenModel = tokenManager.getToken(fullId);
                MutableBalance balance = tokenWallet.getBalance(fullId);
                if (tokenModel == null
                        || balance == null
                        || balance.amountAvailableForWithdrawal() == 0) {
                    setEquipment(slot, null);
                } else if (tokenModel.getWalletViewState() != TokenWalletViewState.WITHDRAWABLE) {
                    balance.deposit(is.getAmount());
                    setEquipment(slot, null);
                } else {
                    if (balance.amountAvailableForWithdrawal() < is.getAmount())
                        is.setAmount(balance.amountAvailableForWithdrawal());

                    balance.withdraw(is.getAmount());

                    updateTokenInEquipmentCheck(tokenModel, balance, is, slot);
                }
            }
        });
    }

    private void updateTokenInEquipmentCheck(TokenModel tokenModel,
                                             MutableBalance balance,
                                             ItemStack is,
                                             EquipmentSlot slot) {
        ItemStack newStack = tokenModel.getItemStack();
        if (newStack == null) {
            balance.deposit(is.getAmount());
            setEquipment(slot, null);
            return;
        }

        newStack.setAmount(is.getAmount());

        String newNBT = NBTItem.convertItemtoNBT(newStack).toString();
        String itemNBT = NBTItem.convertItemtoNBT(is).toString();
        if (itemNBT.equals(newNBT)) {
            return;
        } else if (is.getAmount() > newStack.getMaxStackSize()) {
            balance.deposit(is.getAmount() - newStack.getMaxStackSize());
            newStack.setAmount(newStack.getMaxStackSize());
        }

        if (slot == EquipmentSlot.OFF_HAND || slot == EquipmentSlot.HAND || is.getType() == newStack.getType()) {
            setEquipment(slot, newStack);
        } else {
            setEquipment(slot, null);
            balance.deposit(newStack.getAmount());
        }
    }

    private void validatePlayerCursor() {
        player().ifPresent(p -> {
            TokenManager tokenManager = MetaCity.getInstance().getTokenManager();

            InventoryView view = p.getOpenInventory();
            ItemStack is = view.getCursor();
            if (!TokenUtils.hasTokenData(is)) {
                return;
            } else if (!TokenUtils.isValidTokenItem(is)) {
                view.setCursor(null);
                Logger.debug(String.format("Removed corrupted token from %s's cursor", p.getDisplayName()));
                return;
            }

            String fullId = TokenUtils.createFullId(TokenUtils.getTokenID(is),
                    TokenUtils.getTokenIndex(is));
            TokenModel tokenModel = tokenManager.getToken(fullId);
            MutableBalance balance = tokenWallet.getBalance(fullId);
            if (tokenModel == null
                    || balance == null
                    || balance.amountAvailableForWithdrawal() == 0) {
                view.setCursor(null);
            } else if (tokenModel.getWalletViewState() != TokenWalletViewState.WITHDRAWABLE) {
                balance.deposit(is.getAmount());
                view.setCursor(null);
            } else {
                if (balance.amountAvailableForWithdrawal() < is.getAmount()) {
                    is.setAmount(balance.amountAvailableForWithdrawal());
                }

                balance.withdraw(is.getAmount());

                updateTokenInCursorCheck(tokenModel, balance, is, view);
            }
        });
    }

    private void updateTokenInCursorCheck(TokenModel tokenModel,
                                          MutableBalance balance,
                                          ItemStack is,
                                          InventoryView view) {
        ItemStack newStack = tokenModel.getItemStack();
        if (newStack == null) {
            balance.deposit(is.getAmount());
            view.setCursor(null);
            return;
        }

        newStack.setAmount(is.getAmount());

        String newNBT = NBTItem.convertItemtoNBT(newStack).toString();
        String itemNBT = NBTItem.convertItemtoNBT(is).toString();
        if (itemNBT.equals(newNBT)) {
            return;
        } else {
            balance.deposit(is.getAmount() - newStack.getMaxStackSize());
            newStack.setAmount(newStack.getMaxStackSize());
        }

        view.setCursor(newStack);
    }

    private ItemStack getEquipment(EquipmentSlot slot) {
        if (!player().isPresent()) throw new IllegalStateException("Unable to get equipment of offline player");
        Player p = player().get();
        PlayerInventory inventory = p.getInventory();

        switch (slot) {
            case HAND:
                return null;
            case OFF_HAND:
                return inventory.getItemInOffHand();
            case CHEST:
                return inventory.getChestplate();
            case LEGS:
                return inventory.getLeggings();
            case HEAD:
                return inventory.getHelmet();
            case FEET:
                return inventory.getBoots();
            default:
                Logger.debug(String.format("Unsupported equipment slot type \"%s\"", slot.name()));
                return null;
        }
    }

    private void setEquipment(EquipmentSlot slot, ItemStack is) {
        player().ifPresent(p -> {
            PlayerInventory inventory = p.getInventory();

            switch (slot) {
                case HAND:
                    break;
                case OFF_HAND:
                    inventory.setItemInOffHand(is);
                    break;
                case CHEST:
                    inventory.setChestplate(is);
                    break;
                case LEGS:
                    inventory.setLeggings(is);
                    break;
                case HEAD:
                    inventory.setHelmet(is);
                    break;
                case FEET:
                    inventory.setBoots(is);
                    break;
                default:
                    Logger.debug(String.format("Unsupported equipment slot type \"%s\"", slot.name()));
                    break;
            }
        });

    }

    private void updatePlayerInventory(@NonNull TokenModel tokenModel,
                                       @NonNull MutableBalance balance) throws NullPointerException {
        player().ifPresent(p -> {
            PlayerInventory inventory = p.getInventory();
            for (int i = 0; i < inventory.getStorageContents().length; i++) {
                ItemStack is = inventory.getItem(i);
                if (!TokenUtils.hasTokenData(is)) {
                    continue;
                } else if (!TokenUtils.isValidTokenItem(is)) {
                    inventory.clear(i);
                    Logger.debug(String.format("Removed corrupted token from %s's inventory", p.getDisplayName()));
                    continue;
                }

                String fullId = TokenUtils.createFullId(TokenUtils.getTokenID(is),
                        TokenUtils.getTokenIndex(is));
                if (!fullId.equals(tokenModel.getFullId()))
                    continue;

                updateTokenInInventoryCheck(tokenModel, balance, is, inventory, i);
            }
        });
    }

    private void updatePlayerEquipment(@NonNull TokenModel tokenModel,
                                       @NonNull MutableBalance balance) throws NullPointerException {
        player().ifPresent(p -> {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack is = getEquipment(slot);
                if (!TokenUtils.hasTokenData(is)) {
                    continue;
                } else if (!TokenUtils.isValidTokenItem(is)) {
                    setEquipment(slot, null);
                    Logger.debug(String.format("Removed corrupted token from %s's equipment", p.getDisplayName()));
                    continue;
                }

                String fullId = TokenUtils.createFullId(TokenUtils.getTokenID(is),
                        TokenUtils.getTokenIndex(is));
                if (!fullId.equals(tokenModel.getFullId()))
                    continue;

                updateTokenInEquipmentCheck(tokenModel, balance, is, slot);
            }
        });
    }

    private void updatePlayerCursor(@NonNull TokenModel tokenModel,
                                    @NonNull MutableBalance balance) throws NullPointerException {
        player().ifPresent(p -> {
            InventoryView view = p.getOpenInventory();
            ItemStack is = view.getCursor();
            if (!TokenUtils.hasTokenData(is)) {
                return;
            } else if (!TokenUtils.isValidTokenItem(is)) {
                view.setCursor(null);
                Logger.debug(String.format("Removed corrupted token from %s's cursor", p.getDisplayName()));
                return;
            }

            String fullId = TokenUtils.createFullId(TokenUtils.getTokenID(is),
                    TokenUtils.getTokenIndex(is));
            if (!fullId.equals(tokenModel.getFullId()))
                return;

            updateTokenInCursorCheck(tokenModel, balance, is, view);
        });
    }

    private void initPermissions() {
        if (tokenWallet == null) return;

        player().ifPresent(p -> {
            TokenManager tokenManager = MetaCity.getInstance().getTokenManager();
            Set<String> baseFullIds = new HashSet<>();

            for (MutableBalance balance : tokenWallet.getBalances()) {
                String fullId = TokenUtils.createFullId(balance.id(), balance.index());
                if (balance.balance() == 0 || !tokenManager.hasToken(fullId))
                    continue;

                String baseFullId = TokenUtils.normalizeFullId(fullId);
                if (!baseFullId.equals(fullId)) // Collects the ids for non-fungible base models
                    baseFullIds.add(baseFullId);

                initPermissions(fullId);
            }

            baseFullIds.forEach(this::initPermissions);

            setWorldAttachment(p.getWorld().getName());
        });
    }

    private void initPermissions(String fullId) {
        // Checks if the token has assigned permissions
        player().ifPresent(p -> {
            Map<String, Set<String>> worldPerms = MetaCity.getInstance().getTokenManager()
                    .getTokenPermissions()
                    .getTokenPermissions(fullId);
            if (worldPerms == null)
                return;

            // Assigns global and world permissions
            worldPerms.forEach((world, perms) -> {
                if (world.equals(TokenManager.GLOBAL))
                    globalAttachment.addPermissions(perms);
                else
                    worldPermissionMap.computeIfAbsent(world, k -> new HashSet<>()).addAll(perms);
            });
        });
    }

    void setWorldAttachment(String world) {
        worldAttachment.clear();

        Set<String> perms = worldPermissionMap.get(world);
        if (perms != null)
            worldAttachment.addPermissions(perms);
    }

    /**
     * Adds permissions to the token
     *
     * @param tokenModel The token model to add the permissions to
     */
    public void addTokenPermissions(TokenModel tokenModel) {
        if (tokenWallet == null
                || tokenModel == null
                || tokenModel.isMarkedForDeletion())
            return;

        TokenManager tokenManager = MetaCity.getInstance().getTokenManager();

        MutableBalance balance = tokenWallet.getBalance(tokenModel.getFullId());
        if (balance == null
                || balance.balance() == 0
                || !tokenManager.hasToken(tokenModel.getFullId()))
            return;

        tokenModel.getPermissionsMap()
                .forEach((world, perms) -> perms.forEach(perm -> addTokenPermission(tokenModel, perm, world)));

        // Adds the permissions from the base model if necessary
        boolean applyBasePermissions = tokenModel.isNonFungibleInstance()
                && !hasNonfungibleInstance(tokenModel.getId(), Collections.singleton(tokenModel.getIndex()));
        if (applyBasePermissions) {
            TokenModel baseModel = tokenManager.getToken(tokenModel.getId());
            if (baseModel != null) {
                baseModel.getPermissionsMap()
                        .forEach((world, perms) -> perms.forEach(perm -> addTokenPermission(baseModel, perm, world)));
            }
        }
    }

    /**
     * Add a permission to a token in a specific world
     *
     * @param perm  The permission to add
     * @param id    The id of the token
     * @param world The world to add the token permission for
     */
    public void addPermission(String perm, String id, String world) {
        if (tokenWallet == null) return;

        TokenModel tokenModel = MetaCity.getInstance().getTokenManager().getToken(id);
        if (tokenModel != null
                && tokenModel.isNonfungible()
                && tokenModel.isBaseModel()
                && hasNonfungibleInstance(id)) { // Checks if base model of non-fungible and if permission should be added
            addTokenPermission(tokenModel, perm, world);
        } else if (tokenModel != null) {
            MutableBalance balance = tokenWallet.getBalance(tokenModel.getFullId());
            if (balance != null && balance.balance() > 0)
                addTokenPermission(tokenModel, perm, world);
        }
    }

    private void addTokenPermission(TokenModel tokenModel, String perm, String world) {
        if (world.equals(TokenManager.GLOBAL)) {
            addGlobalPermission(perm, tokenModel.getFullId());
            // Tries to remove any world permission, since global
            worldPermissionMap.keySet().forEach(nonGlobal -> removeWorldPermission(perm, nonGlobal));
        } else {
            addWorldPermission(perm, tokenModel.getFullId(), world);
            // Tries to remove any global permission, since local to world
            removeGlobalPermission(perm);
        }
    }

    private void addGlobalPermission(String perm, String fullId) {
        Map<String, Set<String>> worldPerms = MetaCity.getInstance().getTokenManager()
                .getTokenPermissions()
                .getPermissionTokens(TokenManager.GLOBAL);
        if (worldPerms == null)
            return;

        // Gets the tokens with the given permission from the permission graph
        Set<String> permTokens = worldPerms.get(perm);
        if (permTokens == null)
            return;

        // Checks if the player needs to be given the permission
        if (!globalAttachment.hasPermission(perm) && permTokens.contains(fullId))
            globalAttachment.setPermission(perm);
    }

    private void addWorldPermission(String perm, String fullId, String world) {
        player().ifPresent(p -> {
            Map<String, Set<String>> worldPerms = MetaCity.getInstance().getTokenManager()
                    .getTokenPermissions()
                    .getPermissionTokens(world);

            // Gets the tokens with the given permission from the permission graph
            Set<String> permTokens = worldPerms.get(perm);
            if (permTokens == null)
                return;

            // Checks if the player needs to be given the permission
            Set<String> perms = worldPermissionMap.computeIfAbsent(world, k -> new HashSet<>());
            if (!perms.contains(perm) && permTokens.contains(fullId)) {
                perms.add(perm);

                String currentWorld = p.getWorld().getName();
                if (currentWorld.equals(world))
                    worldAttachment.setPermission(perm);
            }
        });
    }

    /**
     * Remove permissions from the token
     *
     * @param tokenModel The token model to remove the permissions from
     */
    public void removeTokenPermissions(TokenModel tokenModel) {
        if (tokenWallet == null || tokenModel == null) {
            return;
        } else if (!tokenModel.isMarkedForDeletion()) {
            MutableBalance balance = tokenWallet.getBalance(tokenModel.getFullId());
            if (balance != null && balance.balance() > 0)
                return;
        }

        tokenModel.getPermissionsMap()
                .forEach((world, perms) -> perms.forEach(perm -> removePermission(perm, world)));

        // Removes the permissions from the base model if necessary
        boolean removeBasePermissions = tokenModel.isNonFungibleInstance()
                && !hasNonfungibleInstance(tokenModel.getId(), Collections.singleton(tokenModel.getIndex()));
        if (removeBasePermissions) {
            TokenModel baseModel = MetaCity.getInstance().getTokenManager().getToken(tokenModel.getId());
            if (baseModel != null) {
                baseModel.getPermissionsMap()
                        .forEach((world, perms) -> perms.forEach(perm -> removePermission(perm, world)));
            }
        }
    }

    /**
     * Remove permission from the world
     *
     * @param perm  The permission to remove
     * @param world The world to remove the permission from
     */
    public void removePermission(String perm, String world) {
        if (tokenWallet == null)
            return;

        if (world.equals(TokenManager.GLOBAL)) {
            removeGlobalPermission(perm);
            // Tries to remove any world permission too
            worldPermissionMap.keySet().forEach(nonGlobal -> removeWorldPermission(perm, nonGlobal));
        } else {
            removeWorldPermission(perm, world);
        }
    }

    private void removeGlobalPermission(String perm) {
        Map<String, Set<String>> worldPerms = MetaCity.getInstance().getTokenManager()
                .getTokenPermissions()
                .getPermissionTokens(TokenManager.GLOBAL);
        if (worldPerms == null)
            return;

        // Gets the tokens with the given permission from the permission graph
        Set<String> permTokens = worldPerms.get(perm);
        if (permTokens == null)
            return;

        Set<String> intersect = retainPermissionTokens(permTokens);

        // Checks if the permission needs to be removed from the player
        if (globalAttachment.hasPermission(perm) && intersect.size() <= 0)
            globalAttachment.unsetPermission(perm);
    }

    private void removeWorldPermission(String perm, String world) {
        player().ifPresent(p -> {
            Map<String, Set<String>> worldPerms = MetaCity.getInstance().getTokenManager()
                    .getTokenPermissions()
                    .getPermissionTokens(world);
            if (worldPerms == null)
                return;

            // Gets the tokens with the given permission from the permission graph
            Set<String> permTokens = worldPerms.get(perm);
            if (permTokens == null)
                return;

            Set<String> intersect = retainPermissionTokens(permTokens);

            // Checks if the permission needs to be removed from the player
            Set<String> perms = worldPermissionMap.computeIfAbsent(world, k -> new HashSet<>());
            if (perms.contains(perm) && intersect.size() <= 0) {
                perms.remove(perm);

                String currentWorld = p.getWorld().getName();
                if (currentWorld.equals(world))
                    worldAttachment.unsetPermission(perm);
            }
        });
    }

    private Set<String> retainPermissionTokens(Set<String> permTokens) {
        Set<String> intersect = new HashSet<>();

        TokenManager tokenManager = MetaCity.getInstance().getTokenManager();

        // Collects the full ids of all tokens that the player owns
        for (Map.Entry<String, MutableBalance> entry : tokenWallet.getBalancesMap().entrySet()) {
            String fullId = entry.getKey();
            MutableBalance balance = entry.getValue();
            if (balance.balance() > 0 && tokenManager.hasToken(fullId)) {
                intersect.add(fullId);

                String baseFullId = TokenUtils.normalizeFullId(fullId);
                if (!baseFullId.equals(fullId)) // Collects the ids for non-fungible base models
                    intersect.add(baseFullId);
            }
        }

        // Retains only the player's tokens with the permission
        intersect.retainAll(permTokens);

        return intersect;
    }

    /**
     * Reload the identity data for the player
     */
    public void reloadIdentity() {
        MetaCity.getInstance().chain().updateIdentity(this, identity ->
                setIdentity(identity, () -> board().update())
        );
    }

    /**
     * Unlink the player from the block chain
     */
    public void unlink() {
        if (!isLinked()) return;

        MetaCity.getInstance().chain().unlink(this, identity ->
            setIdentity(null, () -> board().update())
        );
    }

    /**
     * Called once the player is unlinked from the server successfully
     */
    public void unlinked() {
        if (!isLinked()) return;

        player().ifPresent(p -> {
            Translation.COMMAND_UNLINK_SUCCESS.send(p);
            Translation.HINT_LINK.send(p);

            Bukkit.getScheduler().runTask(MetaCity.getInstance(), this::removeTokenizedItems);
        });

        reloadIdentity();
    }

    /**
     * Remove tokenized items from the players inventory
     */
    public void removeTokenizedItems() {
        player().ifPresent(p -> {
            Inventory inventory = p.getInventory();
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack is = inventory.getItem(i);
                if (TokenUtils.hasTokenData(is))
                    inventory.setItem(i, null);
            }
        });
    }

    /**
     * Remove the QR map from the players inventory, if the player has it
     */
    public void removeQrMap() {
        player().ifPresent(p -> {
            InventoryView view = p.getOpenInventory();
            PlayerInventory inventory = p.getInventory();

            Inventory top = view.getTopInventory();
            Inventory bottom = view.getBottomInventory();
            int size = top.getSize()
                    + bottom.getSize()
                    - inventory.getExtraContents().length
                    - inventory.getArmorContents().length;
            for (int i = 0; i < size; i++) {
                if (QrUtils.hasQrTag(view.getItem(i)))
                    view.setItem(i, null);
            }

            if (QrUtils.hasQrTag(view.getCursor()))
                view.setCursor(null);
            if (QrUtils.hasQrTag(inventory.getItemInOffHand()))
                inventory.setItemInOffHand(null);
        });
    }

    /**
     * Add link permissions
     */
    public void addLinkPermissions() {
        new ArrayList<>(MetaConfig.LINK_PERMISSIONS)
                .forEach(globalAttachment::setPermission);
    }

    /**
     * Remove link permissions
     */
    public void removeLinkPermissions() {
        new ArrayList<>(MetaConfig.LINK_PERMISSIONS)
                .forEach(globalAttachment::unsetPermission);
    }

    /**
     * Check if the user has been loaded
     *
     * @return If the user has been loaded
     */
    public boolean isUserLoaded() {
        return userLoaded;
    }

    /**
     * Check if the users identity has been loaded
     *
     * @return If the users identity has been loaded
     */
    public boolean isIdentityLoaded() {
        return identityLoaded;
    }

    /**
     * Check if the user has been loaded and their identity has been loaded
     *
     * @return If the user has been loaded their identity has been loaded
     */
    public boolean isLoaded() {
        return isUserLoaded() && isIdentityLoaded();
    }

    /**
     * Check if the users identity has been loaded and the wallet has been correctly loaded
     *
     * @return If the users identity has been loaded had the wallet has been correctly loaded
     */
    public boolean isLinked() {
        return isIdentityLoaded() && wallet != null && !wallet.getEthAddress().isEmpty();
    }

    /**
     * Check if the user has a non-fungible token in their balance
     *
     * @param id The id of te non-fungible token
     * @return If the user has a non-fungible token in their balance
     * @throws IllegalArgumentException If no token exists matching the id or the id given is not non-fungible or not a base model
     */
    public boolean hasNonfungibleInstance(@NonNull String id) throws IllegalArgumentException {
        return hasNonfungibleInstance(id, null);
    }

    /**
     * Check if the user has a non-fungible token in their balance
     *
     * @param id The id of te non-fungible token
     * @return If the user has a non-fungible token in their balance, ignoring mutable balance indexes
     * @throws IllegalArgumentException If no token exists matching the id or the id given is not non-fungible or not a base model
     */
    public boolean hasNonfungibleInstance(@NonNull String id,
                                          Collection<String> ignoredIndices) throws IllegalArgumentException {
        TokenModel baseModel = MetaCity.getInstance().getTokenManager().getToken(id);
        if (baseModel == null)
            throw new IllegalArgumentException(String.format("Token of id \"%s\" is not registered in token manager", id));
        else if (!baseModel.isNonfungible() || !baseModel.isBaseModel())
            throw new IllegalArgumentException(String.format("Token of id \"%s\" is not a base model of a non-fungible token", id));
        else if (tokenWallet == null)
            return false;

        Set<String> indices = ignoredIndices == null
                ? new HashSet<>()
                : new HashSet<>(ignoredIndices);

        TokenManager tokenManager = MetaCity.getInstance().getTokenManager();

        List<MutableBalance> balances = tokenWallet.getBalances();
        for (MutableBalance balance : balances) {
            if (balance.balance() > 0
                    && balance.id().equals(baseModel.getId())
                    && !indices.contains(balance.index())
                    && tokenManager.hasToken(TokenUtils.createFullId(balance.id(), balance.index())))
                return true;
        }

        return false;
    }

//    protected void cleanUp() {
//        PlayerInitializationTask.cleanUp(uuid);
//
//        MetaCity.getInstance().chain().stopNotificationService(this);
//
//        PlayerChangedWorldEvent.getHandlerList().unregister(listener);
//        player().ifPresent(p -> uberBoard.scoreboard().remove(p));
//    }

    /**
     * Get a list of players that this player has sent trade invites to
     *
     * @return A list of players that this player has sent trade invites to
     */
    public List<MetaPlayer> getSentTradeInvites() {
        return sentTradeInvites;
    }

    /**
     * Get a list of players that this player has received trade invites from
     *
     * @return A list of players that this player has received trade invites from
     */
    public List<MetaPlayer> getReceivedTradeInvites() {
        return receivedTradeInvites;
    }

    /**
     * Get the active trading view
     *
     * @return The active trading view
     */
    public TradeView getActiveTradeView() {
        return activeTradeView;
    }

    /**
     * Set the active trading view
     *
     * @param activeTradeView The trading view to change to
     */
    public void setActiveTradeView(TradeView activeTradeView) {
        this.activeTradeView = activeTradeView;
    }

    /**
     * Get the active wallet view
     *
     * @return The active wallet view
     */
    public TokenWalletView getActiveWalletView() {
        return activeWalletView;
    }

    /**
     * Set the active wallet view
     *
     * @param activeWalletView The wallet view to set
     */
    public void setActiveWalletView(TokenWalletView activeWalletView) {
        this.activeWalletView = activeWalletView;
    }

    /**
     * Get this users user id
     *
     * @return This users user id
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * Get this users identity id
     *
     * @return This users identity id
     */
    public Integer getIdentityId() {
        return identityId;
    }

    /**
     * Get this users wallet address
     *
     * @return This users wallet address
     */
    public String getEthereumAddress() {
        return wallet == null
                ? ""
                : wallet.getEthAddress();
    }

    /**
     * Get the code required to link this player, something like 'ABC123'
     *
     * @return The code required to link this player
     */
    public String getLinkingCode() {
        return linkingCode;
    }

    protected void setLinkingCodeQr(Image linkingCodeQr) {
        synchronized (linkingCodeQrLock) {
            this.linkingCodeQr = linkingCodeQr;
        }
    }

    /**
     * Get the linking code qr image, required for linking players to their wallet
     *
     * @return The qr image
     */
    public Image getLinkingCodeQr() {
        synchronized (linkingCodeQrLock) {
            return linkingCodeQr;
        }
    }

    /**
     * Get this players Enjin coin balance (ENJ)
     *
     * @return This players Enjin coin balance
     */
    public BigDecimal getEnjBalance() {
        return wallet == null
                ? BigDecimal.ZERO
                : wallet.getEnjBalance();
    }

    /**
     * Get this players Ethereum balance (ETH)
     *
     * @return This players Ethereum balance
     */
    public BigDecimal getEthBalance() {
        return wallet == null
                ? BigDecimal.ZERO
                : wallet.getEthBalance();
    }

    /**
     * Get this players Enjin coin allowance, i.e how much they can spend
     *
     * @return Get this players Enjin coin allowance, i.e how much they can spend
     */
    public BigDecimal getEnjAllowance() {
        return wallet == null
                ? BigDecimal.ZERO
                : wallet.getEnjAllowance();
    }

    /**
     * Check if this player has an allowance set
     *
     * @return If this player has an allowance set
     */
    public boolean hasAllowance() {
        return wallet != null && wallet.getEnjAllowance().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Check if this player has any Ethereum (ETH)
     *
     * @return If this player has any Ethereum (ETH)
     */
    public boolean hasEth() {
        return wallet != null && wallet.getEthBalance().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Check if this player can send any tokens
     * The player must have an allowance and some ETH
     *
     * @return If this player can send any tokens
     */
    public boolean canSend() {
        return hasAllowance() && hasEth();
    }

    /**
     * Get this players token wallet
     *
     * @return This players token wallet
     */
    public TokenWallet getTokenWallet() {
        return tokenWallet;
    }
}
