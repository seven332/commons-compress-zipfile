/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.zip.CRC32;

import org.apache.commons.compress.AbstractTestCase;
import org.apache.commons.compress.utils.CharsetNames;
import org.junit.Test;

public class UTF8ZipFilesTest extends AbstractTestCase {

    private static final String CP437 = "cp437";
    private static final String ASCII_TXT = "ascii.txt";
    private static final String EURO_FOR_DOLLAR_TXT = "\u20AC_for_Dollar.txt";
    private static final String OIL_BARREL_TXT = "\u00D6lf\u00E4sser.txt";

    /*
     * 7-ZIP created archive, uses EFS to signal UTF-8 filenames.
     *
     * 7-ZIP doesn't use EFS for strings that can be encoded in CP437
     * - which is true for OIL_BARREL_TXT.
     */
    @Test
    public void testRead7ZipArchive() throws IOException {
        final File archive = getFile("utf8-7zip-test.zip");
        ZipFile zf = null;
        try {
            zf = new ZipFile(archive, CP437, false);
            assertNotNull(zf.getEntry(ASCII_TXT));
            assertNotNull(zf.getEntry(EURO_FOR_DOLLAR_TXT));
            assertNotNull(zf.getEntry(OIL_BARREL_TXT));
        } finally {
            ZipFile.closeQuietly(zf);
        }
    }

    /*
     * WinZIP created archive, uses Unicode Extra Fields but only in
     * the central directory.
     */
    @Test
    public void testReadWinZipArchive() throws IOException {
        final File archive = getFile("utf8-winzip-test.zip");
        ZipFile zf = null;
        try {
            zf = new ZipFile(archive, null, true);
            assertCanRead(zf, ASCII_TXT);
            assertCanRead(zf, EURO_FOR_DOLLAR_TXT);
            assertCanRead(zf, OIL_BARREL_TXT);
        } finally {
            ZipFile.closeQuietly(zf);
        }
    }

    private void assertCanRead(final ZipFile zf, final String fileName) throws IOException {
        final ZipArchiveEntry entry = zf.getEntry(fileName);
        assertNotNull("Entry doesn't exist", entry);
        final InputStream is = zf.getInputStream(entry);
        assertNotNull("InputStream is null", is);
        try {
            is.read();
        } finally {
            is.close();
        }
    }

    @Test
    public void testRawNameReadFromZipFile()
        throws IOException {
        final File archive = getFile("utf8-7zip-test.zip");
        ZipFile zf = null;
        try {
            zf = new ZipFile(archive, CP437, false);
            assertRawNameOfAcsiiTxt(zf.getEntry(ASCII_TXT));
        } finally {
            ZipFile.closeQuietly(zf);
        }
    }

    private static void testFile(final File file, final String encoding)
        throws IOException {
        ZipFile zf = null;
        try {
            zf = new ZipFile(file, encoding, false);

            final Enumeration<ZipArchiveEntry> e = zf.getEntries();
            while (e.hasMoreElements()) {
                final ZipArchiveEntry ze = e.nextElement();

                if (ze.getName().endsWith("sser.txt")) {
                    assertUnicodeName(ze, OIL_BARREL_TXT, encoding);

                } else if (ze.getName().endsWith("_for_Dollar.txt")) {
                    assertUnicodeName(ze, EURO_FOR_DOLLAR_TXT, encoding);
                } else if (!ze.getName().equals(ASCII_TXT)) {
                    throw new AssertionError("Unrecognized ZIP entry with name ["
                                             + ze.getName() + "] found.");
                }
            }
        } finally {
            ZipFile.closeQuietly(zf);
        }
    }

    private static UnicodePathExtraField findUniCodePath(final ZipArchiveEntry ze) {
        return (UnicodePathExtraField)
            ze.getExtraField(UnicodePathExtraField.UPATH_ID);
    }

    private static void assertUnicodeName(final ZipArchiveEntry ze,
                                          final String expectedName,
                                          final String encoding)
        throws IOException {
        if (!expectedName.equals(ze.getName())) {
            final UnicodePathExtraField ucpf = findUniCodePath(ze);
            assertNotNull(ucpf);

            final ZipEncoding enc = ZipEncodingHelper.getZipEncoding(encoding);
            final ByteBuffer ne = enc.encode(ze.getName());

            final CRC32 crc = new CRC32();
            crc.update(ne.array(), ne.arrayOffset(),
                       ne.limit() - ne.position());

            assertEquals(crc.getValue(), ucpf.getNameCRC32());
            assertEquals(expectedName, new String(ucpf.getUnicodeName(),
                                                  CharsetNames.UTF_8));
        }
    }

    @Test
    public void testUtf8Interoperability() throws IOException {
        final File file1 = getFile("utf8-7zip-test.zip");
        final File file2 = getFile("utf8-winzip-test.zip");

        testFile(file1,CP437);
        testFile(file2,CP437);

    }

    private static void assertRawNameOfAcsiiTxt(final ZipArchiveEntry ze) {
        final byte[] b = ze.getRawName();
        assertNotNull(b);
        final int len = ASCII_TXT.length();
        assertEquals(len, b.length);
        for (int i = 0; i < len; i++) {
            assertEquals("Byte " + i, (byte) ASCII_TXT.charAt(i), b[i]);
        }
        assertNotSame(b, ze.getRawName());
    }
}

