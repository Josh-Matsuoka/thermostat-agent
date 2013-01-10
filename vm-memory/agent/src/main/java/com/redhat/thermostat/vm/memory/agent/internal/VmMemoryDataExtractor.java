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

package com.redhat.thermostat.vm.memory.agent.internal;

import sun.jvmstat.monitor.Monitor;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredVm;

import com.redhat.thermostat.storage.model.VmMemoryStat.Generation;

/**
 * A helper class to provide type-safe access to commonly used jvmstat monitors
 * <p>
 * Implementation details: For local vms, jvmstat uses a ByteBuffer
 * corresponding to mmap()ed hsperfdata file. The hsperfdata file is updated
 * asynchronously by the vm that created the file. The polling that jvmstat api
 * provides is merely an abstraction over this (possibly always up-to-date)
 * ByteBuffer. So the data this class extracts is as current as possible, and
 * does not correspond to when the jvmstat update events fired.
 */
public class VmMemoryDataExtractor {

    /*
     * Note, there may be a performance issue to consider here. We have a lot of
     * string constants. When we start adding some of the more heavyweight
     * features, and running into CPU issues this may need to be reconsidered in
     * order to avoid the String pool overhead. See also:
     * http://docs.oracle.com/javase/6/docs/api/java/lang/String.html#intern()
     */

    private final MonitoredVm vm;

    public VmMemoryDataExtractor(MonitoredVm vm) {
        this.vm = vm;
    }
    
    public long getTotalGcGenerations() throws MonitorException {
        return (Long) vm.findByName("sun.gc.policy.generations").getValue();
    }

    public String getGenerationName(long generation) throws MonitorException {
        return (String) vm.findByName("sun.gc.generation." + generation + ".name").getValue();
    }

    public long getGenerationCapacity(long generation) throws MonitorException {
        return (Long) vm.findByName("sun.gc.generation." + generation + ".capacity").getValue();
    }

    public long getGenerationMaxCapacity(long generation) throws MonitorException {
        return (Long) vm.findByName("sun.gc.generation." + generation + ".maxCapacity").getValue();
    }

    public String getGenerationCollector(long generation) throws MonitorException {
        // this is just re-implementing getCollectorName()
        // TODO check generation number and collector number are always associated
        Monitor m = vm.findByName("sun.gc.collector." + generation + ".name");
        if (m == null) {
            return Generation.COLLECTOR_NONE;
        }
        return (String) m.getValue();
    }

    public long getTotalSpaces(long generation) throws MonitorException {
        return (Long) vm.findByName("sun.gc.generation." + generation + ".spaces").getValue();
    }

    public String getSpaceName(long generation, long space) throws MonitorException {
        return (String) vm.findByName("sun.gc.generation." + generation + ".space." + space + ".name").getValue();
    }

    public long getSpaceCapacity(long generation, long space) throws MonitorException {
        return (Long) vm.findByName("sun.gc.generation." + generation + ".space." + space + ".capacity").getValue();
    }

    public long getSpaceMaxCapacity(long generation, long space) throws MonitorException {
        return (Long) vm.findByName("sun.gc.generation." + generation + ".space." + space + ".maxCapacity").getValue();
    }

    public long getSpaceUsed(long generation, long space) throws MonitorException {
        return (Long) vm.findByName("sun.gc.generation." + generation + ".space." + space + ".used").getValue();
    }

}