package org.metacity.metacity.wallet;

import com.enjin.minecraft_commons.spigot.ui.AbstractMenu;
import com.enjin.minecraft_commons.spigot.ui.Component;
import com.enjin.minecraft_commons.spigot.ui.Dimension;
import com.enjin.minecraft_commons.spigot.ui.Position;
import com.enjin.minecraft_commons.spigot.ui.menu.ChestMenu;
import com.enjin.minecraft_commons.spigot.ui.menu.component.SimpleMenuComponent;
import com.enjin.minecraft_commons.spigot.ui.menu.component.pagination.SimplePagedComponent;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.token.MetaTokenView;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.token.TokenManager;
import org.metacity.metacity.token.TokenModel;
import org.metacity.metacity.util.TokenUtils;
import org.metacity.metacity.util.UiUtils;
import org.metacity.metacity.util.server.Translation;

import java.util.List;
import java.util.Map;

public class TokenWalletView extends ChestMenu implements MetaTokenView {

    public static final String WALLET_VIEW_NAME = "MetaCity Wallet";
    public static final int WIDTH = 9;
    public static final Dimension INVENTORY_DIMENSION = new Dimension(WIDTH, 4);

    private final MetaCity plugin;
    private final MetaPlayer owner;
    private final SimpleMenuComponent navigationComponent;
    private final SimpleMenuComponent inventoryViewComponent;
    private final SimplePagedComponent pagedFungibleComponent;
    private final SimplePagedComponent pagedNonFungibleComponent;
    private ItemStack nextComponentItem;

    // State Fields
    private int currentFungiblePage = 0;
    private int currentNonFungiblePage = 0;
    protected SimplePagedComponent currentPagedComponent;

    public TokenWalletView(MetaPlayer owner) {
        super(ChatColor.DARK_PURPLE + WALLET_VIEW_NAME, 6);
        this.plugin = MetaCity.getInstance();
        this.owner = owner;
        this.navigationComponent = new SimpleMenuComponent(new Dimension(WIDTH, 1));
        this.inventoryViewComponent = new SimpleMenuComponent(INVENTORY_DIMENSION);
        this.pagedFungibleComponent = new SimplePagedComponent(INVENTORY_DIMENSION);
        this.pagedNonFungibleComponent = new SimplePagedComponent(INVENTORY_DIMENSION);
        init();
    }

    @Override
    public void validateInventory() {
        owner.player().ifPresent(this::repopulate);
    }

    @Override
    public void updateInventory() {
        owner.player().ifPresent(this::repopulate);
    }

    private void init() {
        owner.setActiveWalletView(this);
        currentPagedComponent = pagedFungibleComponent;
        setCloseConsumer(this::closeMenuAction);

        Position pageBackPosition = Position.of(0, 0);
        navigationComponent.setItem(pageBackPosition, createPageBackItemStack());
        navigationComponent.addAction(pageBackPosition, p -> {
            if (getCurrentPage() < 0) {
                setCurrentPage(0);
                return;
            } else if (getCurrentPage() == 0) {
                return;
            }

            setCurrentPage(getCurrentPage() - 1);
            drawInventory();
            refresh(p);
        }, ClickType.LEFT, ClickType.RIGHT);

        Position pageNextPosition = Position.of(WIDTH - 1, 0);
        navigationComponent.setItem(pageNextPosition, createPageNextItemStack());
        navigationComponent.addAction(pageNextPosition, p -> {
            if (getCurrentPage() >= currentPagedComponent.getPageCount() - 1)
                return;

            setCurrentPage(getCurrentPage() + 1);
            drawInventory();
            refresh(p);
        }, ClickType.LEFT, ClickType.RIGHT);

        Position nextItemPagesPosition = Position.of(WIDTH / 2, 0);
        nextComponentItem = createNextComponentItemStack(getPagedComponentName(getNextPagedComponent()));
        navigationComponent.setItem(nextItemPagesPosition, nextComponentItem);
        navigationComponent.addAction(nextItemPagesPosition, p -> {
            currentPagedComponent = getNextPagedComponent();

            ItemMeta meta = nextComponentItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + getPagedComponentName(getNextPagedComponent()));
                nextComponentItem.setItemMeta(meta);
            }

            setCurrentPage(getCurrentPage());
            drawInventory();
            refresh(p);
        }, ClickType.LEFT, ClickType.RIGHT);

        // Creates the navigation separator
        Component separator = UiUtils.createSeparator(new Dimension(WIDTH, 1));

        addComponent(Position.of(0, 5), navigationComponent);
        addComponent(Position.of(0, 4), separator);

        populate();
    }

    private void populate() {
        populate(pagedFungibleComponent, false);
        populate(pagedNonFungibleComponent, true);

        drawInventory();
    }

    private void populate(SimplePagedComponent component, boolean isNonfungible) {
        List<MutableBalance> balances = owner.getTokenWallet().getBalances();

        component.clear();

        int index = 0;
        for (MutableBalance balance : balances) {
            String fullId = TokenUtils.createFullId(balance.id(), balance.index());
            TokenModel tokenModel = plugin.getTokenManager().getToken(fullId);
            boolean inDB = tokenModel != null;

            if (isNonfungible && tokenModel == null) {
                tokenModel = plugin.getTokenManager().getToken(balance.id());
                inDB = false;
            }

            if (tokenModel == null
                    || !tokenModel.isLoaded()
                    || tokenModel.getWalletViewState() == TokenWalletViewState.HIDDEN
                    || tokenModel.isNonfungible() != isNonfungible
                    || balance.amountAvailableForWithdrawal() == 0) {
                continue;
            }

            int page = index / INVENTORY_DIMENSION.getArea();
            int x = index % INVENTORY_DIMENSION.getWidth();
            int y = index % INVENTORY_DIMENSION.getArea() / INVENTORY_DIMENSION.getWidth();
            Position position = Position.of(x, y);

            ItemStack is = tokenModel.getWalletViewItemStack();
            if (is == null)
                continue;

            if (!inDB) {
                NBTItem nbt = new NBTItem(is);
                nbt.setString(TokenModel.NBT_INDEX, balance.index());
                nbt.applyNBT(is);
            }

            is.setAmount(balance.amountAvailableForWithdrawal());
            component.setItem(page, position, is);

            index++;
        }

        if (component == currentPagedComponent) {
            // Puts the view at the nearest empty page if multiple pages were lost when repopulating
            if (getCurrentPage() > component.getPageCount())
                setCurrentPage(component.getPageCount());
        } else {
            setPage(component, 0);
        }
    }

    protected void drawInventory() {
        inventoryViewComponent.removeAllActions();

        TokenManager tokenManager = plugin.getTokenManager();

        int currentPage = getCurrentPage();
        for (int y = 0; y < INVENTORY_DIMENSION.getHeight(); y++) {
            for (int x = 0; x < INVENTORY_DIMENSION.getWidth(); x++) {
                inventoryViewComponent.removeItem(x, y);

                ItemStack is = currentPagedComponent.getItem(currentPage, x, y);
                if (!TokenUtils.isValidTokenItem(is))
                    continue;

                String fullId = TokenUtils.createFullId(TokenUtils.getTokenID(is),
                        TokenUtils.getTokenIndex(is));
                TokenModel tokenModel = tokenManager.getToken(fullId);

                if (tokenModel == null && TokenUtils.isNonFungible(is)) {
                    tokenModel = tokenManager.getToken(TokenUtils.getTokenID(is));
                }

                if (tokenModel == null) {
                    continue;
                }

                MutableBalance balance = owner.getTokenWallet().getBalance(fullId);
                if (tokenModel.getWalletViewState() != TokenWalletViewState.HIDDEN) {
                    inventoryViewComponent.setItem(x, y, is);

                    is = tokenModel.getItemStack(is.getAmount());
                    NBTItem nbt = new NBTItem(is);
                    if (tokenModel.isNonfungible())
                        nbt.setString(TokenModel.NBT_INDEX, balance.index());
                    nbt.applyNBT(is);

                    if (tokenModel.getWalletViewState() == TokenWalletViewState.WITHDRAWABLE)
                        addWithdrawAction(Position.of(x, y), balance, is);
                }
            }
        }

        addComponent(Position.of(0, 0), inventoryViewComponent);
    }

    protected void addWithdrawAction(Position position, MutableBalance balance, ItemStack is) {
        // Withdraws one token
        inventoryViewComponent.addAction(position, player -> withdraw(player, balance, is, 1), ClickType.LEFT);

        // Withdraws a split stack
        inventoryViewComponent.addAction(position, player -> {
            int amount = (int) Math.ceil(Math.min(is.getAmount(), is.getMaxStackSize()) / 2.0);
            withdraw(player, balance, is, amount);
        }, ClickType.RIGHT);

        // Withdraws a full stack
        inventoryViewComponent.addAction(position, player -> {
            int amount = Math.min(is.getAmount(), is.getMaxStackSize());
            withdraw(player, balance, is, amount);
        }, ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT);
    }

    private void withdraw(Player player, MutableBalance balance, ItemStack is, int amount) {
        if (amount == 0)
            return;

        PlayerInventory inventory = player.getInventory();
        if (balance.amountAvailableForWithdrawal() >= amount) {
            boolean changed = false;

            ItemStack clone = is.clone();
            clone.setAmount(amount);

            Map<Integer, ItemStack> leftOver = inventory.addItem(clone);
            if (leftOver.size() == 0) {
                balance.withdraw(amount);
                changed = true;
            } else if (leftOver.size() == 1) {
                for (Map.Entry<Integer, ItemStack> entry : leftOver.entrySet()) {
                    ItemStack item = entry.getValue();

                    if (item.getAmount() != amount) {
                        balance.withdraw(amount - item.getAmount());
                        changed = true;
                    }
                }
            }

            if (changed)
                repopulate(player);
        }
    }

    public void repopulate(Player player) {
        for (int y = 0; y < INVENTORY_DIMENSION.getHeight(); y++) {
            for (int x = 0; x < INVENTORY_DIMENSION.getWidth(); x++) {
                inventoryViewComponent.removeItem(x, y);
            }
        }

        populate();

        refresh(player);
    }

    public static boolean isViewingWallet(Player player) {
        if (player != null) {
            InventoryView view = player.getOpenInventory();
            return ChatColor.stripColor(view.getTitle()).equalsIgnoreCase(TokenWalletView.WALLET_VIEW_NAME);
        }

        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTokenDeposit(InventoryClickEvent event) {
        owner.player().ifPresent(p -> {
            if (p != event.getWhoClicked()) return;

            if (event.getClickedInventory() instanceof PlayerInventory) {
                ItemStack is = event.getCurrentItem();
                String id = TokenUtils.getTokenID(is);
                String index = TokenUtils.getTokenIndex(is);
                if (id == null || id.isEmpty() || index == null || index.isEmpty()) return;

                MetaPlayer metaPlayer = plugin.getPlayerManager()
                        .getPlayer((Player) event.getWhoClicked())
                        .orElse(null);
                if (metaPlayer == null)
                    return;

                int amount;
                switch (event.getClick()) {
                    case LEFT: // Deposits one token
                        amount = 1;
                        break;
                    case RIGHT: // Deposits a split stack
                        amount = (int) Math.ceil(Math.min(is.getAmount(), is.getMaxStackSize()) / 2.0);
                        break;
                    case SHIFT_LEFT: // Deposits the entire stack
                    case SHIFT_RIGHT:
                        amount = is.getAmount();
                        break;
                    default:
                        return;
                }

                MutableBalance balance = metaPlayer.getTokenWallet().getBalance(id, index);
                balance.deposit(amount);
                is.setAmount(is.getAmount() - amount);
                metaPlayer.player().ifPresent(pl -> {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> repopulate(pl));
                });
            }
        });
    }

    @Override
    protected void onClose(Player player) {
        HandlerList.unregisterAll(this);
        super.onClose(player);
    }

    private void closeMenuAction(Player player, AbstractMenu menu) {
        owner.player().ifPresent(p -> {
            if (player != p || this != owner.getActiveWalletView())
                return;

            owner.setActiveWalletView(null);
        });

    }

    protected ItemStack createPageBackItemStack() {
        ItemStack is = new ItemStack(Material.HOPPER);
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "<--");
            is.setItemMeta(meta);
        }

        return is;
    }

    protected ItemStack createPageNextItemStack() {
        ItemStack is = new ItemStack(Material.HOPPER);
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "-->");
            is.setItemMeta(meta);
        }

        return is;
    }

    protected ItemStack createNextComponentItemStack(String nextComponentName) {
        ItemStack is = new ItemStack(Material.HOPPER);
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + nextComponentName);
            is.setItemMeta(meta);
        }

        return is;
    }

    protected int getPage(SimplePagedComponent component) {
        if (component == pagedFungibleComponent)
            return currentFungiblePage;
        else if (component == pagedNonFungibleComponent)
            return currentNonFungiblePage;

        return -1;
    }

    protected int getCurrentPage() throws IllegalStateException {
        int page = getPage(currentPagedComponent);
        if (page >= 0)
            return page;

        throw new IllegalStateException("No set paged component");
    }

    protected boolean setPage(SimplePagedComponent component, int page) {
        if (component == pagedFungibleComponent)
            currentFungiblePage = page;
        else if (component == pagedNonFungibleComponent)
            currentNonFungiblePage = page;
        else
            return false;

        return true;
    }

    protected void setCurrentPage(int page) throws IllegalStateException {
        if (!setPage(currentPagedComponent, page))
            throw new IllegalStateException("No set paged component");
    }

    protected SimplePagedComponent getNextPagedComponent() throws IllegalStateException {
        if (currentPagedComponent == pagedFungibleComponent)
            return pagedNonFungibleComponent;
        else if (currentPagedComponent == pagedNonFungibleComponent)
            return pagedFungibleComponent;
        else
            throw new IllegalStateException("No set paged component");
    }

    protected String getPagedComponentName(SimplePagedComponent component) {
        if (component == pagedFungibleComponent)
            return Translation.WALLET_UI_FUNGIBLE.translation();
        else if (component == pagedNonFungibleComponent)
            return Translation.WALLET_UI_NONFUNGIBLE.translation();

        return null;
    }

}
