/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [bugzilla 329114] rewrite context help binding feature
 ******************************************************************************/

package org.eclipse.sapphire.ui.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.sapphire.Disposable;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionGroup;

/**
 * Temporary class. Do not reference or otherwise use.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

/*
 * This class duplicates code found in SapphirePart class. It is to be eliminated when ISapphirePart
 * interface is eliminated and all parts extend SapphirePart.
 */

public final class SapphireActionManager implements Disposable
{
    private final ISapphirePart part;
    private final Set<String> contexts;
    private Map<String,SapphireActionGroup> actions;
    
    public SapphireActionManager( final ISapphirePart part,
                                   final Set<String> contexts )
    {
        this.part = part;
        this.contexts = contexts;
    }
    
    public Set<String> getActionContexts()
    {
        return this.contexts;
    }
    
    public String getMainActionContext()
    {
        final Set<String> contexts = getActionContexts();
        
        if( ! contexts.isEmpty() )
        {
            return contexts.iterator().next();
        }
        
        return null;
    }
    
    public SapphireActionGroup getActions()
    {
        final String context = getMainActionContext();
        
        if( context != null )
        {
            return getActions( context );
        }
        
        return null;
    }
    
    public final SapphireActionGroup getActions( final String context )
    {
        if( this.actions == null )
        {
            this.actions = new HashMap<String,SapphireActionGroup>();
            
            for( String ctxt : getActionContexts() )
            {
                final SapphireActionGroup actionsForContext = new SapphireActionGroup( this.part, ctxt );
                this.actions.put( ctxt.toLowerCase(), actionsForContext );
            }
        }
        
        return this.actions.get( context.toLowerCase() );
    }
    
    public final SapphireAction getAction( final String id )
    {
        for( final String context : getActionContexts() )
        {
            final SapphireAction action = getActions( context ).getAction( id );
            
            if( action != null )
            {
                return action;
            }
        }
        
        if( this.part.parent() != null )
        {
            return this.part.parent().getAction( id );
        }
        
        return null;
    }
    
    @Override
    public void dispose()
    {
        if( this.actions != null )
        {
            for( SapphireActionGroup actionsForContext : this.actions.values() )
            {
                actionsForContext.dispose();
            }
        }
    }
    
}
