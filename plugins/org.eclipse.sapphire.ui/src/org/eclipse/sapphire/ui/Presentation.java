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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sapphire.Disposable;
import org.eclipse.sapphire.Listener;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class Presentation implements Disposable
{
    private SapphirePart part;
    private Presentation parent;
    private List<Listener> partListeners;
    
    public Presentation( final SapphirePart part, final Presentation parent )
    {
        if( part == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.part = part;
        this.parent = parent;
    }
    
    public SapphirePart part()
    {
        return this.part;
    }
    
    public Presentation parent()
    {
        return this.parent;
    }
    
    public abstract void render();
    
    protected final void attachPartListener( final Listener listener )
    {
        if( listener == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( this.partListeners == null )
        {
            this.partListeners = new ArrayList<Listener>( 1 );
        }
        
        this.partListeners.add( listener );
        this.part.attach( listener );
    }
    
    @Override
    public void dispose()
    {
        this.parent = null;
        
        if( this.partListeners != null )
        {
            for( final Listener listener : this.partListeners )
            {
                this.part.detach( listener );
            }
            
            this.partListeners = null;
        }
        
        this.part = null;
    }
    
    public final boolean disposed()
    {
        return ( this.part == null );
    }

}
