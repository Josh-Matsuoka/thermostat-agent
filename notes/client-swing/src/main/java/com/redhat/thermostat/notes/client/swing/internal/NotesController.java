/*
 * Copyright 2012-2014 Red Hat, Inc.
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

package com.redhat.thermostat.notes.client.swing.internal;

import com.redhat.thermostat.client.core.controllers.InformationServiceController;
import com.redhat.thermostat.client.core.views.UIComponent;
import com.redhat.thermostat.common.ActionEvent;
import com.redhat.thermostat.common.ActionListener;
import com.redhat.thermostat.common.Clock;
import com.redhat.thermostat.shared.locale.LocalizedString;
import com.redhat.thermostat.shared.locale.Translate;
import com.redhat.thermostat.storage.core.Ref;

public abstract class NotesController<T extends Ref> implements InformationServiceController<T> {

    protected static final Translate<LocaleResources> translator = LocaleResources.createLocalizer();

    protected NotesView view;
    protected Clock clock;

    public NotesController(Clock clock, NotesView view) {
        this.clock = clock;
        this.view = view;

        this.view.getNotifier().addActionListener(new ActionListener<NotesView.Action>() {
            @Override
            public void actionPerformed(ActionEvent<NotesView.Action> actionEvent) {
                switch(actionEvent.getActionId()) {
                    /* remote-storage operations */
                    case REMOTE_REFRESH: {
                        remoteGetNotesFromStorage();
                        break;
                    }
                    case REMOTE_SAVE: {
                        remoteSaveNotes();
                        break;
                    }
                    /* local operations */
                    case LOCAL_ADD: {
                        localAddNewNote();
                        break;
                    }
                    case LOCAL_SAVE: {
                        String noteId = /* tag = */ (String) actionEvent.getPayload();
                        localSaveNote(noteId);
                        break;
                    }
                    case LOCAL_DELETE: {
                        String noteId = /* tag = */ (String) actionEvent.getPayload();
                        localDeleteNote(noteId);
                        break;
                    }
                }
            }
        });
    }

    /** Add a new note */
    protected abstract void localAddNewNote();

    /** Update the local cache of notes to match what's in the view */
    protected abstract void localSaveNote(String noteId);

    /** Delete a note */
    protected abstract void localDeleteNote(String noteId);

    /** Update the view to match what's in the local cache */
    protected abstract void localUpdateNotesInView();

    protected abstract void remoteSaveNotes();
    protected abstract void remoteGetNotesFromStorage();

    @Override
    public UIComponent getView() {
        return view;
    }

    @Override
    public LocalizedString getLocalizedName() {
        return translator.localize(LocaleResources.TAB_NAME);
    }

}
