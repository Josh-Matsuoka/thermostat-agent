/*
 * Copyright 2012-2016 Red Hat, Inc.
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

package com.redhat.thermostat.common.cli;

import jline.console.completer.StringsCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides cli/shell tab completion for various strings, ex. agentId, vmId.
 */
public class CompletionFinderTabCompleter implements TabCompleter {

    private CompletionFinder finder;

    public CompletionFinderTabCompleter(CompletionFinder finder) {
        this.finder = finder;
    }

    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        List<CompletionInfo> completions = filterCompletionsWithBuffer(finder.findCompletions(), buffer);

        // If there is only one it will be completed on the prompt line.
        // Then any additional information is unwanted there.
        List<String> results;
        if (completions.isEmpty()) {
            results = Collections.emptyList();
        } else if (completions.size() == 1) {
            results = getCompletions(completions);
        } else {
            results = getCompletionsWithUserVisibleText(completions);
        }

        return new StringsCompleter(results).complete(buffer, cursor, candidates);
    }

    private List<CompletionInfo> filterCompletionsWithBuffer(List<CompletionInfo> completions, String buffer) {
        List<CompletionInfo> result = new ArrayList<>();
        for (CompletionInfo completion : completions) {
            if (buffer == null || completion.getActualCompletion().startsWith(buffer)) {
                result.add(completion);
            }
        }
        return result;
    }

    private List<String> getCompletions(List<CompletionInfo> completions) {
        List<String> result = new ArrayList<>();
        for (CompletionInfo completion : completions) {
            result.add(completion.getActualCompletion() + " ");
        }
        return result;
    }

    private List<String> getCompletionsWithUserVisibleText(List<CompletionInfo> completions) {
        List<String> result = new ArrayList<>();
        for (CompletionInfo completion : completions) {
            result.add(completion.getCompletionWithUserVisibleText());
        }
        return result;
    }

}