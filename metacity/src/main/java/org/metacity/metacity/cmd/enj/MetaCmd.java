package org.metacity.metacity.cmd.enj;

import org.bukkit.command.CommandSender;
import org.metacity.commands.Command;
import org.metacity.metacity.cmd.enj.perm.AddPermCmd;
import org.metacity.metacity.cmd.enj.perm.AddPermNFTCmd;
import org.metacity.metacity.cmd.enj.perm.RevokePermCmd;
import org.metacity.metacity.cmd.enj.perm.RevokePermNFTCmd;
import org.metacity.metacity.cmd.chain.LinkCmd;
import org.metacity.metacity.cmd.chain.UnlinkCmd;
import org.metacity.metacity.cmd.enj.token.CreateCmd;
import org.metacity.metacity.cmd.enj.token.DeleteCmd;
import org.metacity.metacity.cmd.enj.token.ExportCmd;
import org.metacity.metacity.cmd.enj.token.GetURICmd;
import org.metacity.metacity.cmd.enj.token.GiveCmd;
import org.metacity.metacity.cmd.enj.token.ImportCmd;
import org.metacity.metacity.cmd.enj.token.ListCmd;
import org.metacity.metacity.cmd.enj.token.NicknameCmd;
import org.metacity.metacity.cmd.enj.token.RemoveURICmd;
import org.metacity.metacity.cmd.enj.token.SetWalletViewCmd;
import org.metacity.metacity.cmd.enj.token.UpdateCmd;
import org.metacity.metacity.cmd.chain.wallet.send.DevSendCmd;
import org.metacity.metacity.cmd.chain.wallet.send.SendCmd;
import org.metacity.metacity.cmd.chain.wallet.trade.AcceptCmd;
import org.metacity.metacity.cmd.chain.wallet.trade.DeclineCmd;
import org.metacity.metacity.cmd.chain.wallet.trade.InviteCmd;

public class MetaCmd extends Command<CommandSender> {

    public MetaCmd() {
        super(CommandSender.class, "meta");
        addSubCommands(
                new AddPermCmd(),
                new AddPermNFTCmd(),
                new RevokePermCmd(),
                new RevokePermNFTCmd(),

                new CreateCmd(),
                new DeleteCmd(),
                new ExportCmd(),
                new GetURICmd(),
                new GiveCmd(),
                new ImportCmd(),
                new ListCmd(),
                new NicknameCmd(),
                new RemoveURICmd(),
                new SetWalletViewCmd(),
                new UpdateCmd()
        );
    }

}
