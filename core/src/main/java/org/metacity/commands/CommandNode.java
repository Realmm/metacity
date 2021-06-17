package org.metacity.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.metacity.core.Core;
import org.metacity.util.CC;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class CommandNode<T extends CommandSender> {

    private final String command;

    private final List<String> permissions = new ArrayList<>();
    private final List<Condition<T>> conditions = new ArrayList<>();
    private BiConsumer<T, CommandWrapper<T>> execution;
    private final Class<T> allowedSender;
    protected final BukkitCommand bukkitCommand;

    private final static List<BukkitCommand> bukkitCommands = new ArrayList<>();

    CommandNode(Class<T> sender, String command) {
        this.command = command;
        this.allowedSender = sender;
        this.bukkitCommand = new BukkitCommand(command) {
            @Override
            public boolean execute(@Nonnull CommandSender sender, @Nonnull String s, @Nonnull String[] args) {
                return onCommand(sender, this, s, args);
            }
        };
    }

    CommandNode(Class<T> sender) {
        this.command = "";
        this.allowedSender = sender;
        this.bukkitCommand = null;
    }

    public void addPermission(String s) {
        permissions.add(s.toLowerCase());
    }

    public void addCondition(Condition<T> condition) {
        conditions.add(condition);
    }

    public void setExecution(BiConsumer<T, CommandWrapper<T>> execution) {
        this.execution = execution;
    }

    Stream<Condition<T>> conditions() {
        return this.conditions.stream();
    }

    BiConsumer<T, CommandWrapper<T>> execution() {
        return execution;
    }

    Class<T> allowedSender() {
        return allowedSender;
    }

    Collection<String> perms() {
        return Collections.unmodifiableList(permissions);
    }

    boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String s, String[] args) {
        if (!(this instanceof Command<T>)) return true;
        Command<T> co = (Command<T>) this;
        if (!s.equalsIgnoreCase(command) || !cmd.getLabel().equalsIgnoreCase(command)) {
            if (co.bukkitCommand.getAliases().stream().noneMatch(a -> a.equalsIgnoreCase(s))) return true;
        }

        if (!allowedSender.isAssignableFrom(sender.getClass())) return true;

        if (!permissions.stream().allMatch(sender::hasPermission)) {
            sender.sendMessage(CC.RED + "Incorrect permissions");
            return true;
        }

        final T t = (T) sender;

        final CommandWrapper<T> commandWrapper = new CommandWrapper<>(t, cmd, s, args);
        final CommandWrapper<CommandSender> normalWrapper = new CommandWrapper<>(sender, cmd, s, args);

        if (conditions.stream().anyMatch(c -> !c.validate(t, commandWrapper))) {
            if (this instanceof Command<T> base) {
                if (base.failExecution() != null) base.failExecution().accept(t, commandWrapper);
            }
            return true;
        }

        Predicate<SubCommand<CommandSender>> pred = sub ->
                sub.execution() != null &&
                        sub.conditions().filter(c ->
                                sub.allowedSender().isAssignableFrom(sender.getClass()))
                                .anyMatch(c -> c.validate(sender, normalWrapper));

        if (co.subCommands().stream().filter(pred).count() > 1) {
            throw new IllegalStateException("Unable to parse SubCommand, " +
                    "duplicate valid subcommands for input, found \'" + co.subCommands().stream().filter(pred).count() +
                    "\' valid sub commands, should be 1");
        }
        SubCommand<CommandSender> sub = co.subCommands().stream().filter(pred).findFirst().orElse(null);
        if (sub == null) {
            if (execution == null) return true;
            execution.accept(t, commandWrapper);
            return true;
        }

        if (!sub.allowedSender().isAssignableFrom(sender.getClass())) return true;
        if (!sub.perms().stream().allMatch(sender::hasPermission)) {
            sender.sendMessage(CC.RED + "Incorrect permissions");
            return true;
        }

        if (sub.execution() != null) sub.execution().accept(t, normalWrapper);
        return true;
    }

    public void register() {
        register(this);
    }

    public static <T extends CommandSender> void register(CommandNode<T> command) {
        if (command.command.equalsIgnoreCase("") || command.bukkitCommand == null)
            throw new IllegalArgumentException("Parsed illegal sub command, should be base");
        SimpleCommandMap map = ((CraftServer) Core.getInstance().getServer()).getCommandMap();
        Collection<org.bukkit.command.Command> commands = map.getCommands();
        if (commands.stream().anyMatch(c -> c.getName().equalsIgnoreCase(command.bukkitCommand.getName())))
            throw new IllegalStateException("Command " + command.bukkitCommand.getName() + " already registered");
        map.register(command.command, command.bukkitCommand);
        bukkitCommands.add(command.bukkitCommand);
    }

    public static void unregisterAll() {
        SimpleCommandMap map = ((CraftServer) Core.getInstance().getServer()).getCommandMap();
        Collection<org.bukkit.command.Command> mapCommands = new ArrayList<>(map.getCommands());
        map.clearCommands();
        mapCommands.removeIf(c -> bukkitCommands.stream().anyMatch(bc -> c.getName().equalsIgnoreCase(bc.getName())));
        mapCommands.forEach(c -> map.register(c.getName(), c));
    }

}
