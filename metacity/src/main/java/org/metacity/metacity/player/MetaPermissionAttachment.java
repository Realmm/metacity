package org.metacity.metacity.player;

import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import org.metacity.metacity.MetaCity;

import java.util.Collection;

public class MetaPermissionAttachment {

    private final Permissible permissible;
    private final Plugin plugin;
    private PermissionAttachment attachment;

    public MetaPermissionAttachment(Permissible permissible) {
        this.permissible = permissible;
        this.plugin = MetaCity.getInstance();
        clear();
    }

    public boolean hasPermission(String permission) {
        return attachment.getPermissions().containsKey(permission);
    }

    public void addPermissions(Collection<String> permissions) {
        if (permissions == null)
            return;

        permissions.forEach(this::setPermission);
    }

    public void setPermission(String permission) {
        attachment.setPermission(permission, true);
    }

    public void unsetPermission(String permission) {
        attachment.unsetPermission(permission);
    }

    public void clear() {
        if (attachment != null)
            attachment.remove();

        attachment = permissible.addAttachment(plugin);
    }

}
