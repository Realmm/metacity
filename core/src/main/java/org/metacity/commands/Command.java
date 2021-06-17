package org.metacity.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.util.Consumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class Command<T extends CommandSender> extends CommandNode<T> {

    private final List<SubCommand<? extends CommandSender>> subCommands = new ArrayList<>();
    private BiConsumer<T, CommandWrapper<T>> failExecution;

    public Command(Class<T> sender, String name) {
        super(sender, name);
    }

    public void setAliases(String... s) {
        bukkitCommand.setAliases(Arrays.asList(s));
    }

    public void addSubCommands(SubCommand<? extends CommandSender>... subCommands) {
        Arrays.asList(subCommands).forEach(this::addSubCommand);
    }

    public void addSubCommand(SubCommand<? extends CommandSender> subCommand) {
        subCommands.add(subCommand);
    }

    public <E extends CommandSender> void addSubCommand(Class<E> allowed, SubBuilder<E> builder) {
        subCommands.add(builder.build(new SubCommandBuilder<>(allowed)));
    }

    public static <T extends CommandSender> CommandBuilder<T> builder(Class<T> sender, String command) {
        return new CommandBuilder<>(sender, command);
    }

    public void setFailExecution(BiConsumer<T, CommandWrapper<T>> execution) {
        this.failExecution = execution;
    }

    BiConsumer<T, CommandWrapper<T>> failExecution() {
        return failExecution;
    }

    List<SubCommand<CommandSender>> subCommands() {
        List<SubCommand<CommandSender>> list = new ArrayList<>();
        this.subCommands.forEach(s -> list.add((SubCommand<CommandSender>) s));
        return list;
    }

}
