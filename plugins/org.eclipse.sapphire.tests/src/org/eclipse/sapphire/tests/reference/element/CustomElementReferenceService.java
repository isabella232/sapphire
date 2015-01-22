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

package org.eclipse.sapphire.tests.reference.element;

import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementReferenceService;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PropertyContentEvent;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class CustomElementReferenceService extends ElementReferenceService
{
    private Listener listener;
    
    @Override
    protected void initReferenceService()
    {
        final TestElement element = context( TestElement.class );
        
        this.listener = new FilteredListener<PropertyContentEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyContentEvent event )
            {
                if( event.property() == element.getUseItemList2() )
                {
                    broadcast( new ListEvent() );
                }
                else
                {
                    broadcast( new KeyEvent() );
                }
            }
        };
        
        element.getUseItemList2().attach( this.listener );
        element.getUseValueAsKey().attach( this.listener );
        
        super.initReferenceService();
    }
    
    @Override
    public ElementList<?> list()
    {
        final TestElement element = context( TestElement.class );
        return ( element.getUseItemList2().content() ? element.getItemList2() : element.getItemList1() );
    }

    @Override
    public String key()
    {
        final TestElement element = context( TestElement.class );
        return ( element.getUseValueAsKey().content() ? "Value" : "Name" );
    }

    @Override
    public void dispose()
    {
        final TestElement element = context( TestElement.class );
        
        if( ! element.disposed() )
        {
            element.getUseItemList2().detach( this.listener );
            element.getUseValueAsKey().detach( this.listener );
        }
        
        super.dispose();
    }

}
