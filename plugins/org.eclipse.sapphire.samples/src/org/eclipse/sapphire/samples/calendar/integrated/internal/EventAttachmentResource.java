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

package org.eclipse.sapphire.samples.calendar.integrated.internal;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.BindingImpl;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.PropertyEvent;
import org.eclipse.sapphire.modeling.PropertyInitializationEvent;
import org.eclipse.sapphire.modeling.Resource;
import org.eclipse.sapphire.modeling.ValueBindingImpl;
import org.eclipse.sapphire.samples.calendar.integrated.IEventAttachment;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class EventAttachmentResource extends Resource
{
    private final org.eclipse.sapphire.samples.calendar.IEventAttachment base;
    
    public EventAttachmentResource( final Resource parent,
                                    final org.eclipse.sapphire.samples.calendar.IEventAttachment base )
    {
        super( parent );
        
        this.base = base;
        
        final Listener listener = new FilteredListener<PropertyEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyEvent event )
            {
                if( ! ( event instanceof PropertyInitializationEvent ) )
                {
                    final ModelProperty property = event.property();
                    final IModelElement element = element();
                    
                    if( property == org.eclipse.sapphire.samples.calendar.IEventAttachment.PROP_LOCAL_COPY_LOCATION )
                    {
                        element.refresh( IEventAttachment.PROP_LOCAL_COPY_LOCATION );
                    }
                    else if( property == org.eclipse.sapphire.samples.calendar.IEventAttachment.PROP_PUBLIC_COPY_LOCATION )
                    {
                        element.refresh( IEventAttachment.PROP_PUBLIC_COPY_LOCATION );
                    }
                }
            }
        };
        
        this.base.attach( listener );
    }
    
    public org.eclipse.sapphire.samples.calendar.IEventAttachment getBase()
    {
        return this.base;
    }
    
    @Override
    protected BindingImpl createBinding( final ModelProperty property )
    {
        if( property == IEventAttachment.PROP_LOCAL_COPY_LOCATION )
        {
            return new ValueBindingImpl()
            {
                @Override
                public String read()
                {
                    return getBase().getLocalCopyLocation().getText( false );
                }
                
                @Override
                public void write( String value )
                {
                    getBase().setLocalCopyLocation( value );
                }
            };
        }
        else if( property == IEventAttachment.PROP_PUBLIC_COPY_LOCATION )
        {
            return new ValueBindingImpl()
            {
                @Override
                public String read()
                {
                    return getBase().getPublicCopyLocation().getText( false );
                }
                
                @Override
                public void write( String value )
                {
                    getBase().setPublicCopyLocation( value );
                }
            };
        }
        
        return null;
    }
    

    @Override
    public <A> A adapt( final Class<A> adapterType )
    {
        A res = super.adapt( adapterType );
        
        if( res == null )
        {
            res = this.base.adapt( adapterType );
        }
        
        return res;
    }

}
