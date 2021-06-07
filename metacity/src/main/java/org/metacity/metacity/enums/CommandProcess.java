package org.metacity.metacity.enums;

public enum CommandProcess {

    EXECUTE(MessageAction.SEND),
    TAB(MessageAction.OMIT);

    private final MessageAction messageAction;

    CommandProcess(MessageAction messageAction) {
        this.messageAction = messageAction;
    }

    public MessageAction getMessageAction() {
        return messageAction;
    }
}
