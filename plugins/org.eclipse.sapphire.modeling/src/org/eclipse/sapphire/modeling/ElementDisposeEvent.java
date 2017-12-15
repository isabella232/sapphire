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

package org.eclipse.sapphire.modeling;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.services.ServiceEvent;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ElementDisposeEvent extends ElementEvent
{
    public ElementDisposeEvent( final IModelElement element )
    {
        super( element );
    }

    @Override
    public boolean supersedes( final Event event )
    {
        // When a dispose event is issued on an element, it makes irrelevant all other outstanding
        // events on that element.
        
        final IModelElement element = element();
        
        if( event instanceof ElementEvent )
        {
            if( ( (ElementEvent) event ).element() == element )
            {
                return true;
            }
        }
        else if( event instanceof PropertyEvent )
        {
            if( ( (PropertyEvent) event ).element() == element )
            {
                return true;
            }
        }
        else if( event instanceof ServiceEvent )
        {
            if( ( (ServiceEvent) event ).service().context( IModelElement.class ) == element )
            {
                return true;
            }
        }
        
        return false;
    }

}
