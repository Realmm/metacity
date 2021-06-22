package org.metacity.metacity.token;

import com.google.gson.Gson;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.metacity.metacity.util.server.MetaConfig;
import org.metacity.util.CC;
import org.metacity.util.Util;

public enum TokenType {

    TEST_SWORD("3880000000000883",
            "123",
            3,
            Material.STONE_SWORD,
            1,
            CC.YELLOW.bold() + "Test Sword",
            "Powerful sword");

    private ItemStack stack;
    private final String metadataURI;
    private final String id, alternateId;
    private final int maxAmount;

    TokenType(String id, String alternateId, int maxAmount, Material material, int i, String name, String desc) {
        this.metadataURI = "https://" +
                (MetaConfig.DEV_MODE ? "kovan" : "jumpnet") +
                ".cloud.enjin.io/platform/" +
                MetaConfig.getAppId() + "/assets/" + id + ".json";
        this.id = id;
        this.alternateId = alternateId;
        this.maxAmount = maxAmount;
        this.stack = new ItemStack(material, 1, (short) i);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Util.wrapWithColor(CC.GRAY + desc, 40));
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        this.stack.setItemMeta(meta);
        NBTContainer container = NBTItem.convertItemtoNBT(stack);
        NBTItem item = new NBTItem(NBTItem.convertNBTtoItem(container));
        item.setString("token", name().toLowerCase().replace("_", "-"));
        this.stack = item.getItem();
    }

    public String getId() {
        return id;
    }

    public String getAlternateId() {
        return alternateId;
    }

    public String getMetadataURI() {
        return metadataURI;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public ItemStack item() {
        return stack;
    }

    public String data() {
        return CraftItemStack.asNMSCopy(stack).save(new NBTTagCompound()).toString();
    }

}

