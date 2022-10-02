/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package com.disposableemail.apache.james.listener;

import org.apache.james.events.Event;
import org.apache.james.events.EventListener;
import org.apache.james.events.Group;
import org.apache.james.mailbox.MailboxManager;
import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.MessageManager;
import org.apache.james.mailbox.MessageUid;
import org.apache.james.mailbox.events.MailboxEvents.Added;
import org.apache.james.mailbox.exception.MailboxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

/**
 * A Listener to determine the size of added messages.
 * <p>
 * If the size is greater or equals than the BIG_MESSAGE size threshold ({@value ONE_MB}).
 * Then it will be considered as a big message and will be deleted
 */
class DeleteBigMessages implements EventListener.GroupEventListener {
    public static class PositionCustomFlagOnBigMessagesGroup extends Group {

    }

    private static final PositionCustomFlagOnBigMessagesGroup GROUP = new PositionCustomFlagOnBigMessagesGroup();
    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteBigMessages.class);

    static final long ONE_MB = 200L * 100L;

    private final MailboxManager mailboxManager;

    @Inject
    DeleteBigMessages(MailboxManager mailboxManager) {
        this.mailboxManager = mailboxManager;
    }

    @Override
    public void event(Event event) {
        if (event instanceof Added) {
            Added addedEvent = (Added) event;
            addedEvent.getUids().stream()
                    .filter(messageUid -> isBig(addedEvent, messageUid))
                    .forEach(messageUid -> setBigMessageFlag(addedEvent, messageUid));
        }
    }

    private boolean isBig(Added addedEvent, MessageUid messageUid) {
        return addedEvent.getMetaData(messageUid).getSize() >= ONE_MB;
    }

    private void setBigMessageFlag(Added addedEvent, MessageUid messageUid) {
        try {
            MailboxSession session = mailboxManager.createSystemSession(addedEvent.getUsername());
            MessageManager messageManager = mailboxManager.getMailbox(addedEvent.getMailboxId(), session);

            messageManager.delete(List.of(messageUid), session);

                   } catch (MailboxException e) {
            LOGGER.error("error happens when deleting the message with uid {} in mailbox {} of user {}",
                    messageUid.asLong(), addedEvent.getMailboxId(), addedEvent.getUsername().asString(), e);
        }
    }

    @Override
    public Group getDefaultGroup() {
        return GROUP;
    }
}
