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

package org.eclipse.sapphire.ui;

import org.eclipse.sapphire.Disposable;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.ListenerContext;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class SapphireCondition implements Disposable
{
    private ISapphirePart part;
    private boolean conditionState;
    private final ListenerContext listeners = new ListenerContext();
    
    public static SapphireCondition create( final ISapphirePart part,
                                            final Class<?> conditionClass,
                                            final String conditionParameter )
    {
        try
        {
            final SapphireCondition condition = (SapphireCondition) conditionClass.newInstance();
            condition.init( part, conditionParameter );
            return condition;
        }
        catch( Exception e )
        {
            Sapphire.service( LoggingService.class ).log( e );
        }
        
        return null;
    }
    
    public final void init( final ISapphirePart part,
                            final String parameter )
    {
        this.part = part;
        initCondition( part, parameter );
        updateConditionState();
    }

    protected void initCondition( final ISapphirePart part,
                                  final String parameter )
    {
    }
    
    public final ISapphirePart getPart()
    {
        return this.part;
    }
    
    protected abstract boolean evaluate();
    
    public final boolean getConditionState()
    {
        return this.conditionState;
    }

    protected final void updateConditionState()
    {
        final boolean newConditionState;
        
        try
        {
            newConditionState = evaluate();
        }
        catch( Exception e )
        {
            Sapphire.service( LoggingService.class ).log( e );
            return;
        }
        
        if( this.conditionState != newConditionState )
        {
            this.conditionState = newConditionState;
            this.listeners.broadcast( new Event() );
        }
    }
    
    @Override
    public void dispose()
    {
    }
    
    public final void attach( final Listener listener )
    {
        this.listeners.attach( listener );
    }
    
    public final void detach( final Listener listener )
    {
        this.listeners.detach( listener );
    }
    
}
