package com.disposableemail.telegram.bot.model.dto;

import com.disposableemail.telegram.client.disposableemail.webclient.model.Address;
import com.vdurmont.emoji.EmojiParser;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

@Data
public class MessageDto {

    private String id;
    private List<Address> to;
    private List<Address> from;
    private LocalDateTime date;
    private String subject;
    private String text;
    private String downloadUrl;
    private List<String> html;

    @Override
    public String toString() {
        var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        var toAddressJoiner = new StringJoiner(", ");
        var fromAddressJoiner = new StringJoiner(", ");
        if (!Objects.equals(to, null)) {
            this.to.forEach(address -> toAddressJoiner.add(address.getAddress()));
        }
        if (!Objects.equals(from, null)) {
            this.from.forEach(address -> fromAddressJoiner.add(address.getAddress()));
        }
        String dateAsString = "";
        if (!Objects.equals(date, null)) {
            dateAsString = date.format(dateTimeFormatter);
        }
        return EmojiParser.parseToUnicode(":e-mail: ") +
                "to: " + toAddressJoiner + System.lineSeparator() +
                "from: " + fromAddressJoiner + System.lineSeparator() +
                "date: " + dateAsString + System.lineSeparator() +
                "subject: " + subject + System.lineSeparator() +
                "text:* " + text;
    }
}
