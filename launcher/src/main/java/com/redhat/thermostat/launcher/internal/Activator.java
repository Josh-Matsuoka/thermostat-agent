/*
 * Copyright 2012-2017 Red Hat, Inc.
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

package com.redhat.thermostat.launcher.internal;

import java.io.File;
import java.io.IOException;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.redhat.thermostat.common.ExitStatus;
import com.redhat.thermostat.common.MultipleServiceTracker;
import com.redhat.thermostat.common.MultipleServiceTracker.Action;
import com.redhat.thermostat.common.MultipleServiceTracker.DependencyProvider;
import com.redhat.thermostat.common.cli.CommandContextFactory;
import com.redhat.thermostat.common.cli.CommandRegistry;
import com.redhat.thermostat.common.cli.CommandRegistryImpl;
import com.redhat.thermostat.common.config.experimental.ConfigurationInfoSource;

import com.redhat.thermostat.launcher.BundleManager;
import com.redhat.thermostat.launcher.Launcher;

import com.redhat.thermostat.shared.config.CommonPaths;

public class Activator implements BundleActivator {

    @SuppressWarnings({ "rawtypes" })
    class RegisterLauncherAction implements Action {

        private ServiceRegistration launcherReg;
        private ServiceRegistration bundleManReg;
        private ServiceRegistration cmdInfoReg;
        private ServiceRegistration exitStatusReg;
        private ServiceRegistration pluginConfReg;
        private ServiceRegistration commandGroupMetaReg;
        private BundleContext context;

        RegisterLauncherAction(BundleContext context) {
            this.context = context;
        }

        @Override
        public void dependenciesAvailable(DependencyProvider services) {
            CommonPaths paths = services.get(CommonPaths.class);

            String commandsDir = new File(paths.getSystemConfigurationDirectory(), "commands").toString();
            CommandInfoSource builtInCommandSource =
                    new BuiltInCommandInfoSource(commandsDir, paths.getSystemLibRoot().toString());
            PluginInfoSource pluginSource = new PluginInfoSource(
                            paths.getSystemLibRoot().toString(),
                            paths.getSystemPluginRoot().toString(),
                            paths.getUserPluginRoot().toString(),
                            paths.getSystemPluginConfigurationDirectory().toString(),
                            paths.getUserPluginConfigurationDirectory().toString());

            ConfigurationInfoSource configurations = pluginSource;
            pluginConfReg = context.registerService(ConfigurationInfoSource.class, configurations, null);

            commandGroupMetaReg = context.registerService(CommandGroupMetadataSource.class, pluginSource, null);

            CommandInfoSource commands = new CompoundCommandInfoSource(builtInCommandSource, pluginSource);
            cmdInfoReg = context.registerService(CommandInfoSource.class, commands, null);

            BundleManager bundleService = null;
            try {
                bundleService = new BundleManagerImpl(paths);
            } catch (IOException e) {
                throw new RuntimeException("Could not initialize launcher.", e);
            }

            // Register Launcher service since FrameworkProvider is waiting for it blockingly.
            LauncherImpl launcher = new LauncherImpl(context, new CommandContextFactory(context),
                    bundleService, commands, paths);
            launcherReg = context.registerService(Launcher.class.getName(), launcher, null);
            bundleManReg = context.registerService(BundleManager.class, bundleService, null);
            ExitStatus exitStatus = new ExitStatusImpl(ExitStatus.EXIT_SUCCESS);
            exitStatusReg = context.registerService(ExitStatus.class, exitStatus, null);
        }

        @Override
        public void dependenciesUnavailable() {
            // Keyring or CommonPaths are gone, remove launcher, et. al. as well
            launcherReg.unregister();
            bundleManReg.unregister();
            cmdInfoReg.unregister();
            exitStatusReg.unregister();
            pluginConfReg.unregister();
            commandGroupMetaReg.unregister();
        }

    }

    private MultipleServiceTracker launcherDepsTracker;

    private CommandRegistry registry;

    private MultipleServiceTracker commandInfoSourceTracker;

    @SuppressWarnings({ "rawtypes" })
    @Override
    public void start(final BundleContext context) throws Exception {
        Class[] launcherDeps = new Class[]{
            CommonPaths.class,
        };
        registry = new CommandRegistryImpl(context);
        RegisterLauncherAction registerLauncherAction = new RegisterLauncherAction(context);
        launcherDepsTracker = new MultipleServiceTracker(context, launcherDeps,
                registerLauncherAction);
        launcherDepsTracker.open();

        final HelpCommand helpCommand = new HelpCommand();
        final Class<?>[] helpCommandClasses = new Class<?>[] {
                CommandInfoSource.class,
                CommandGroupMetadataSource.class
        };
        commandInfoSourceTracker = new MultipleServiceTracker(context, helpCommandClasses, new Action() {
            @Override
            public void dependenciesAvailable(DependencyProvider services) {
                CommandInfoSource infoSource = services.get(CommandInfoSource.class);
                helpCommand.setCommandInfoSource(infoSource);

                CommandGroupMetadataSource commandGroupMetadataSource = services.get(CommandGroupMetadataSource.class);
                helpCommand.setCommandGroupMetadataSource(commandGroupMetadataSource);
            }

            @Override
            public void dependenciesUnavailable() {
                helpCommand.setCommandInfoSource(null);
                helpCommand.setCommandGroupMetadataSource(null);
            }
        });
        commandInfoSourceTracker.open();

        registry.registerCommand("help", helpCommand);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (launcherDepsTracker != null) {
            launcherDepsTracker.close();
        }
        if (commandInfoSourceTracker != null) {
            commandInfoSourceTracker.close();
        }
        registry.unregisterCommands();
    }
}

