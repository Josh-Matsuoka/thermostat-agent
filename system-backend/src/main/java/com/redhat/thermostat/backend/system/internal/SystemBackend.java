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

package com.redhat.thermostat.backend.system.internal;

import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.redhat.thermostat.agent.VmBlacklist;
import com.redhat.thermostat.backend.BaseBackend;
import com.redhat.thermostat.backend.system.internal.models.InfoBuilderFactory;
import com.redhat.thermostat.common.Version;
import com.redhat.thermostat.common.portability.ProcessUserInfoBuilder;
import com.redhat.thermostat.common.portability.UserNameUtil;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.storage.core.WriterID;
import com.redhat.thermostat.storage.dao.NetworkInterfaceInfoDAO;
import com.redhat.thermostat.storage.dao.VmInfoDAO;
import com.redhat.thermostat.storage.model.NetworkInterfaceInfo;

import sun.jvmstat.monitor.HostIdentifier;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;

public class SystemBackend extends BaseBackend {

    private static final Logger logger = LoggingUtils.getLogger(SystemBackend.class);

    private NetworkInterfaceInfoDAO networkInterfaces;
    private VmInfoDAO vmInfoDAO;

    private long procCheckInterval = 1000; // TODO make this configurable.

    private Timer timer = null;

    private HostIdentifier hostId = null;
    private MonitoredHost host = null;
    private JvmStatHostListener hostListener = null;

    private final NetworkInfoBuilder networkInfoBuilder;
    private final ProcessUserInfoBuilder userInfoBuilder;
    private final VmStatusChangeNotifier notifier;
    private final WriterID writerId;
    private final VmBlacklist blacklist;

    public SystemBackend(NetworkInterfaceInfoDAO netInfoDAO, VmInfoDAO vmInfoDAO,
            Version version, VmStatusChangeNotifier notifier, UserNameUtil userNameUtil, WriterID writerId,
            VmBlacklist blacklist) {
        super("System Backend",
                "Gathers basic information from the system",
                "Red Hat, Inc.",
                version.getVersionNumber(), true);
        this.networkInterfaces = netInfoDAO;
        this.vmInfoDAO = vmInfoDAO;
        this.notifier = notifier;
        this.writerId = writerId;
        this.blacklist = blacklist;

        userInfoBuilder = InfoBuilderFactory.INSTANCE.createProcessUserInfoBuilder(userNameUtil);
        networkInfoBuilder = new NetworkInfoBuilder(writerId);
    }

    @Override
    public synchronized boolean activate() {
        if (timer != null) {
            return true;
        }

        if (!getObserveNewJvm()) {
            logger.fine("not monitoring new vms");
        }

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (NetworkInterfaceInfo info: networkInfoBuilder.build()) {
                    networkInterfaces.putNetworkInterfaceInfo(info);
                }
            }
        }, 0, procCheckInterval);

        try {
            hostId = new HostIdentifier((String) null);
            hostListener = new JvmStatHostListener(vmInfoDAO, notifier, 
                    userInfoBuilder, writerId, blacklist);
            host = MonitoredHost.getMonitoredHost(hostId);
            host.addHostListener(hostListener);
        } catch (MonitorException me) {
            logger.log(Level.WARNING, "problems with connecting jvmstat to local machine", me);
        } catch (URISyntaxException use) {
            logger.log(Level.WARNING, "problems with connecting jvmstat to local machine", use);
        }

        return true;
    }

    @Override
    public synchronized boolean deactivate() {
        if (timer == null) {
            return true;
        }

        timer.cancel();
        timer = null;

        try {
            host.removeHostListener(hostListener);
        } catch (MonitorException me) {
            logger.log(Level.INFO, "something went wrong in jvmstat's listening to this host");
        }
        host = null;
        hostId = null;

        return true;
    }

    @Override
    public synchronized boolean isActive() {
        return (timer != null);
    }

    @Override
    public int getOrderValue() {
        return ORDER_DEFAULT_GROUP;
    }
}

