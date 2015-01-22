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

package org.eclipse.sapphire;

/**
 * Used by {@link ListenerContext} to deliver an {@link Event} to a {@link Listener} through a {@link JobQueue}.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class EventDeliveryJob implements Runnable
{
    private final Listener listener;
    private final Event event;
     
    EventDeliveryJob( final Listener listener, final Event event )
    {
        this.listener = listener;
        this.event = event;
    }
    
    /**
     * Returns the listener to which the event is to be delivered.
     */
    
    public Listener listener()
    {
        return this.listener;
    }
    
    /**
     * Returns the event which is to be delivered to the listener.
     */
    
    public Event event()
    {
        return this.event;
    }

    @Override
    public void run()
    {
        this.listener.handle( this.event );
    }
}