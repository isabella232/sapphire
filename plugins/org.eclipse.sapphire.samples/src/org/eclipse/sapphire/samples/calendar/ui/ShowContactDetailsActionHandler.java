/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.samples.calendar.ui;

import org.eclipse.sapphire.DisposeEvent;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ModelPropertyListener;
import org.eclipse.sapphire.samples.calendar.integrated.IAttendee;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.def.ISapphireActionHandlerDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ShowContactDetailsActionHandler extends SapphireActionHandler
{
    @Override
    public void init( final SapphireAction action,
                      final ISapphireActionHandlerDef def )
    {
        super.init( action, def );
        
        final IModelElement element = getModelElement();
        
        final ModelPropertyListener listener = new ModelPropertyListener()
        {
            @Override
            public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
            {
                refreshEnablementState();
            }
        };
        
        element.addListener( listener, IAttendee.PROP_IN_CONTACTS_DATABASE.getName() );
        
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
                        element.removeListener( listener, IAttendee.PROP_IN_CONTACTS_DATABASE.getName() );
                    }
                }
            }
        );
    }
    
    protected void refreshEnablementState()
    {
        setEnabled( ( (IAttendee) getModelElement() ).isInContactsDatabase().getContent() );
    }

    @Override
    protected Object run( SapphireRenderingContext context )
    {
        final ISapphirePart part = getPart();
        final CalendarEditor editor = part.nearest( CalendarEditor.class );
        final IModelElement modelElement = part.getModelElement();
        
        ContactDetailsJumpHandler.jump( editor, modelElement );
        
        return null;
    }
    
}
