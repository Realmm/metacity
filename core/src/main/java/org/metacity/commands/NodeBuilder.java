package org.metacity.commands;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class NodeBuilder<T extends CommandSender> {

    private final String command;

    private final List<String> permissions = new ArrayList<>(), aliases = new ArrayList<>();
    private final Class<T> allowed;
    private final List<Condition<T>> conditions = new ArrayList<>();
    private BiConsumer<T, CommandWrapper<T>> execution;

    NodeBuilder(Class<T> allowed, String command) {
        this.command = command;
        this.allowed = allowed;
    }

    NodeBuilder(Class<T> allowed) {
        this.command = "";
        this.allowed = allowed;
    }

    public NodeBuilder<T> withExecution(BiConsumer<T, CommandWrapper<T>> execution) {
        this.execution = execution;
        return this;
    }

    public NodeBuilder<T> withPermission(String s) {
        permissions.add(s);
        return this;
    }

    public NodeBuilder<T> withPermissions(String... strings) {
        permissions.addAll(Arrays.asList(strings));
        return this;
    }

    public NodeBuilder<T> withConditions(Condition<T>... conditions) {
        this.conditions.addAll(Arrays.asList(conditions));
        return this;
    }

    public NodeBuilder<T> withAliases(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
        return this;
    }

    public CommandNode<T> build() {
        boolean sub = this.command == null;
        CommandNode<T> base = sub ? new SubCommand<>(allowed) : new Command<>(allowed, this.command);
        permissions.forEach(base::addPermission);
        conditions.forEach(base::addCondition);
        base.setExecution(execution);
        if (base instanceof Command<T> cmd) cmd.setAliases(aliases.toArray(String[]::new));
        return base;
    }

}
