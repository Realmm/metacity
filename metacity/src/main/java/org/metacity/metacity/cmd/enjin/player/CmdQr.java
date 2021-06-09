package org.metacity.metacity.cmd.enjin.player;

import de.tr7zw.changeme.nbtapi.NBTItem;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.metacity.metacity.cmd.enjin.CommandContext;
import org.metacity.metacity.cmd.enjin.CommandRequirements;
import org.metacity.metacity.cmd.enjin.MetaCommand;
import org.metacity.metacity.cmd.enjin.SenderType;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.util.QrUtils;
import org.metacity.metacity.util.server.Translation;

import java.awt.*;
import java.util.Map;
import java.util.Objects;

public class CmdQr extends MetaCommand {

    public CmdQr(MetaCommand parent) {
        super(parent);
        this.aliases.add("qr");
        this.requirements = CommandRequirements.builder()
                .withAllowedSenderTypes(SenderType.PLAYER)
                .withPermission(Permission.CMD_LINK)
                .build();
    }

    @Override
    public void execute(CommandContext context) {
        Player sender = Objects.requireNonNull(context.player());

        MetaPlayer senderMetaPlayer = getValidSenderEnjPlayer(context);
        if (senderMetaPlayer == null)
            return;

        Image qr = senderMetaPlayer.getLinkingCodeQr();
        if (qr == null) {
            Translation.COMMAND_QR_CODENOTLOADED.send(sender);
            return;
        }

        NBTItem nbtItem;
        ItemStack is;
        try {
            nbtItem = new NBTItem(new ItemStack(Material.FILLED_MAP));
            ItemMeta meta = nbtItem.getItem().getItemMeta();
            if (!(meta instanceof MapMeta))
                throw new Exception("Map does not contain map metadata");

            MapView map = Bukkit.createMap(sender.getWorld());
            ImageRenderer.apply(map, qr);

            meta.setDisplayName(ChatColor.DARK_PURPLE + Translation.QR_DISPLAYNAME.translation());
            ((MapMeta) meta).setMapView(map);
            nbtItem.getItem().setItemMeta(meta);
            nbtItem.setBoolean(QrUtils.QR_TAG, true);

            is = nbtItem.getItem().clone();
        } catch (Exception e) {
            Translation.COMMAND_QR_ERROR.send(sender);
            bootstrap.log(e);
            return;
        }

        senderMetaPlayer.removeQrMap();
        if (!placeQrInInventory(sender, is))
            Translation.COMMAND_QR_INVENTORYFULL.send(sender);
    }

    @Override
    protected MetaPlayer getValidSenderEnjPlayer(@NonNull CommandContext context) throws NullPointerException {
        Player sender = Objects.requireNonNull(context.player(), "Expected context to have non-null player as sender");

        MetaPlayer senderMetaPlayer = context.enjinPlayer();
        if (senderMetaPlayer == null) {
            Translation.ERRORS_PLAYERNOTREGISTERED.send(sender, sender.getName());
            return null;
        } else if (!senderMetaPlayer.isLoaded()) {
            Translation.IDENTITY_NOTLOADED.send(sender);
            return null;
        } else if (senderMetaPlayer.isLinked()) {
            Translation.COMMAND_QR_ALREADYLINKED.send(sender);
            return null;
        }

        return senderMetaPlayer;
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.COMMAND_QR_DESCRIPTION;
    }

    private boolean placeQrInInventory(Player player, ItemStack is) {
        PlayerInventory inventory = player.getInventory();

        if (inventory.getItemInOffHand().getType() == Material.AIR) {
            inventory.setItemInOffHand(is);
            return true;
        }

        Map<Integer, ItemStack> leftOver = inventory.addItem(is);

        return leftOver.size() == 0;
    }

    private static class ImageRenderer extends MapRenderer {

        private final Image image;

        private ImageRenderer() {
            throw new IllegalStateException();
        }

        public ImageRenderer(Image image) {
            this.image = image;
        }

        @Override
        public void render(MapView map, MapCanvas canvas, Player player) {
            canvas.drawImage(0, 0, MapPalette.resizeImage(image));
        }

        public static void apply(MapView map, Image image) {
            if (map == null)
                throw new NullPointerException();

            map.getRenderers().clear();
            map.addRenderer(new ImageRenderer(image));
        }

    }
}
