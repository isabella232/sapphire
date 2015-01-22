/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class EventLog extends Listener
{
    private final List<Event> events = new CopyOnWriteArrayList<Event>();
    private final List<Event> eventsReadOnly = Collections.unmodifiableList( this.events );
    
    @Override
    public synchronized void handle( final Event event )
    {
        this.events.add( event );
    }
    
    public synchronized List<Event> events()
    {
        return this.eventsReadOnly;
    }
    
    public synchronized Event event( final int index )
    {
        return this.events.get( index );
    }
    
    public synchronized int size()
    {
        return this.events.size();
    }
    
    public synchronized void clear()
    {
        this.events.clear();
    }
    
}
