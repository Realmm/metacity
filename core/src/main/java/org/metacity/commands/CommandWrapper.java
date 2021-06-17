package org.metacity.commands;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * CommandWrapper is a wrapper class for sending commands
 * Note: args starts from the initial command, this is different from normal Spigot
 * So,
 * /test sub1 sub2
 * would have an args length of 3
 * @param <T> The sender of the command
 */
public class CommandWrapper<T extends CommandSender> {

    private final T sender;
    private final org.bukkit.command.Command command;
    private final String label;
    private final String[] args;

    CommandWrapper(T t, org.bukkit.command.Command cmd, String s, String[] args) {
        this.sender = t;
        this.command = cmd;
        this.label = s;

        this.args = new String[args.length + 1];
        this.args[0] = s;
        for (int i = 1; i < this.args.length; i++) {
            this.args[i] = args[i - 1];
        }
    }

    /**
     * Get the sender of the command
     * @return The sender of the command
     */
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

    /**
     * Get all nodes at the given args
     * @param args The args to get the nodes of
     * @return The nodes at the given args
     */
    public List<String> nodes(int... args) {
        List<String> list = new ArrayList<>();
        Arrays.stream(args).forEach(i -> {
            if (!hasNode(i)) throw new IllegalStateException("Unable to get node that doesn't exist at index " + i);
            list.add(node(i));
        });
        return list;
    }

    /**
     * Get the nodes from the given arg, inclusive
     * @param arg The argument to begin getting from
     * @return All the strings after and including the given arg in the sent command
     */
    public List<String> nodesFrom(int arg) {
        return arg >= args.length ? new ArrayList<>() : Arrays.asList(Arrays.copyOfRange(args, arg, args.length).clone());
    }

    /**
     * Check if the command sent has a particular node
     * @param arg
     * @return
     */
    public boolean hasNode(int arg) {
        try {
            String s = args[arg];
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Check if the command sent has all the nodes at the given args
     * @param args The args to check
     * @return Whether all args are present
     */
    public boolean hasNodes(int... args) {
        return Arrays.stream(args).allMatch(this::hasNode);
    }

    /**
     * Get the string at this arg
     * @param arg The arg to check
     * @return The string at the arg
     */
    public String node(int arg) {
        return !hasNode(arg) ? "" : args[arg];
    }

    /**
     * Get the args of the sent command
     * @return The args of the command, initial command inclusive
     */
    public String[] args() {
        return args;
    }

}
