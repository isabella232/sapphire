/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class EnablementService

    extends ModelPropertyService
    
{
    private boolean state = false;
    
    @Override
    public final void init( final IModelElement element,
                            final ModelProperty property,
                            final String[] params )
    {
        super.init( element, property, params );
        
        initEnablementService( element, property, params );
        
        refresh( false );
    }

    protected void initEnablementService( final IModelElement element,
                                          final ModelProperty property,
                                          final String[] params )
    {
    }
    
    public boolean state()
    {
        return this.state;
    }
    
    protected abstract boolean compute();
    
    protected final void refresh()
    {
        refresh( true );
    }
    
    protected final void refresh( final boolean notifyListeners )
    {
        final boolean newState = compute();
        
        if( this.state != newState )
        {
            this.state = newState;
            
            if( notifyListeners )
            {
                notifyListeners( new StateChangedEvent( this ) );
            }
        }
    }
    
    public static class StateChangedEvent extends Event
    {
        public StateChangedEvent( final ModelService service )
        {
            super( service );
        }
    }
    
}
