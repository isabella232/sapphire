/******************************************************************************
 * Copyright (c) 2015 Liferay and Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gregory Amerson - initial implementation
 *    Konstantin Komissarchik - initial implementation review and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import static org.eclipse.sapphire.util.CollectionsUtil.equalsBasedOnEntryIdentity;

import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceEvent;
import org.eclipse.sapphire.util.ListFactory;

/**
 * Serves as a conduit between the presentation layer and anything that may want 
 * to see or change the selection.
 * 
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ListSelectionService extends Service
{
    private List<Element> elements = Collections.emptyList();

    /**
     * Returns the list of currently selected elements.
     * 
     * @return an unmodifiable list of the current selection
     */
    
    public List<Element> selection()
    {
        return this.elements;
    }

    /**
     * Selects a single element in the list. If selection changes, ListSelectionChangedEvent will
     * be fired.
     * 
     * @param element the new selection or null
     */
    
    public void select( final Element element )
    {
        select( ListFactory.<Element>start().add( element ).result() );
    }

    /**
     * Select zero or more elements in the list. If the selection changes, ListSelectionChangedEvent will
     * be fired.
     * 
     * @param elements the new selection
     */
    
    public void select( final List<Element> elements )
    {
        if( ! equalsBasedOnEntryIdentity( this.elements, elements ) )
        {
            final List<Element> before = this.elements;
            
            this.elements = ListFactory.unmodifiable( elements );

            broadcast( new ListSelectionChangedEvent( this, before, this.elements ) );
        }
    }
    
    /**
     * The event that is fired when list selection changes.
     */

    public static final class ListSelectionChangedEvent extends ServiceEvent
    {
        private List<Element> before;
        private List<Element> after;

        ListSelectionChangedEvent( final ListSelectionService service,
                                   final List<Element> before, 
                                   final List<Element> after )
        {
            super( service );
            
            this.before = before;
            this.after = after;
        }

        public List<Element> before()
        {
            return this.before;
        }

        public List<Element> after()
        {
            return this.after;
        }
    }

}
