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

import static org.apache.commons.compress.AbstractTestCase.getFile;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

/**
 * JUnit testcase for a multi-volume zip file.
 *
 * Some tools (like 7-zip) allow users to split a large archives into 'volumes'
 * with a given size to fit them into multiple cds, usb drives, or emails with
 * an attachment size limit. It's basically the same file split into chunks of
 * exactly 65536 bytes length. Concatenating volumes yields exactly the original
 * file. There is no mechanism in the ZIP algorithm to accommodate for this.
 * Before commons-compress used to enter an infinite loop on the last entry for
 * such a file. This test is intended to prove that this error doesn't occur
 * anymore. All entries but the last one are returned correctly, the last entry
 * yields an exception.
 *
 */
public class Maven221MultiVolumeTest {

    @Test(expected=IOException.class)
    public void testRead7ZipMultiVolumeArchiveForFile() throws IOException {
        final File file = getFile("apache-maven-2.2.1.zip.001");
        new ZipFile(file);
    }
}
