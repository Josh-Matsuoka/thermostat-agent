/*
 * Copyright 2012-2015 Red Hat, Inc.
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

package com.redhat.thermostat.storage.core;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.storage.model.TimeStampedPojo;

import static com.redhat.thermostat.common.utils.IteratorUtils.head;

public class HostBoundaryPojoGetter<T extends TimeStampedPojo> {

    // QUERY %s WHERE 'agentId' = ?s AND \
    //                        SORT 'timeStamp' DSC  \
    //                        LIMIT 1
    public static final String DESC_NEWEST_HOST_STAT = "QUERY %s " +
            "WHERE '" + Key.AGENT_ID.getName() + "' = ?s " +
            "SORT '" + Key.TIMESTAMP.getName() + "' DSC " +
            "LIMIT 1";

    // QUERY %s WHERE 'agentId' = ?s AND \
    //                        SORT 'timeStamp' ASC  \
    //                        LIMIT 1
    public static final String DESC_OLDEST_HOST_STAT = "QUERY %s " +
            "WHERE '" + Key.AGENT_ID.getName() + "' = ?s " +
            "SORT '" + Key.TIMESTAMP.getName() + "' ASC " +
            "LIMIT 1";

    private static final Logger logger = LoggingUtils.getLogger(HostBoundaryPojoGetter.class);

    private final Storage storage;
    private final Category<T> cat;
    private final String queryNewest;
    private final String queryOldest;

    public HostBoundaryPojoGetter(Storage storage, Category<T> cat) {
        this.storage = storage;
        this.cat = cat;
        this.queryNewest = String.format(DESC_NEWEST_HOST_STAT, cat.getName());
        this.queryOldest= String.format(DESC_OLDEST_HOST_STAT, cat.getName());
    }

    public T getNewestStat(HostRef ref) {
        return runAgentIdQuery(ref, queryNewest);
    }

    public T getOldestStat(HostRef ref) {
        return runAgentIdQuery(ref, queryOldest);
    }

    private T runAgentIdQuery(final HostRef ref, final String descriptor) {
        StatementDescriptor<T> desc = new StatementDescriptor<>(cat, descriptor);
        PreparedStatement<T> prepared;
        try {
            prepared = storage.prepareStatement(desc);
            prepared.setString(0, ref.getAgentId());
            Cursor<T> cursor = prepared.executeQuery();
            return head(cursor);
        } catch (DescriptorParsingException e) {
            logger.log(Level.SEVERE, "Preparing stmt '" + desc + "' failed!", e);
            return null;
        } catch (StatementExecutionException e) {
            logger.log(Level.SEVERE, "Executing stmt '" + desc + "' failed!", e);
            return null;
        }
    }

    //Package private for testing
    String getNewestQueryDesc() {
        return queryNewest;
    }

    String getOldestQueryDesc() {
        return queryOldest;
    }
}
