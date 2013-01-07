/*
 * Copyright 2013 Red Hat, Inc.
 *
 * This file is part of Thermostat.
 *
 * Thermostat is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your
 * option) any later version.
 *
 * Thermostat is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Thermostat; see the file COPYING.  If not see
 * <http://www.gnu.org/licenses/>.
 *
 * Linking this code with other modules is making a combined work
 * based on this code.  Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this code give
 * you permission to link this code with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions
 * of the license of that module.  An independent module is a module
 * which is not derived from or based on this code.  If you modify
 * this code, you may extend this exception to your version of the
 * library, but you are not obligated to do so.  If you do not wish
 * to do so, delete this exception statement from your version.
 */

package com.redhat.thermostat.distribution;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import expectj.ExpectJException;
import expectj.Spawn;
import expectj.TimeoutException;

public class StorageConnectionTest extends IntegrationTest {

    @BeforeClass
    public static void setUpOnce() throws IOException, TimeoutException, ExpectJException {
        Spawn storage = spawnThermostat("storage", "--start");
        storage.expect("pid:");
        storage.expectClose();

        assertNoExceptions(storage.getCurrentStandardOutContents(), storage.getCurrentStandardErrContents());
    }

    @AfterClass
    public static void tearDownOnce() throws IOException, TimeoutException, ExpectJException {
        Spawn storage = spawnThermostat("storage", "--stop");
        storage.expect("server shutdown complete");
        storage.expectClose();

        assertNoExceptions(storage.getCurrentStandardOutContents(), storage.getCurrentStandardErrContents());
    }

    @Test
    public void testConnect() throws ExpectJException, TimeoutException, IOException {
        Spawn shell = spawnThermostat("shell");

        shell.expect(SHELL_PROMPT);
        shell.send("connect -d mongodb://127.0.0.1:27518\n");
        shell.expect(SHELL_PROMPT);
        shell.send("exit\n");
        shell.expectClose();

        assertCommandIsFound(shell.getCurrentStandardOutContents(), shell.getCurrentStandardErrContents());
        assertNoExceptions(shell.getCurrentStandardOutContents(), shell.getCurrentStandardErrContents());
    }

    @Test
    public void testDisconnectWithoutConnecting() throws ExpectJException, TimeoutException, IOException {
        Spawn shell = spawnThermostat("shell");

        shell.expect(SHELL_PROMPT);
        shell.send("disconnect\n");
        shell.expect(SHELL_PROMPT);
        shell.send("exit\n");
        shell.expectClose();

        assertCommandIsFound(shell.getCurrentStandardOutContents(), shell.getCurrentStandardErrContents());
        assertNoExceptions(shell.getCurrentStandardOutContents(), shell.getCurrentStandardErrContents());
    }

    @Test
    public void testConnectAndDisconnectInShell() throws IOException, TimeoutException, ExpectJException {
        Spawn shell = spawnThermostat("shell");

        shell.expect(SHELL_PROMPT);
        shell.send("connect -d mongodb://127.0.0.1:27518\n");
        shell.expect(SHELL_PROMPT);
        shell.send("disconnect\n");
        shell.send("exit\n");
        shell.expectClose();

        assertNoExceptions(shell.getCurrentStandardOutContents(), shell.getCurrentStandardErrContents());
    }

    // TODO add a test to make sure connect/disconnect is not visible outside the shell
}