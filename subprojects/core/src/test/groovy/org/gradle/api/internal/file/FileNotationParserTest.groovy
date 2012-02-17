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

package org.gradle.api.internal.file

import org.gradle.api.internal.notations.api.UnsupportedNotationException
import org.gradle.internal.nativeplatform.FileSystems
import org.gradle.util.TemporaryFolder
import org.junit.Rule
import spock.lang.Specification

class FileNotationParserTest extends Specification {

    @Rule public TemporaryFolder folder = new TemporaryFolder();

    final FileNotationParser<File> parser = new FileNotationParser<File>(FileSystems.default)

    def "with File returns this File"() {
        setup:
        def testFile = folder.createFile("test1")
        when:
        def object = parser.parseNotation(testFile)
        then:
        object instanceof File
        testFile == object
    }

    def "with file path as String"() {
        setup:
        def testFile = folder.createFile("test1")
        when:
        def object = parser.parseNotation(testFile.getAbsolutePath())
        then:
        object instanceof File
        testFile.getAbsolutePath() == object.getAbsolutePath()
    }

    def "with file:// URI"() {
        setup:
        def testFileURI = folder.createFile("test1").toURI()
        when:
        def object = parser.parseNotation(testFileURI)
        then:
        object instanceof File
        object.toURI() == testFileURI
    }

    def "with file:// URI as CharSequence"() {
        setup:
        def uriString = folder.createFile("test1").toURI().toString()
        when:
        def object = parser.parseNotation(uriString)
        then:
        object instanceof File
        object.toURI().toString() == uriString
    }

    def "with file:// URL"() {
        setup:
        def testFileURL = folder.createFile("test1").toURL()
        when:
        def object = parser.parseNotation(testFileURL)
        then:
        object instanceof File
        object.toURL() == testFileURL
    }

    def "with unsupported URI UnsupportedNotationException is thrown"() {
        setup:
        def unsupportedURI = URI.create("http://gradle.org")
        when:
        parser.parseNotation(unsupportedURI)
        then:
        thrown(UnsupportedNotationException)
    }

    def "with unsupported URI String UnsupportedNotationException is thrown"() {
            setup:
            def unsupportedURIString = "http://gradle.org"
            when:
            parser.parseNotation(unsupportedURIString)
            then:
            thrown(UnsupportedNotationException)

        }

//    @Issue("GRADLE-2072")
//    def "parsing unknown types causes UnsupportedNotationException"() {
//        setup:
//        def taskInternalMock = Mock(TaskInternal)
//        def fileResolverMock = Mock(FileResolver)
//        when:
//        parser.parseNotation(new DefaultTaskOutputs(fileResolverMock, taskInternalMock))
//        then:
//        thrown(UnsupportedNotationException)
//    }
}
