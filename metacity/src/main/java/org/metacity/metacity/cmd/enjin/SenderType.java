package org.metacity.metacity.cmd.enjin;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;

public enum SenderType {

    BLOCK(BlockCommandSender.class),
    CONSOLE(ConsoleCommandSender.class),
    REMOTE_CONSOLE(RemoteConsoleCommandSender.class),
    PLAYER(Player.class),
    ANY(CommandSender.class);

    private final Class<? extends CommandSender> instanceSuperClass;

    SenderType(Class<? extends CommandSender> instanceSuperClass) {
        this.instanceSuperClass = instanceSuperClass;
    }

    public static SenderType type(CommandSender sender) {
        for (SenderType type : values()) {
            if (type.instanceSuperClass.isInstance(sender))
                return type;
        }
        return ANY;
    }

}
