package org.metacity.commands;

import org.bukkit.command.CommandSender;

public class SubCommand<T extends CommandSender> extends CommandNode<T> {

    private Command<? extends CommandSender> base;

    public SubCommand(Class<T> sender) {
        super(sender);
    }

    Command<? extends CommandSender> getBase() {
        return base;
    }

    void setBase(Command<? extends CommandSender> command) {
        this.base = command;
    }

}
