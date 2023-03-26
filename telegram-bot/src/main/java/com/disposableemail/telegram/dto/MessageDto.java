package com.disposableemail.telegram.dto;

import com.disposableemail.telegram.client.disposableemail.webclient.model.Address;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

@Data
public class MessageDto {
    private List<Address> to;
    private List<Address> from;
    private LocalDateTime date;
    private String subject;
    private String text;
    private String downloadUrl;

    @Override
    public String toString() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringJoiner toAddressJoiner = new StringJoiner(", ");
        StringJoiner fromAddressJoiner = new StringJoiner(", ");
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
        return "to: " + toAddressJoiner + System.lineSeparator() +
                "from: " + fromAddressJoiner + System.lineSeparator() +
                "date: " + dateAsString + System.lineSeparator() +
                "subject: " + subject + System.lineSeparator() +
                "text: " + text;
    }
}
