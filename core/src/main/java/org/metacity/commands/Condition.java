package org.metacity.commands;

import org.bukkit.command.CommandSender;

/**
 * Used to determine if the command should be ran
 * Any input validation should be done here
 */
@FunctionalInterface
public interface Condition<T extends CommandSender> {

    boolean validate(T sender, CommandWrapper<T> command);

}
