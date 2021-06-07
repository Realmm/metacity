package org.metacity.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.entity.Player;
import org.metacity.core.Core;
import org.metacity.util.CC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.function.BiConsumer;

public class Command<T extends CommandSender> {

    private final String command;

    private final List<String> permissions = new ArrayList<>();
    private final List<Condition<T>> conditions = new ArrayList<>();
    private BiConsumer<T, CommandWrapper<T>> execution, failExecution;
    private final Class<T> allowedSender;
    private final BukkitCommand bukkitCommand;

    private final static List<BukkitCommand> bukkitCommands = new ArrayList<>();

    private Command(Class<T> sender, String command) {
        this.command = command;
        this.allowedSender = sender;
        this.bukkitCommand = new BukkitCommand(command) {
            @Override
            public boolean execute(CommandSender sender, String s, String[] args) {
                return onCommand(sender, this, s, args);
            }
        };
    }

    private Command(Class<T> sender) {
        this.command = "";
        this.allowedSender = sender;
        this.bukkitCommand = null;
    }

    public static <T extends CommandSender> CommandBuilder<T> builder(Class<T> sender, String command) {
        return new CommandBuilder<T>(sender, command);
    }

    public void addPermission(String s) {
        permissions.add(s.toLowerCase());
    }

    public void addCondition(Condition<T> condition) {
        conditions.add(condition);
    }

    public void setFailExecution(BiConsumer<T, CommandWrapper<T>> execution) {
        this.failExecution = execution;
    }

    public void setExecution(BiConsumer<T, CommandWrapper<T>> execution) {
        this.execution = execution;
    }

    private boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String s, String[] args) {
        if (!s.equalsIgnoreCase(command) || !cmd.getLabel().equalsIgnoreCase(command)) return true;

        if (!allowedSender.isAssignableFrom(sender.getClass())) return true;
        if (execution == null) return true;

        if (!permissions.stream().allMatch(sender::hasPermission)) {
            sender.sendMessage(CC.RED + "Incorrect permissions");
            return true;
        }

        T t = (T) sender;

        CommandWrapper<T> commandWrapper = new CommandWrapper<T>(t, cmd, s, args);

        if (conditions.stream().anyMatch(c -> !c.validate(t, commandWrapper))) {
            if (failExecution != null) failExecution.accept(t, commandWrapper);
            return true;
        }

        execution.accept(t, commandWrapper);
        return true;
    }

    public void register() {
        register(this);
    }

    public static <T extends CommandSender> void register(Command<T> command) {
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

    public static class CommandWrapper<T extends CommandSender> {

        private final T sender;
        private final org.bukkit.command.Command command;
        private final String label;
        private final String[] args;

        private CommandWrapper(T t, org.bukkit.command.Command cmd, String s, String[] args) {
            this.sender = t;
            this.command = cmd;
            this.label = s;

            this.args = new String[args.length + 1];
            this.args[0] = s;
            for (int i = 1; i < this.args.length; i++) {
                this.args[i] = args[i - 1];
            }
        }

        public T sender() {
            return sender;
        }

        /**
         * Check if a particular node should be ran based on the provided condition
         *
         * @param arg       The argument to check
         * @param validator The condition to determine if this node should be executed
         * @return Whether this node should be executed
         */
        public boolean validateNode(int arg, NodeValidator validator) {
            if (!hasNode(arg)) return false;
            return validator.validate(node(arg));
        }

        public boolean hasNode(int arg) {

//            Command.builder(Player.class, "balance")
//                    .addSubCommand(Player.class, b ->
//                            b.withConditions((s, w) ->
//                                    w.validateNode(0, n -> true))
//                                    .build()
//                            ).build();
            return args.length - 1 >= arg && arg >= 0;
        }

        public String node(int arg) {
            return !hasNode(arg) ? "" : args[arg];
        }

        public String[] args() {
            return args;
        }



    }

    public static class CommandBuilder<T extends CommandSender> {

        private final String command;

        private final List<String> permissions = new ArrayList<>();
        private final Class<T> allowed;
        private final List<Condition<T>> conditions = new ArrayList<>();
        private BiConsumer<T, CommandWrapper<T>> execution, failExecution;
        private final List<Command<T>> subCommands = new ArrayList<>();

        private CommandBuilder(Class<T> allowed, String command) {
            this.command = command;
            this.allowed = allowed;
        }

        private CommandBuilder(Class<T> allowed) {
            this.command = "";
            this.allowed = allowed;
        }

        public CommandBuilder<T> withExecution(BiConsumer<T, CommandWrapper<T>> execution) {
            this.execution = execution;
            return this;
        }

        public CommandBuilder<T> addSubCommand(SubBuilder<T> builder) {
            CommandBuilder<T> b = new CommandBuilder<>(allowed);
            Command<T> cmd = builder.build(b);
            subCommands.add(cmd);
            return this;
        }

        public CommandBuilder<T> withPermission(String s) {
            permissions.add(s);
            return this;
        }

        public CommandBuilder<T> withPermissions(String... strings) {
            permissions.addAll(Arrays.asList(strings));
            return this;
        }

        public CommandBuilder<T> withConditions(Condition<T>... conditions) {
            this.conditions.addAll(Arrays.asList(conditions));
            return this;
        }

        public CommandBuilder<T> withFailExecution(BiConsumer<T, CommandWrapper<T>> failExecution) {
            this.failExecution = failExecution;
            return this;
        }

        public Command<T> build() {
            Command<T> base = this.command == null ? new Command<>(allowed) : new Command<T>(allowed, this.command);
            permissions.forEach(base::addPermission);
            conditions.forEach(base::addCondition);
            base.setFailExecution(failExecution);
            base.setExecution(execution);
            return base;
        }

    }

    /**
     * Check a particular argument in the command
     * Determine if the node should be executed
     */
    @FunctionalInterface
    public interface NodeValidator {

        boolean validate(String s);

    }

    /**
     * Used to build the SubCommands
     *
     * @param <T> The authorised CommandSender
     */
    @FunctionalInterface
    public interface SubBuilder<T extends CommandSender> {

        Command<T> build(CommandBuilder<T> builder);

    }

    /**
     * Used to determine if the command should be ran
     * Any input validation should be done here
     */
    @FunctionalInterface
    public interface Condition<T extends CommandSender> {

        boolean validate(T sender, CommandWrapper<T> command);

    }


}
