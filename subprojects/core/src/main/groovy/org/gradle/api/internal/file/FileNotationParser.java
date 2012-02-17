/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.file;

import org.gradle.api.UncheckedIOException;
import org.gradle.api.internal.notations.api.NotationParser;
import org.gradle.api.internal.notations.api.UnsupportedNotationException;
import org.gradle.internal.nativeplatform.FileSystem;
import org.gradle.util.DeprecationLogger;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileNotationParser<T extends File> implements NotationParser<T> {

    private static final Pattern URI_SCHEME = Pattern.compile("[a-zA-Z][a-zA-Z0-9+-\\.]*:.+");
    private static final Pattern ENCODED_URI = Pattern.compile("%([0-9a-fA-F]{2})");
    private final FileSystem fileSystem;

    public FileNotationParser(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    public void describe(Collection<String> candidateFormats) {
        candidateFormats.add("File, URI, URL or CharSequence is supported");
    }

    public T parseNotation(Object notation) throws UnsupportedNotationException {
        if (notation instanceof File) {
            return (T) notation;
        }
        if (notation instanceof URL) {
            try {
                notation = ((URL) notation).toURI();
            } catch (URISyntaxException e) {
                throw new UncheckedIOException(e);
            }
        }
        if (notation instanceof URI) {
            URI uri = (URI) notation;
            if (uri.getScheme().equals("file")) {
                return (T) new File(uri.getPath());
            }
            throw new UnsupportedNotationException(String.format("Unable to convert URI %s to a file", uri.toString()));
        }
        if (notation instanceof CharSequence) {
            String notationString = notation.toString();
            if (notationString.startsWith("file:")) {
                return (T) new File(uriDecode(notationString.substring(5)));
            }
            // Check if string starts with a URI scheme
            if (URI_SCHEME.matcher(notationString).matches()) {
                throw new UnsupportedNotationException(String.format("Cannot convert URL '%s' to a file.", notationString));
            }
            for (File file : File.listRoots()) {
                String rootPath = file.getAbsolutePath();
                String normalisedStr = notationString;
                if (!fileSystem.isCaseSensitive()) {
                    rootPath = rootPath.toLowerCase();
                    normalisedStr = normalisedStr.toLowerCase();
                }
                if (normalisedStr.startsWith(rootPath) || normalisedStr.startsWith(rootPath.replace(File.separatorChar,
                        '/'))) {
                    return (T) new File(notationString);
                }
            }
        }else{
            DeprecationLogger.nagUserWith(String.format("Converting class %s to URI using toString() Method. "
                                    + " This has been deprecated and will be removed in the next version of Gradle. Please use java.io.File, java.lang.String, java.net.URL, or java.net.URI instead.", notation.getClass().getName()));
        }
        return (T)new File(notation.toString());
    }

    private String uriDecode(String path) {
        StringBuffer builder = new StringBuffer();
        Matcher matcher = ENCODED_URI.matcher(path);
        while (matcher.find()) {
            String val = matcher.group(1);
            matcher.appendReplacement(builder, String.valueOf((char) (Integer.parseInt(val, 16))));
        }
        matcher.appendTail(builder);
        return builder.toString();
    }

}
