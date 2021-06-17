package org.metacity.commands;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class CommandBuilder<T extends CommandSender> extends NodeBuilder<T> {

    private final Map<Class<CommandSender>, SubBuilder<CommandSender>> subCommandBuilders = new HashMap<>();
    private BiConsumer<T, CommandWrapper<T>> failExecution;

    CommandBuilder(Class<T> allowed, String command) {
        super(allowed, command);
    }

    public <E extends CommandSender> CommandBuilder<T> addSubCommand(Class<E> allowed, SubBuilder<E> builder) {
        subCommandBuilders.put((Class<CommandSender>) allowed, (SubBuilder<CommandSender>) builder);
        return this;
    }

    public CommandBuilder<T> withFailExecution(BiConsumer<T, CommandWrapper<T>> failExecution) {
        this.failExecution = failExecution;
        return this;
    }

    @Override
    public Command<T> build() {
        Command<T> command = (Command<T>) super.build();
        subCommandBuilders.entrySet().stream().map(s -> s.getValue().build(new SubCommandBuilder<>(s.getKey()))).forEach(s -> {
            s.setBase(command);
            command.addSubCommand(s);
        });
        command.setFailExecution(failExecution);

        return command;
    }
}
