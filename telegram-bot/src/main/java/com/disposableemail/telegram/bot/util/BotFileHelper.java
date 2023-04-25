package com.disposableemail.telegram.bot.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public final class BotFileHelper {

    private BotFileHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static InputStream getHtmlPartsInputStream(List<String> htmlParts) {
        if (Objects.nonNull(htmlParts)) {
            var outputStream = new ByteArrayOutputStream();
            htmlParts.forEach(htmlPart -> {
                try {
                    outputStream.write(htmlPart.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new IllegalStateException("Illegal html part");
                }
            });
            return new ByteArrayInputStream(outputStream.toByteArray());
        }
        return InputStream.nullInputStream();
    }
}
