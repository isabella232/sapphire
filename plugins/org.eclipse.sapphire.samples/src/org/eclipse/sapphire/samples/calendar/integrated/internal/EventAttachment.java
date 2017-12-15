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

package org.eclipse.sapphire.samples.calendar.integrated.internal;

import org.eclipse.sapphire.modeling.IModelParticle;
import org.eclipse.sapphire.modeling.ModelElementListener;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class EventAttachment

    extends EventAttachmentStub
    
{
    private final org.eclipse.sapphire.samples.calendar.IEventAttachment base;
    private final ModelElementListener listener;

    public EventAttachment( final IModelParticle parent,
                            final ModelProperty parentProperty,
                            final org.eclipse.sapphire.samples.calendar.IEventAttachment base )
    {
        super( parent, parentProperty );
        
        this.base = base;
        
        this.listener = new ModelElementListener()
        {
            @Override
            public void propertyChanged( final ModelPropertyChangeEvent event )
            {
                final ModelProperty property = event.getProperty();
                
                if( property == org.eclipse.sapphire.samples.calendar.IEventAttachment.PROP_LOCAL_COPY_LOCATION )
                {
                    refresh( PROP_LOCAL_COPY_LOCATION );
                }
                else if( property == org.eclipse.sapphire.samples.calendar.IEventAttachment.PROP_PUBLIC_COPY_LOCATION )
                {
                    refresh( PROP_PUBLIC_COPY_LOCATION );
                }
            }
        };
        
        this.base.addListener( this.listener );
    }
    
    org.eclipse.sapphire.samples.calendar.IEventAttachment getBase()
    {
        return this.base;
    }
    
    @Override
    protected String readLocalCopyLocation()
    {
        return this.base.getLocalCopyLocation().getText( false );
    }

    @Override
    protected void writeLocalCopyLocation( final String localCopyLocation )
    {
        this.base.setLocalCopyLocation( localCopyLocation );
    }

    @Override
    protected String readPublicCopyLocation()
    {
        return this.base.getPublicCopyLocation().getText( false );
    }

    @Override
    protected void writePublicCopyLocation( final String publicCopyLocation )
    {
        this.base.setPublicCopyLocation( publicCopyLocation );
    }

    @Override
    protected void doRemove()
    {
        this.base.remove();
    }
    
}
