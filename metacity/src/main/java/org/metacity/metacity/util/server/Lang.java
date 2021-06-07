package org.metacity.metacity.util.server;

import java.util.Optional;

public enum Lang {

    COMMAND_API_BADUSAGE("command-api-badusage"),
    COMMAND_API_USAGE("command-api-usage"),
    COMMAND_API_REQUIREMENTS_INVALIDPLAYER("command-api-requirements-invalidplayer"),
    COMMAND_API_REQUIREMENTS_INVALIDCONSOLE("command-api-requirements-invalidconsole"),
    COMMAND_API_REQUIREMENTS_INVALIDREMOTE("command-api-requirements-invalidremote"),
    COMMAND_API_REQUIREMENTS_INVALIDBLOCK("command-api-requirements-invalidblock"),
    COMMAND_API_REQUIREMENTS_NOPERMISSION("command-api-requirements-nopermission"),

    COMMAND_ROOT_DESCRIPTION("command-root-desc"),
    COMMAND_ROOT_DETAILS("command-root-details"),

    COMMAND_BALANCE_DESCRIPTION("command-balance-desc"),
    COMMAND_BALANCE_WALLETADDRESS("command-balance-walletaddress"),
    COMMAND_BALANCE_IDENTITYID("command-balance-identityid"),
    COMMAND_BALANCE_ENJBALANCE("command-balance-enjbalance"),
    COMMAND_BALANCE_ETHBALANCE("command-balance-ethbalance"),
    COMMAND_BALANCE_TOKENDISPLAY("command-balance-tokendisplay"),
    COMMAND_BALANCE_NOTOKENS("command-balance-notokens"),
    COMMAND_BALANCE_TOKENCOUNT("command-balance-tokencount"),

    COMMAND_HELP_DESCRIPTION("command-help-desc"),

    COMMAND_LINK_DESCRIPTION("command-link-desc"),
    COMMAND_LINK_NULLWALLET("command-link-nullwallet"),
    COMMAND_LINK_SHOWWALLET("command-link-showwallet"),
    COMMAND_LINK_NULLCODE("command-link-nullcode"),
    COMMAND_LINK_INSTRUCTIONS_1("command-link-instructions.1"),
    COMMAND_LINK_INSTRUCTIONS_2("command-link-instructions.2"),
    COMMAND_LINK_INSTRUCTIONS_3("command-link-instructions.3"),
    COMMAND_LINK_INSTRUCTIONS_4("command-link-instructions.4"),
    COMMAND_LINK_INSTRUCTIONS_5("command-link-instructions.5"),
    COMMAND_LINK_INSTRUCTIONS_6("command-link-instructions.6"),
    COMMAND_LINK_INSTRUCTIONS_7("command-link-instructions.7"),
    COMMAND_LINK_SUCCESS("command-link-success"),

    COMMAND_DEVSEND_DESCRIPTION("command-devsend-desc"),
    COMMAND_DEVSEND_INVALIDAMOUNT("command-devsend-invalidamount"),
    COMMAND_DEVSEND_INVALIDTOKEN("command-devsend-invalidtoken"),

    COMMAND_SEND_DESCRIPTION("command-send-desc"),
    COMMAND_SEND_SUBMITTED("command-send-submitted"),
    COMMAND_SEND_MUSTHOLDITEM("command-send-mustholditem"),
    COMMAND_SEND_ITEMNOTTOKEN("command-send-itemnottoken"),
    COMMAND_SEND_DOESNOTHAVETOKEN("command-send-doesnothavetoken"),

    COMMAND_TOKEN_DESCRIPTION("command-token-desc"),
    COMMAND_TOKEN_CREATE_DESCRIPTION("command-token-create-desc"),
    COMMAND_TOKEN_CREATE_SUCCESS("command-token-create-success"),
    COMMAND_TOKEN_CREATE_FAILED("command-token-create-failed"),
    COMMAND_TOKEN_CREATE_DUPLICATE("command-token-create-duplicate"),
    COMMAND_TOKEN_CREATENFT_DESCRIPTION("command-token-createnft-desc"),
    COMMAND_TOKEN_CREATENFT_BASEFAILED("command-token-createnft-basefailed"),
    COMMAND_TOKEN_CREATENFT_DUPLICATE("command-token-createnft-duplicate"),
    COMMAND_TOKEN_CREATENFT_REPLACENICKNAME("command-token-createnft-replacenickname"),
    COMMAND_TOKEN_CREATENFT_MISSINGBASE("command-token-createnft-missingbase"),
    COMMAND_TOKEN_UPDATE_DESCRIPTION("command-token-update-desc"),
    COMMAND_TOKEN_UPDATE_SUCCESS("command-token-update-success"),
    COMMAND_TOKEN_UPDATE_FAILED("command-token-update-failed"),
    COMMAND_TOKEN_DELETE_DESCRIPTION("command-token-delete-desc"),
    COMMAND_TOKEN_DELETE_SUCCESS("command-token-delete-success"),
    COMMAND_TOKEN_DELETE_FAILED("command-token-delete-failed"),
    COMMAND_TOKEN_DELETE_BASENFT_1("command-token-delete-basenft.1"),
    COMMAND_TOKEN_DELETE_BASENFT_2("command-token-delete-basenft.2"),
    COMMAND_TOKEN_TOINV_DESCRIPTION("command-token-toinv-desc"),
    COMMAND_TOKEN_TOINV_SUCCESS("command-token-toinv-success"),
    COMMAND_TOKEN_TOINV_FAILED("command-token-toinv-failed"),
    COMMAND_TOKEN_NICKNAME_DESCRIPTION("command-token-nickname-desc"),
    COMMAND_TOKEN_NICKNAME_SUCCESS("command-token-nickname-success"),
    COMMAND_TOKEN_NICKNAME_DUPLICATE("command-token-nickname-duplicate"),
    COMMAND_TOKEN_NICKNAME_HAS("command-token-nickname-has"),
    COMMAND_TOKEN_NICKNAME_INVALID("command-token-nickname-invalid"),
    COMMAND_TOKEN_ADDPERM_DESCRIPTION("command-token-addperm-desc"),
    COMMAND_TOKEN_ADDPERM_PERMADDED("command-token-addperm-permadded"),
    COMMAND_TOKEN_ADDPERM_PERMREJECTED("command-token-addperm-permrejected"),
    COMMAND_TOKEN_ADDPERM_DUPLICATEPERM("command-token-addperm-duplicateperm"),
    COMMAND_TOKEN_ADDPERMNFT_DESCRIPTION("command-token-addpermnft-desc"),
    COMMAND_TOKEN_REVOKEPERM_DESCRIPTION("command-token-revokeperm-desc"),
    COMMAND_TOKEN_REVOKEPERM_PERMREVOKED("command-token-revokeperm-permrevoked"),
    COMMAND_TOKEN_REVOKEPERM_PERMNOTONTOKEN("command-token-revokeperm-permnotontoken"),
    COMMAND_TOKEN_REVOKEPERMNFT_DESCRIPTION("command-token-revokepermnft-desc"),
    COMMAND_TOKEN_PERM_ISGLOBAL("command-token-perm-isglobal"),
    COMMAND_TOKEN_GETURI_DESCRIPTION("command-token-geturi-desc"),
    COMMAND_TOKEN_GETURI_SUCCESS("command-token-geturi-success"),
    COMMAND_TOKEN_GETURI_FAILED("command-token-geturi-failed"),
    COMMAND_TOKEN_GETURI_EMPTY_1("command-token-geturi-empty.1"),
    COMMAND_TOKEN_GETURI_EMPTY_2("command-token-geturi-empty.2"),
    COMMAND_TOKEN_REMOVEURI_DESCRIPTION("command-token-removeuri-desc"),
    COMMAND_TOKEN_REMOVEURI_SUCCESS("command-token-removeuri-success"),
    COMMAND_TOKEN_REMOVEURI_FAILED("command-token-removeuri-failed"),
    COMMAND_TOKEN_REMOVEURI_EMPTY("command-token-removeuri-empty"),
    COMMAND_TOKEN_SETWALLETVIEW_DESCRIPTION("command-token-setwalletview-desc"),
    COMMAND_TOKEN_SETWALLETVIEW_INVALIDVIEW("command-token-setwalletview-invalidview"),
    COMMAND_TOKEN_SETWALLETVIEW_HAS("command-token-setwalletview-has"),
    COMMAND_TOKEN_LIST_DESCRIPTION("command-token-list-desc"),
    COMMAND_TOKEN_LIST_EMPTY("command-token-list-empty"),
    COMMAND_TOKEN_LIST_HEADER_TOKENS("command-token-list-header-tokens"),
    COMMAND_TOKEN_LIST_HEADER_NONFUNGIBLE("command-token-list-header-nonfungible"),
    COMMAND_TOKEN_EXPORT_DESCRIPTION("command-token-export-desc"),
    COMMAND_TOKEN_EXPORT_COMPLETE("command-token-export-complete"),
    COMMAND_TOKEN_EXPORT_SUCCESS("command-token-export-success"),
    COMMAND_TOKEN_EXPORT_EMPTY("command-token-export-empty"),
    COMMAND_TOKEN_EXPORT_FAILED("command-token-export-failed"),
    COMMAND_TOKEN_EXPORT_PARTIAL("command-token-export-partial"),
    COMMAND_TOKEN_IMPORT_DESCRIPTION("command-token-import-desc"),
    COMMAND_TOKEN_IMPORT_COMPLETE("command-token-import-complete"),
    COMMAND_TOKEN_IMPORT_SUCCESS("command-token-import-success"),
    COMMAND_TOKEN_IMPORT_EMPTY("command-token-import-empty"),
    COMMAND_TOKEN_IMPORT_FAILED("command-token-import-failed"),
    COMMAND_TOKEN_IMPORT_PARTIAL("command-token-import-partial"),
    COMMAND_TOKEN_NOSUCHTOKEN("command-token-nosuchtoken"),
    COMMAND_TOKEN_NOHELDITEM("command-token-nohelditem"),
    COMMAND_TOKEN_INVALIDFULLID("command-token-invalidfullid"),
    COMMAND_TOKEN_INVALIDID("command-token-invalidid"),
    COMMAND_TOKEN_INVALIDDATA("command-token-invaliddata"),
    COMMAND_TOKEN_ISFUNGIBLE("command-token-isfungible"),
    COMMAND_TOKEN_ISNONFUNGIBLE("command-token-isnonfungible"),
    COMMAND_TOKEN_ISNONFUNGIBLEINSTANCE("command-token-isnonfungibleinstance"),
    COMMAND_TOKEN_MUSTPASSINDEX("command-token-mustpassindex"),

    COMMAND_TRADE_DESCRIPTION("command-trade-desc"),
    COMMAND_TRADE_INVITE_DESCRIPTION("command-trade-invite-desc"),
    COMMAND_TRADE_ACCEPT_DESCRIPTION("command-trade-accept-desc"),
    COMMAND_TRADE_DECLINE_DESCRIPTION("command-trade-decline-desc"),
    COMMAND_TRADE_NOOPENINVITE("command-trade-noopeninvite"),
    COMMAND_TRADE_DECLINED_SENDER("command-trade-declined-sender"),
    COMMAND_TRADE_DECLINED_TARGET("command-trade-declined-target"),
    COMMAND_TRADE_ALREADYINVITED("command-trade-alreadyinvited"),
    COMMAND_TRADE_WANTSTOTRADE("command-trade-wantstotrade"),
    COMMAND_TRADE_INVITESENT("command-trade-invitesent"),
    COMMAND_TRADE_INVITEDTOTRADE("command-trade-invitedtotrade"),
    COMMAND_TRADE_CONFIRM_ACTION("command-trade-confirm-action"),
    COMMAND_TRADE_CONFIRM_WAIT("command-trade-confirm-wait"),
    COMMAND_TRADE_COMPLETE("command-trade-complete"),

    COMMAND_QR_DESCRIPTION("command-qr-desc"),
    COMMAND_QR_ALREADYLINKED("command-qr-alreadylinked"),
    COMMAND_QR_INVENTORYFULL("command-qr-inventoryfull"),
    COMMAND_QR_CODENOTLOADED("command-qr-codenotloaded"),
    COMMAND_QR_ERROR("command-qr-error"),

    COMMAND_UNLINK_DESCRIPTION("command-unlink-desc"),
    COMMAND_UNLINK_SUCCESS("command-unlink-success"),

    COMMAND_WALLET_DESCRIPTION("command-wallet-desc"),

    HINT_LINK("hint-link"),

    IDENTITY_NOTLOADED("identity-notloaded"),

    ERRORS_EXCEPTION("errors-exception"),
    ERRORS_CHOOSEOTHERPLAYER("errors-chooseotherplayer"),
    ERRORS_INVALIDPLAYERNAME("errors-invalidplayername"),
    ERRORS_PLAYERNOTONLINE("errors-playernotonline"),
    ERRORS_PLAYERNOTREGISTERED("errors-playernotregistered"),

    QR_DISPLAYNAME("qr-displayname"),

    WALLET_UI_FUNGIBLE("wallet-ui-fungible"),
    WALLET_UI_NONFUNGIBLE("wallet-ui-nonfungible"),
    WALLET_NOTLINKED_SELF("wallet-notlinked-self"),
    WALLET_NOTLINKED_OTHER("wallet-notlinked-other"),
    WALLET_ALLOWANCENOTSET("wallet-allowance"),
    WALLET_NOTENOUGHETH("wallet-notenougheth"),
    WALLET_OTHERNOTENOUGHETH("wallet-othernotenougheth");

    private final String path;

    Lang(String path) {
        this.path = path;
    }

    protected String path() {
        return path;
    }

    public int getInt(LangConfig config) {
        return getOptInt(config).orElseThrow(() -> new IllegalStateException("No int"));
    }

    public boolean getBool(LangConfig config) {
        return getOptBool(config).orElseThrow(() -> new IllegalStateException("No boolean"));
    }

    public String getString(LangConfig config) {
        return getOptString(config).orElseThrow(() -> new IllegalStateException("No string"));
    }

    public double getDouble(LangConfig config) {
        return getOptDouble(config).orElseThrow(() -> new IllegalStateException("No double"));
    }

    private Optional<Integer> getOptInt(LangConfig config) {
        return config.config().getConfig().isInt(path) ? Optional.of(config.config().getConfig().getInt(path)) : Optional.empty();
    }

    private Optional<Boolean> getOptBool(LangConfig config) {
        return config.config().getConfig().isBoolean(path) ? Optional.of(config.config().getConfig().getBoolean(path)) : Optional.empty();
    }

    private Optional<String> getOptString(LangConfig config) {
        return config.config().getConfig().isString(path) ? Optional.ofNullable(config.config().getConfig().getString(path)) : Optional.empty();
    }

    private Optional<Double> getOptDouble(LangConfig config) {
        return config.config().getConfig().isDouble(path) ? Optional.of(config.config().getConfig().getDouble(path)) : Optional.empty();
    }

}
