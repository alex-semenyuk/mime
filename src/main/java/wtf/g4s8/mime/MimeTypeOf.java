/*
 * The MIT License (MIT) Copyright (c) 2017-2021 Kirill Ch. <g4s8.public@gmail.com>
 * https://github.com/g4s8/mime/LICENSE.txt
 */
package wtf.g4s8.mime;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MIME type from string.
 *
 * @since 0.1
 */
@SuppressWarnings("PMD.StaticAccessToStaticFields")
public final class MimeTypeOf implements MimeType {

    /**
     * Type pattern.
     */
    private static final Pattern PTN_TYPE;

    /**
     * Parameters pattern.
     */
    private static final Pattern PTN_PARAM;

    static {
        final String token = "([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)";
        PTN_TYPE = Pattern.compile(
            String.format("%s/%s", token, token)
        );
        PTN_PARAM = Pattern.compile(
            String.format(
                ";\\s*(?:%s=(?:%s|\"([^\"]*)\"))?",
                token,
                token
            )
        );
    }

    /**
     * Source string.
     */
    private final String src;

    /**
     * Ctor.
     * @param source Source string
     */
    public MimeTypeOf(final String source) {
        this.src = source;
    }

    @Override
    public String type() throws IOException {
        return this.matcher().group(1).toLowerCase(Locale.US);
    }

    @Override
    public String subtype() throws IOException {
        return this.matcher().group(2).toLowerCase(Locale.US);
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Map<String, String> params() throws IOException {
        final Matcher match = this.matcher();
        final Matcher param = MimeTypeOf.PTN_PARAM.matcher(this.src);
        final Map<String, String> map = new HashMap<>(1);
        for (int id = match.end(); id < this.src.length(); id = param.end()) {
            param.region(id, this.src.length());
            if (!param.lookingAt()) {
                throw new IOException("Invalid mime-type params format");
            }
            final String name = param.group(1);
            if (map.containsKey(name)) {
                throw new IOException(
                    String.format("Parameter %s may only exist once.", name)
                );
            }
            map.put(name, MimeTypeOf.paramValue(param));
        }
        return map;
    }

    @Override
    public String toString() {
        return this.src;
    }

    /**
     * Matcher for type/subtype.
     * @return Pattern matcher
     * @throws IOException If failed
     */
    private Matcher matcher() throws IOException {
        final Matcher matcher = MimeTypeOf.PTN_TYPE.matcher(this.src);
        if (!matcher.lookingAt()) {
            throw new IOException("Invalid mime-type format");
        }
        return matcher;
    }

    /**
     * Read parameter value from matcher.
     * @param matcher Pattern matcher
     * @return Value
     * @throws IOException If failed
     * @checkstyle MagicNumberCheck (30 lines)
     */
    private static String paramValue(final Matcher matcher) throws IOException {
        final String token = matcher.group(2);
        final String value;
        if (token == null) {
            value = matcher.group(3);
        } else {
            if (token.length() > 2
                && token.charAt(0) == '\''
                && token.charAt(token.length() - 1) == '\'') {
                value = token.substring(1, token.length() - 1);
            } else {
                value = token;
            }
        }
        if (value == null) {
            throw new IOException("Bad parameter value");
        }
        return value;
    }
}