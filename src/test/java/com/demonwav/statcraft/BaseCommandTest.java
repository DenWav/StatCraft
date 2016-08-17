/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft;

import com.demonwav.statcraft.commands.BaseCommand;
import com.demonwav.statcraft.commands.CommandAlreadyDefinedException;
import com.demonwav.statcraft.commands.sc.SCTemplate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@PrepareForTest({StatCraft.class, SCTemplate.class})
@RunWith(PowerMockRunner.class)
public class BaseCommandTest {

    private StatCraft plugin = PowerMockito.mock(StatCraft.class);
    private SCTemplate template = PowerMockito.mock(SCTemplate.class);

    @Test(expected = CommandAlreadyDefinedException.class)
    public void testConflictingCommands() {
        BaseCommand baseCommand = new BaseCommand(plugin);
        baseCommand.registerCommand("one", template);
        baseCommand.registerCommand("one", template);
    }

    @Test
    public void testOneCommand() {
        BaseCommand baseCommand = new BaseCommand(plugin);
        baseCommand.registerCommand("one", template);
    }

    @Test
    public void test100000Commands() {
        BaseCommand baseCommand = new BaseCommand(plugin);
        for (int i = 0; i < 100000; i++) {
            baseCommand.registerCommand(String.valueOf(i), template);
        }
    }

    @Test(expected = CommandAlreadyDefinedException.class)
    public void test100000CommandsWithOneConflict() {
        BaseCommand baseCommand = new BaseCommand(plugin);
        int j = 0;
        for (int i = 0; i < 100000; i++) {
            baseCommand.registerCommand(String.valueOf(i), template);
            j = i;
        }
        baseCommand.registerCommand(String.valueOf(j), template);
    }
}
