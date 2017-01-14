/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.commons.compress.archivers.zip;

import static org.apache.commons.compress.AbstractTestCase.getFile;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;

import org.apache.commons.compress.utils.IOUtils;
import org.junit.Test;

public class ExplodeSupportTest {

    private void testArchiveWithImplodeCompression(final String filename, final String entryName) throws IOException {
        final ZipFile zip = new ZipFile(new File(filename));
        final ZipArchiveEntry entry = zip.getEntries().nextElement();
        assertEquals("entry name", entryName, entry.getName());
        assertTrue("entry can't be read", zip.canReadEntryData(entry));
        assertEquals("method", ZipMethod.IMPLODING.getCode(), entry.getMethod());

        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final CheckedOutputStream out = new CheckedOutputStream(bout, new CRC32());
        IOUtils.copy(zip.getInputStream(entry), out);

        out.flush();

        assertEquals("CRC32", entry.getCrc(), out.getChecksum().getValue());
        zip.close();
    }

    @Test
    public void testArchiveWithImplodeCompression4K2Trees() throws IOException {
        testArchiveWithImplodeCompression(getFile("imploding-4Kdict-2trees.zip").getPath(), "HEADER.TXT");
    }

    @Test
    public void testArchiveWithImplodeCompression8K3Trees() throws IOException {
        testArchiveWithImplodeCompression(getFile("imploding-8Kdict-3trees.zip").getPath(), "LICENSE.TXT");
    }

    @Test
    public void testTikaTestArchive() throws IOException {
        testArchiveWithImplodeCompression(getFile("moby-imploded.zip").getPath(), "README");
    }
}
