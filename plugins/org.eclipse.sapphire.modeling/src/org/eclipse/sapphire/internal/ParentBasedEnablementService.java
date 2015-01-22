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

package org.eclipse.sapphire.internal;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.EnablementService;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyEnablementEvent;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ParentBasedEnablementService extends EnablementService
{
    private Property parent;
    private Listener listener;
    
    @Override
    protected void initEnablementService()
    {
        this.parent = context( Element.class ).parent();
        
        this.listener = new FilteredListener<PropertyEnablementEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyEnablementEvent event )
            {
                refresh();
            }
        };
        
        this.parent.attach( this.listener );
    }

    @Override
    public Boolean compute()
    {
        return this.parent.enabled();
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.listener != null )
        {
            this.parent.detach( this.listener );
        }
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            return ( context.find( Element.class ).parent() != null );
        }
    }
    
}
