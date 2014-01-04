/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import java.util.List;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.ListenerContext;
import org.eclipse.sapphire.ui.def.ActionHandlerFactoryDef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class SapphireActionHandlerFactory
{
    private SapphireAction action;
    private final ListenerContext listeners = new ListenerContext();

    public void init( final SapphireAction action,
                      final ActionHandlerFactoryDef def )
    {
        this.action = action;
    }
    
    public final SapphireAction getAction()
    {
        return this.action;
    }
    
    public final ISapphirePart getPart()
    {
        return this.action.getPart();
    }
    
    public final String getContext()
    {
        return this.action.getContext();
    }
    
    public final Element getModelElement()
    {
        return getPart().getModelElement();
    }

    public abstract List<SapphireActionHandler> create();
    
    public final boolean attach( final Listener listener )
    {
        return this.listeners.attach( listener );
    }
    
    public final boolean detach( final Listener listener )
    {
        return this.listeners.detach( listener );
    }
    
    protected final void broadcast( final Event event )
    {
        this.listeners.broadcast( event );
    }
    
    public void dispose()
    {
    }
    
}