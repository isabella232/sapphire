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

package org.eclipse.sapphire.modeling;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.PropertyEvent;
import org.eclipse.sapphire.services.ServiceEvent;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ElementDisposeEvent extends ElementEvent
{
    public ElementDisposeEvent( final Element element )
    {
        super( element );
    }

    @Override
    public boolean supersedes( final Event event )
    {
        // When a dispose event is issued on an element, it makes irrelevant all other outstanding
        // events on that element.
        
        final Element element = element();
        
        if( event instanceof ElementEvent )
        {
            if( ( (ElementEvent) event ).element() == element )
            {
                return true;
            }
        }
        else if( event instanceof PropertyEvent )
        {
            if( ( (PropertyEvent) event ).property().element() == element )
            {
                return true;
            }
        }
        else if( event instanceof ServiceEvent )
        {
            if( ( (ServiceEvent) event ).service().context( Element.class ) == element )
            {
                return true;
            }
        }
        
        return false;
    }

}
