package org.metacity.metacity.cmd.enjin;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.metacity.metacity.Bootstrap;
import org.metacity.metacity.cmd.enjin.arg.PlayerArgumentProcessor;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.exceptions.UnregisteredPlayerException;

import java.util.*;

public class CommandContext {

    protected CommandSender sender;
    protected SenderType senderType;
    @Nullable
    protected Player player;
    @Nullable
    protected MetaPlayer metaPlayer;
    protected List<String> args;
    protected String alias;
    protected Deque<EnjCommand> commandStack;
    protected List<String> tabCompletionResult;

    public CommandContext(Bootstrap bootstrap, CommandSender sender, List<String> args, String alias) {
        this.sender = sender;
        this.senderType = SenderType.type(sender);
        this.args = args;
        this.alias = alias;
        this.commandStack = new ArrayDeque<>();
        this.tabCompletionResult = new ArrayList<>();

        if (sender instanceof Player) {
            this.player = (Player) sender;
            this.metaPlayer = bootstrap.getPlayerManager()
                    .getPlayer(this.player)
                    .orElse(null);
            if (this.metaPlayer == null)
                throw new UnregisteredPlayerException(player);
        }
    }

    public Optional<Player> argToPlayer(int index) {
        if (args.isEmpty() || index >= args.size())
            return Optional.empty();

        return PlayerArgumentProcessor.INSTANCE.parse(sender, args.get(index));
    }

    public Optional<Integer> argToInt(int index) {
        return index < args.size()
                ? Optional.of(Integer.parseInt(args.get(index)))
                : Optional.empty();
    }

    public static List<EnjCommand> createCommandStackAsList(EnjCommand top) {
        List<EnjCommand> list = new ArrayList<>();

        list.add(top);
        Optional<EnjCommand> parent = top.parent;
        while (parent.isPresent()) {
            list.add(0, parent.get());
            parent = parent.get().parent;
        }

        return list;
    }

    public MetaPlayer enjinPlayer() {
        return metaPlayer;
    }

    public Player player() {
        return player;
    }

    public CommandSender sender() {
        return sender;
    }

    public List<String> args() {
        return args;
    }

}
