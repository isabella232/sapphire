/******************************************************************************
 * Copyright (c) 2013 Oracle
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
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.PropertyContentEvent;
import org.eclipse.sapphire.samples.calendar.integrated.IAttendee;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ShowContactDetailsActionHandler extends SapphireActionHandler
{
    @Override
    public void init( final SapphireAction action,
                      final ActionHandlerDef def )
    {
        super.init( action, def );
        
        final IModelElement element = getModelElement();
        
        final Listener listener = new FilteredListener<PropertyContentEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyContentEvent event )
            {
                refreshEnablementState();
            }
        };
        
        element.attach( listener, IAttendee.PROP_IN_CONTACTS_DATABASE.getName() );
        
        refreshEnablementState();
        
        attach
        (
            new FilteredListener<DisposeEvent>()
            {
                @Override
                protected void handleTypedEvent( final DisposeEvent event )
                {
                    element.detach( listener, IAttendee.PROP_IN_CONTACTS_DATABASE.getName() );
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
