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

package org.eclipse.sapphire.ui.forms;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sapphire.DisposeEvent;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PropertyEvent;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class JumpActionHandler extends PropertyEditorActionHandler
{
    private List<String> dependencies;
    
    @Override
    public void init( final SapphireAction action,
                      final ActionHandlerDef def )
    {
        super.init( action, def );
        
        this.dependencies = new ArrayList<String>();
        initDependencies( this.dependencies );
        
        final Listener listener = new FilteredListener<PropertyEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyEvent event)
            {
                refreshEnablementState();
            }
        };
        
        final Element element = getModelElement();
        
        for( String dependency : this.dependencies )
        {
            element.attach( listener, dependency );
        }
        
        refreshEnablementState();
        
        attach
        (
            new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof DisposeEvent )
                    {
                        final Element element = getModelElement();
                        
                        for( String dependency : JumpActionHandler.this.dependencies )
                        {
                            element.detach( listener, dependency );
                        }
                    }
                }
            }
        );
    }

    protected void initDependencies( final List<String> dependencies )
    {
        this.dependencies.add( property().name() );
    }
   
}