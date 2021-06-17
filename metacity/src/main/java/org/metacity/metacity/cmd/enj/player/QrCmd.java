package org.metacity.metacity.cmd.enj.player;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.metacity.commands.Command;
import org.metacity.commands.SubCommand;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.util.QrUtils;
import org.metacity.metacity.util.server.Translation;
import org.metacity.util.CC;

import java.awt.*;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class QrCmd extends SubCommand<Player> {

    public QrCmd() {
        super(Player.class);
        addPermission(Permission.CMD_LINK.node());
        addCondition((p, w) -> w.validateNode(1, s -> s.equalsIgnoreCase("qr")));
        setExecution((p, w) -> {
            Optional<MetaPlayer> mo = MetaCity.getInstance().getPlayerManager().getPlayer(p);
            if (!mo.isPresent()) {
                Translation.ERRORS_PLAYERNOTREGISTERED.send(p, p.getName());
                return;
            }
            MetaPlayer m = mo.get();
            if (!m.isLoaded()) {
                Translation.IDENTITY_NOTLOADED.send(m);
                return;
            }
            if (m.isLinked()) {
                Translation.COMMAND_QR_ALREADYLINKED.send(m);
                return;
            }

            Image qr = m.getLinkingCodeQr();
            if (qr == null) {
                Translation.COMMAND_QR_CODENOTLOADED.send(m);
                return;
            }

            NBTItem nbtItem;
            ItemStack is;
            try {
                nbtItem = new NBTItem(new ItemStack(Material.FILLED_MAP));
                ItemMeta meta = nbtItem.getItem().getItemMeta();
                if (!(meta instanceof MapMeta))
                    throw new Exception("Map does not contain map metadata");

                MapView map = Bukkit.createMap(m.world());
                ImageRenderer.apply(map, qr);

                meta.setDisplayName(CC.DARK_PURPLE + Translation.QR_DISPLAYNAME.translation());
                ((MapMeta) meta).setMapView(map);
                nbtItem.getItem().setItemMeta(meta);
                nbtItem.setBoolean(QrUtils.QR_TAG, true);

                is = nbtItem.getItem().clone();
            } catch (Exception e) {
                Translation.COMMAND_QR_ERROR.send(m);
                e.printStackTrace();
                return;
            }

            m.removeQrMap();
            if (!placeQrInInventory(p, is)) Translation.COMMAND_QR_INVENTORYFULL.send(m);
        });

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

