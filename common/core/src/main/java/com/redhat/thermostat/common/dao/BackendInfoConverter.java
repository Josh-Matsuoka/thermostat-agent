/*
 * Copyright 2012 Red Hat, Inc.
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

package com.redhat.thermostat.common.dao;

import java.util.List;

import com.redhat.thermostat.common.model.BackendInformation;
import com.redhat.thermostat.common.storage.Chunk;

public class BackendInfoConverter {

    public Chunk toChunk(BackendInformation backendInfo) {
        Chunk chunk = new Chunk(BackendInfoDAO.CATEGORY, false);

        chunk.put(BackendInfoDAO.BACKEND_NAME, backendInfo.getName());
        chunk.put(BackendInfoDAO.BACKEND_DESCRIPTION, backendInfo.getDescription());
        chunk.put(BackendInfoDAO.IS_ACTIVE, backendInfo.isActive());
        List<Integer> pids = backendInfo.getPids();
        chunk.put(BackendInfoDAO.PIDS_TO_MONITOR, pids);
        chunk.put(BackendInfoDAO.SHOULD_MONITOR_NEW_PROCESSES, backendInfo.isObserveNewJvm());

        return chunk;
    }

    public BackendInformation fromChunk(Chunk parseFrom) {
        if (!parseFrom.getCategory().equals(BackendInfoDAO.CATEGORY)) {
            throw new IllegalArgumentException("chunk not a " + BackendInfoDAO.CATEGORY);
        }

        BackendInformation info = new BackendInformation();

        info.setName(parseFrom.get(BackendInfoDAO.BACKEND_NAME));
        info.setDescription(parseFrom.get(BackendInfoDAO.BACKEND_DESCRIPTION));
        info.setActive(parseFrom.get(BackendInfoDAO.IS_ACTIVE));
        info.setPids(parseFrom.get(BackendInfoDAO.PIDS_TO_MONITOR));
        info.setObserveNewJvm(parseFrom.get(BackendInfoDAO.SHOULD_MONITOR_NEW_PROCESSES));

        return info;
    }
}