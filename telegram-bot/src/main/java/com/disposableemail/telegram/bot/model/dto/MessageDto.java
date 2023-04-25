package com.disposableemail.telegram.bot.model.dto;

import com.disposableemail.telegram.client.disposableemail.webclient.model.Address;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

@Data
public class MessageDto implements Comparable<MessageDto> {

    private String id;
    private List<Address> to;
    private List<Address> from;
    private LocalDateTime date;
    private String subject;
    private String text;
    private String downloadUrl;
    private List<String> html;

    private static final int MAX_CAPTION_SIZE = 1024;
    private static final int MAX_TEXT_SIZE = 4096;
    private static final String END_TRUNCATED_MESSAGE = "...";
    private static final String DATE_TIME_PATTERN = "dd.MM.yyyy HH:mm (z)";


    public String getSendMessageText() {
        return handleLongMessage(MAX_TEXT_SIZE);
    }

    public String getSendDocumentCaption() {
        return handleLongMessage(MAX_CAPTION_SIZE);
    }

    @Override
    public int compareTo(MessageDto o) {
        if (isEmptyDate(o)) {
            return -1;
        } else {
            return getDate().compareTo(o.getDate());
        }
    }

    private boolean isEmptyDate(MessageDto o) {
        return Objects.isNull(this.getDate()) || Objects.isNull(o.getDate());
    }

    private String handleLongMessage(int maxSize) {
        var result = getMessageHtmlMode();
        int messageLength = getMessageHtmlMode().length();
        if (messageLength > maxSize) {
            int oversizeLength = messageLength - maxSize;
            int truncatedTextSize = text.length() - oversizeLength - END_TRUNCATED_MESSAGE.length();
            var truncatedText = StringUtils.truncate(text, truncatedTextSize) + END_TRUNCATED_MESSAGE;
            return result.replace(text, truncatedText);
        } else {
            return result;
        }
    }

    private String getMessageHtmlMode() {
        var toAddressJoiner = new StringJoiner(", ");
        var fromAddressJoiner = new StringJoiner(", ");
        if (Objects.nonNull(to)) {
            this.to.forEach(address -> toAddressJoiner.add(address.getAddress()));
        }
        if (Objects.nonNull(from)) {
            this.from.forEach(address -> fromAddressJoiner.add(address.getAddress()));
        }
        String dateAsString = "";
        if (Objects.nonNull(date)) {
            dateAsString = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)
                    .format(ZonedDateTime.of(date, ZoneId.of("UTC")));
        }
        return ":e-mail: " + System.lineSeparator() +
                "<b>to:</b> " + toAddressJoiner + System.lineSeparator() +
                "<b>from:</b> " + fromAddressJoiner + System.lineSeparator() +
                "<b>date:</b> " + dateAsString + System.lineSeparator() +
                "<b>subject:</b> " + "<u>" + subject + "</u>" + System.lineSeparator() +
                "<b>text:</b> " + System.lineSeparator() +
                "<i>" + text + "</i>";
    }

}
