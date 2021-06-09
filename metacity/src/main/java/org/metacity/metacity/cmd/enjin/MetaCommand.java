package org.metacity.metacity.cmd.enjin;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.metacity.metacity.SpigotBootstrap;
import org.metacity.metacity.enums.CommandProcess;
import org.metacity.metacity.enums.MessageAction;
import org.metacity.metacity.enums.Usage;
import org.metacity.metacity.enums.VeryifyRequirements;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.util.TextUtil;
import org.metacity.metacity.util.server.Translation;
import org.metacity.util.CC;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class MetaCommand {

    protected SpigotBootstrap bootstrap;
    protected Optional<MetaCommand> parent;
    protected List<String> aliases;
    protected List<MetaCommand> subCommands;
    protected List<String> requiredArgs;
    protected List<String> optionalArgs;
    protected CommandRequirements requirements;

    protected MetaCommand(SpigotBootstrap bootstrap, MetaCommand parent) {
        this.bootstrap = bootstrap;
        this.parent = Optional.ofNullable(parent);
        this.aliases = new ArrayList<>();
        this.subCommands = new ArrayList<>();
        this.requiredArgs = new ArrayList<>();
        this.optionalArgs = new ArrayList<>();
        this.requirements = CommandRequirements.builder()
                .withAllowedSenderTypes(SenderType.ANY)
                .build();
    }

    public MetaCommand(SpigotBootstrap bootstrap) {
        this(bootstrap, null);
    }

    public MetaCommand(MetaCommand parent) {
        this(parent.bootstrap, parent);
    }

    public abstract void execute(CommandContext context);

    public abstract Translation getUsageTranslation();

    public List<String> tab(CommandContext context) {
        return new ArrayList<>();
    }

    private List<String> tab0(CommandContext context) {
        List<String> tabResults = new ArrayList<>();

        if (!subCommands.isEmpty()) {
            List<String> als = subCommands.stream()
                    .filter(c -> c.requirements.areMet(context, MessageAction.OMIT))
                    .map(c -> c.aliases.get(0).toLowerCase())
                    .collect(Collectors.toList());

            if (!context.args.isEmpty()) {
                tabResults.addAll(als.stream()
                        .filter(a -> a.startsWith(context.args.get(0).toLowerCase()))
                        .collect(Collectors.toList()));
            }
        } else {
            tabResults.addAll(tab(context));
        }

        return tabResults;
    }

    public void showHelp(CommandSender sender) {
        showHelp(sender, VeryifyRequirements.YES, Usage.ALL);
    }

    public void showHelp(CommandSender sender, VeryifyRequirements verifyRequirements, Usage usage) {
        if (verifyRequirements == VeryifyRequirements.YES && !requirements.areMet(sender, MessageAction.OMIT))
            return;

        sender.sendMessage(getUsage(SenderType.type(sender), usage));
    }

    public String getUsage(SenderType type, Usage usage) {
        String output = buildUsage(type, usage);
        if (type != SenderType.PLAYER)
            output = output.replaceFirst("/", "");

        return output;
    }

    public String buildUsage(SenderType type, Usage usage) {
        StringBuilder builder = new StringBuilder();

        builder.append("&6/");

        List<MetaCommand> commandStack = CommandContext.createCommandStackAsList(this);
        for (int i = 0; i < commandStack.size(); i++) {
            MetaCommand command = commandStack.get(i);

            if (i > 0)
                builder.append(' ');

            builder.append(TextUtil.concat(command.aliases, ","));
        }

        builder.append("&e");

        if (!requiredArgs.isEmpty()) {
            builder.append(' ')
                    .append(TextUtil.concat(requiredArgs.stream()
                            .map(s -> String.format("<%s>", s))
                            .collect(Collectors.toList()), " "));
        }

        if (!optionalArgs.isEmpty()) {
            builder.append(' ')
                    .append(TextUtil.concat(optionalArgs.stream()
                            .map(s -> String.format("[%s]", s))
                            .collect(Collectors.toList()), " "));
        }

        if (usage == Usage.ALL)
            builder.append(" &f").append(getUsageTranslation().translation());

        return CC.translate('&', builder.toString());
    }

    public void process(CommandContext context, CommandProcess process) {
        try {
            if (!isValid(context, process.getMessageAction()))
                return;

            if (!context.args.isEmpty()) {
                for (MetaCommand subCommand : subCommands) {
                    if (!subCommand.aliases.contains(context.args.get(0).toLowerCase()))
                        continue;

                    context.args.remove(0);
                    context.commandStack.push(this);
                    subCommand.process(context, process);
                    return;
                }
            }

            if (process == CommandProcess.EXECUTE)
                execute(context);
            else
                context.tabCompletionResult = tab0(context);
        } catch (Exception ex) {
            bootstrap.log(ex);
            Translation.ERRORS_EXCEPTION.send(context.sender, ex.getMessage());
        }
    }

    protected void addSubCommand(MetaCommand subCommand) {
        this.subCommands.add(subCommand);
    }

    private boolean isValid(CommandContext context, MessageAction action) {
        return requirements.areMet(context, action) && validArgs(context, action);
    }

    private boolean validArgs(CommandContext context, MessageAction action) {
        boolean result = context.args.size() >= requiredArgs.size();

        if (action == MessageAction.SEND && !result) {
            Translation.COMMAND_API_BADUSAGE.send(context.sender);
            Translation.COMMAND_API_USAGE.send(context.sender, getUsage(context.senderType, Usage.COMMAND_ONLY));
        }

        return result;
    }

    protected MetaPlayer getValidSenderEnjPlayer(@NonNull CommandContext context) throws NullPointerException {
        Player sender = Objects.requireNonNull(context.player, "Expected context to have non-null player as sender");

        MetaPlayer senderMetaPlayer = context.metaPlayer;
        if (senderMetaPlayer == null) {
            Translation.ERRORS_PLAYERNOTREGISTERED.send(sender, sender.getName());
            return null;
        } else if (!senderMetaPlayer.isLinked()) {
            Translation.WALLET_NOTLINKED_SELF.send(sender);
            return null;
        }

        return senderMetaPlayer;
    }

    protected Player getValidTargetPlayer(@NonNull CommandContext context, @NonNull String targetName) {
        Player targetPlayer = Bukkit.getPlayer(targetName);
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            Translation.ERRORS_PLAYERNOTONLINE.send(context.sender, targetName);
            return null;
        } else if (context.player != null && context.player == targetPlayer) {
            Translation.ERRORS_CHOOSEOTHERPLAYER.send(context.sender);
            return null;
        }

        return targetPlayer;
    }

    protected MetaPlayer getValidTargetEnjPlayer(@NonNull CommandContext context,
                                                 @NonNull Player targetPlayer) throws NullPointerException {
        MetaPlayer targetMetaPlayer = bootstrap.getPlayerManager()
                .getPlayer(targetPlayer)
                .orElse(null);
        if (targetMetaPlayer == null) {
            Translation.ERRORS_PLAYERNOTREGISTERED.send(context.sender, targetPlayer.getName());
            return null;
        } else if (!targetMetaPlayer.isLinked()) {
            Translation.WALLET_NOTLINKED_OTHER.send(context.sender, targetPlayer.getName());
            return null;
        }

        return targetMetaPlayer;
    }

    public SpigotBootstrap bootstrap() {
        return bootstrap;
    }

}
