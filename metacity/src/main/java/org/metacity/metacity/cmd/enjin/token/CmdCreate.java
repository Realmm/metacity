package org.metacity.metacity.cmd.enjin.token;

import com.enjin.sdk.graphql.GraphQLResponse;
import com.enjin.sdk.models.token.GetToken;
import com.enjin.sdk.models.token.Token;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.metacity.metacity.cmd.enjin.CommandContext;
import org.metacity.metacity.cmd.enjin.CommandRequirements;
import org.metacity.metacity.cmd.enjin.MetaCommand;
import org.metacity.metacity.cmd.enjin.SenderType;
import org.metacity.metacity.conversations.Conversations;
import org.metacity.metacity.conversations.prompts.TokenIdPrompt;
import org.metacity.metacity.conversations.prompts.TokenIndexPrompt;
import org.metacity.metacity.conversations.prompts.TokenNicknamePrompt;
import org.metacity.metacity.conversations.prompts.TokenTypePrompt;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.token.TokenManager;
import org.metacity.metacity.token.TokenModel;
import org.metacity.metacity.util.TokenUtils;
import org.metacity.metacity.util.server.Translation;

import java.math.BigInteger;
import java.util.Map;

public class CmdCreate extends MetaCommand {

    public CmdCreate(MetaCommand parent) {
        super(parent);
        this.aliases.add("create");
        this.requiredArgs.add("token-id");
        this.requirements = CommandRequirements.builder()
                .withPermission(Permission.CMD_TOKEN_CREATE)
                .withAllowedSenderTypes(SenderType.PLAYER)
                .build();
    }

    @Override
    public void execute(CommandContext context) {
        Player sender = context.player();
        ItemStack held = sender.getInventory().getItemInMainHand();

        // Ensure player is holding a valid item
        if (held.getType() == Material.AIR || !held.getType().isItem()) {
            Translation.COMMAND_TOKEN_NOHELDITEM.send(sender);
            return;
        }

        String id = context.args().get(0);
        TokenManager tm = bootstrap.getTokenManager();
        TokenModel model = tm.getToken(id);

        if (model == null) {
            bootstrap().getTrustedPlatformClient().getTokenService().getTokenAsync(new GetToken().tokenId(id), res -> {
                if (res.isEmpty())
                    return;

                GraphQLResponse<Token> resGql = res.body();
                if (!resGql.isSuccess())
                    return;

                Token token = resGql.getData();
                Bukkit.getScheduler().runTask(bootstrap.plugin(), () -> startConversation(sender, held, id, token.getNonFungible(), false));
            });
        } else {
            startConversation(sender, held, model.getId(), model.isNonfungible(), true);
        }
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.COMMAND_TOKEN_CREATE_DESCRIPTION;
    }

    public void startConversation(Conversable sender, ItemStack ref, String id, boolean nft, boolean baseExists) {
        // Setup Conversation
        Conversations conversations = new Conversations(bootstrap.plugin(), nft, baseExists);
        Conversation conversation = conversations.startTokenCreationConversation(sender);
        conversation.addConversationAbandonedListener(this::execute);
        conversation.getContext().setSessionData("sender", sender);
        conversation.getContext().setSessionData("nbt-item", NBTItem.convertItemtoNBT(ref));
        conversation.getContext().setSessionData(TokenTypePrompt.KEY, nft);
        conversation.getContext().setSessionData(TokenIdPrompt.KEY, id);
        conversation.begin();
    }

    public void execute(ConversationAbandonedEvent event) {
        // Check if the conversation completed gracefully.
        if (!event.gracefulExit())
            return;

        // Load managers and data store
        Map<Object, Object> data = event.getContext().getAllSessionData();
        TokenManager tokenManager = bootstrap.getTokenManager();
        // Load data from conversation context
        Player sender = (Player) data.get("sender");
        boolean nft = (boolean) data.get(TokenTypePrompt.KEY);
        String id = (String) data.get(TokenIdPrompt.KEY);
        BigInteger index = (BigInteger) data.getOrDefault(TokenIndexPrompt.KEY, BigInteger.ZERO);
        // Convert index from decimal to hexadecimal representation
        String indexHex = index == null ? null : TokenUtils.bigIntToIndex(index);

        // Check whether the token can be created if another already exists.
        // This will only ever pass if the token is an nft, the index is non-zero
        // and doesn't exist in the database.
        if (tokenManager.hasToken(id)) {
            TokenModel base = tokenManager.getToken(id);

            if (base.isNonfungible() && !nft) {
                Translation.COMMAND_TOKEN_ISFUNGIBLE.send(sender);
                return;
            } else if (!base.isNonfungible()) {
                Translation.COMMAND_TOKEN_CREATE_DUPLICATE.send(sender);
                return;
            } else if (tokenManager.hasToken(TokenUtils.createFullId(id, indexHex))) {
                Translation.COMMAND_TOKEN_CREATENFT_DUPLICATE.send(sender);
                return;
            }
        } else if (nft && !index.equals(BigInteger.ZERO)) {
            Translation.COMMAND_TOKEN_CREATENFT_MISSINGBASE.send(sender);
            return;
        }

        // Start token model creation process
        NBTContainer nbt = (NBTContainer) data.get("nbt-item");
        TokenModel.TokenModelBuilder modelBuilder = TokenModel.builder()
                .id(id)
                .nonfungible(nft)
                .nbt(nbt.toString());

        // Add index if creating an nft
        if (nft) {
            modelBuilder.index(indexHex);
        }

        // Validate and add nickname if present
        if (data.containsKey(TokenNicknamePrompt.KEY)) {
            String nickname = (String) data.get(TokenNicknamePrompt.KEY);

            if (!TokenManager.isValidAlternateId(nickname)) {
                Translation.COMMAND_TOKEN_NICKNAME_INVALID.send(sender);
                return;
            }

            modelBuilder.alternateId(nickname);
        }

        // Create token model and save to database
        TokenModel model = modelBuilder.build();
        int result = tokenManager.saveToken(model);

        // Inform sender of result or log to console if unknown
        switch (result) {
            case TokenManager.TOKEN_CREATE_SUCCESS:
                Translation.COMMAND_TOKEN_CREATE_SUCCESS.send(sender);
                break;
            case TokenManager.TOKEN_CREATE_FAILED:
                Translation.COMMAND_TOKEN_CREATE_FAILED.send(sender);
                break;
            case TokenManager.TOKEN_ALREADYEXISTS:
                Translation translation = nft
                        ? Translation.COMMAND_TOKEN_CREATENFT_DUPLICATE
                        : Translation.COMMAND_TOKEN_CREATE_DUPLICATE;
                translation.send(sender);
                break;
            case TokenManager.TOKEN_INVALIDDATA:
                Translation.COMMAND_TOKEN_INVALIDDATA.send(sender);
                break;
            case TokenManager.TOKEN_CREATE_FAILEDNFTBASE:
                Translation.COMMAND_TOKEN_CREATENFT_BASEFAILED.send(sender);
                break;
            case TokenManager.TOKEN_DUPLICATENICKNAME:
                Translation.COMMAND_TOKEN_NICKNAME_DUPLICATE.send(sender);
                break;
            case TokenManager.TOKEN_INVALIDNICKNAME:
                Translation.COMMAND_TOKEN_NICKNAME_INVALID.send(sender);
                break;
            default:
                bootstrap.debug(String.format("Unhandled result when creating token (status: %d)", result));
                break;
        }
    }

}
