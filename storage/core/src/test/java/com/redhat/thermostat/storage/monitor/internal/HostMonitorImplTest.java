/*
 * Copyright 2012, 2013 Red Hat, Inc.
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

package com.redhat.thermostat.storage.monitor.internal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.redhat.thermostat.common.ActionListener;
import com.redhat.thermostat.common.ActionNotifier;
import com.redhat.thermostat.common.Pair;
import com.redhat.thermostat.common.Timer;
import com.redhat.thermostat.common.TimerFactory;
import com.redhat.thermostat.storage.core.HostRef;
import com.redhat.thermostat.storage.dao.VmInfoDAO;
import com.redhat.thermostat.storage.monitor.HostMonitor;

public class HostMonitorImplTest {

    private VmInfoDAO vmDao;
    private TimerFactory timerFactory;
    
    @Before
    public void setup() {
        vmDao = mock(VmInfoDAO.class);
        timerFactory = mock(TimerFactory.class);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void test() {
        ActionListener<HostMonitor.Action> listener1 = mock(ActionListener.class);
        ActionListener<HostMonitor.Action> listener2 = mock(ActionListener.class);

        Timer timer1 = mock(Timer.class);
        when(timerFactory.createTimer()).thenReturn(timer1);
        
        HostRef host1 = new HostRef("0", "0");

        HostMonitor monitor = new HostMonitorImpl(timerFactory, vmDao);
        Map<HostRef, Pair<Timer, ActionNotifier<HostMonitor.Action>>> listeners =
                ((HostMonitorImpl) monitor).getListeners();
        assertTrue(listeners.isEmpty());
        
        monitor.addHostChangeListener(host1, listener1);
        
        assertEquals(1, listeners.size());

        verify(timer1, times(1)).setTimeUnit(TimeUnit.MILLISECONDS);
        verify(timer1, times(1)).setDelay(HostMonitorImpl.DELAY);
        verify(timer1, times(1)).setSchedulingType(Timer.SchedulingType.FIXED_RATE);
        verify(timer1, times(1)).start();

        verify(timer1).setAction(any(HostMonitorAction.class));
        
        monitor.addHostChangeListener(host1, listener2);
        
        assertEquals(1, listeners.size());

        verify(timer1, times(1)).setTimeUnit(TimeUnit.MILLISECONDS);
        verify(timer1, times(1)).setDelay(HostMonitorImpl.DELAY);
        verify(timer1, times(1)).start();
        verify(timer1, times(1)).setSchedulingType(Timer.SchedulingType.FIXED_RATE);
        
        verify(timer1).setAction(any(HostMonitorAction.class));
        
        monitor.removeHostChangeListener(host1, listener1);
        verify(timer1, times(0)).stop();
        
        assertEquals(1, listeners.size());
        monitor.removeHostChangeListener(host1, listener2);

        assertTrue(listeners.isEmpty());
        verify(timer1, times(1)).stop();
        
        HostRef host2 = new HostRef("1", "1");

        monitor.addHostChangeListener(host1, listener1);
        monitor.addHostChangeListener(host2, listener2);
    }

}