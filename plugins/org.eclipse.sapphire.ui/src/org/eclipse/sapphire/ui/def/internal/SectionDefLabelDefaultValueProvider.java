/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.def.internal;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.services.DefaultValueService;
import org.eclipse.sapphire.services.DefaultValueServiceData;
import org.eclipse.sapphire.ui.forms.MasterDetailsContentNodeDef;
import org.eclipse.sapphire.ui.forms.SectionDef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SectionDefLabelDefaultValueProvider extends DefaultValueService
{
    private MasterDetailsContentNodeDef node;
    private Listener listener;
    
    @Override
    protected void initDefaultValueService()
    {
        final SectionDef section = context( SectionDef.class );
        
        if( section.parent() != null )
        {
            final Element parent = section.parent().element();
            
            if( parent instanceof MasterDetailsContentNodeDef )
            {
                this.node = (MasterDetailsContentNodeDef) parent;
                
                this.listener = new FilteredListener<PropertyContentEvent>()
                {
                    @Override
                    protected void handleTypedEvent( final PropertyContentEvent event )
                    {
                        refresh();
                    }
                };
                
                this.node.getLabel().attach( this.listener );
            }
        }
    }

    @Override
    protected DefaultValueServiceData compute()
    {
        return new DefaultValueServiceData( this.node != null ? this.node.getLabel().text() : null );
    }
    
    @Override
    public void dispose()
    {
        if( this.node != null )
        {
            if( ! this.node.disposed() )
            {
                this.node.getLabel().detach( this.listener );
            }
            
            this.node = null;
            this.listener = null;
        }
        
        super.dispose();
    }

}
