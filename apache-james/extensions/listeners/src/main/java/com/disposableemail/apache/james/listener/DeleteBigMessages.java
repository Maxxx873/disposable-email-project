package com.disposableemail.apache.james.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.james.events.Event;
import org.apache.james.events.EventListener;
import org.apache.james.events.Group;
import org.apache.james.mailbox.MailboxManager;
import org.apache.james.mailbox.MessageUid;
import org.apache.james.mailbox.events.MailboxEvents.Added;
import org.apache.james.mailbox.exception.MailboxException;

import javax.inject.Inject;
import java.util.List;

/**
 * A Listener to determine the size of added messages.
 * <p>
 * If the size is greater or equals than the BIG_MESSAGE size threshold ({@value SIZE_MB}).
 * Then it will be considered as a big message and will be deleted
 */

@Slf4j
class DeleteBigMessages implements EventListener.GroupEventListener {

    public static class PositionCustomFlagOnBigMessagesGroup extends Group {
    }

    private static final PositionCustomFlagOnBigMessagesGroup GROUP = new PositionCustomFlagOnBigMessagesGroup();

    static final long SIZE_MB = 200L * 100L;

    private final MailboxManager mailboxManager;

    @Inject
    DeleteBigMessages(MailboxManager mailboxManager) {
        this.mailboxManager = mailboxManager;
    }

    @Override
    public void event(Event event) {
        if (event instanceof Added) {
            Added addedEvent = (Added) event;
            addedEvent.getUids().stream().filter(messageUid -> isBig(addedEvent, messageUid))
                    .forEach(messageUid -> deleteBigMessage(addedEvent, messageUid));
        }
    }

    private boolean isBig(Added addedEvent, MessageUid messageUid) {
        return addedEvent.getMetaData(messageUid).getSize() >= SIZE_MB;
    }

    private void deleteBigMessage(Added addedEvent, MessageUid messageUid) {
        try {
            var session = mailboxManager.createSystemSession(addedEvent.getUsername());
            var messageManager = mailboxManager.getMailbox(addedEvent.getMailboxId(), session);
            messageManager.delete(List.of(messageUid), session);
        } catch (MailboxException e) {
            log.error("error happens when deleting the message with uid {} in mailbox {} of user {}",
                    messageUid.asLong(), addedEvent.getMailboxId(), addedEvent.getUsername().asString(), e);
        }
    }

    @Override
    public Group getDefaultGroup() {
        return GROUP;
    }
}
