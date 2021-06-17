package org.metacity.commands;

import org.bukkit.command.CommandSender;

@FunctionalInterface
public interface SubBuilder<T extends CommandSender> {

    SubCommand<T> build(SubCommandBuilder<T> builder);

}
