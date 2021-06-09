package org.metacity.metacity.cmd.enjin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.metacity.metacity.SpigotBootstrap;
import org.metacity.metacity.cmd.enjin.player.CmdLink;
import org.metacity.metacity.cmd.enjin.player.CmdQr;
import org.metacity.metacity.cmd.enjin.player.CmdUnlink;
import org.metacity.metacity.cmd.enjin.token.CmdToken;
import org.metacity.metacity.cmd.enjin.wallet.CmdBalance;
import org.metacity.metacity.cmd.enjin.wallet.CmdWallet;
import org.metacity.metacity.cmd.enjin.wallet.send.CmdDevSend;
import org.metacity.metacity.cmd.enjin.wallet.send.CmdSend;
import org.metacity.metacity.cmd.enjin.wallet.trade.CmdTrade;
import org.metacity.metacity.enums.CommandProcess;
import org.metacity.metacity.exceptions.UnregisteredPlayerException;
import org.metacity.metacity.util.server.Translation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CmdMeta extends MetaCommand implements CommandExecutor, TabCompleter {

    private final CmdHelp cmdHelp;

    public CmdMeta(SpigotBootstrap bootstrap) {
        super(bootstrap);
        this.aliases.add("meta");
        this.addSubCommand(new CmdBalance(this));
        this.addSubCommand(new CmdDevSend(this));
        this.addSubCommand(this.cmdHelp = new CmdHelp(this));
        this.addSubCommand(new CmdLink(this));
        this.addSubCommand(new CmdQr(this));
        this.addSubCommand(new CmdSend(this));
        this.addSubCommand(new CmdToken(this));
        this.addSubCommand(new CmdTrade(this));
        this.addSubCommand(new CmdUnlink(this));
        this.addSubCommand(new CmdWallet(this));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            process(new CommandContext(bootstrap, sender, new ArrayList<>(Arrays.asList(args)), label), CommandProcess.EXECUTE);
        } catch (UnregisteredPlayerException ex) {
            bootstrap.log(ex);
        }

        return true;
    }

    @Override
    public void execute(CommandContext context) {
        Plugin plugin = bootstrap.plugin();
        PluginDescriptionFile description = plugin.getDescription();
        Translation.COMMAND_ROOT_DETAILS.send(context.sender,
                description.getName(),
                description.getVersion(),
                "/enj help");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        CommandContext context = new CommandContext(bootstrap, sender, new ArrayList<>(Arrays.asList(args)), label);
        process(context, CommandProcess.TAB);
        return context.tabCompletionResult;
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.COMMAND_ROOT_DESCRIPTION;
    }

}
