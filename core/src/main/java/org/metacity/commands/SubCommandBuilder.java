package org.metacity.commands;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class SubCommandBuilder<T extends CommandSender> extends NodeBuilder<T> {

    SubCommandBuilder(Class<T> allowed) {
        super(allowed);
    }

    @Override
    public SubCommand<T> build() {
        return (SubCommand<T>) super.build();
    }
}
