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

package org.eclipse.sapphire.modeling;

import org.eclipse.sapphire.modeling.util.MiscUtil;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class Transient<T>

    extends ModelParticle
    
{
    private final TransientProperty property;
    private final T content;
    private Status valres;

    public Transient( final IModelElement parent,
                      final TransientProperty property,
                      final T content )
    {
        super( parent, parent.resource() );
        
        this.property = property;
        this.content = content;
        this.valres = null;
    }
    
    public void init()
    {
        initValidation();
    }
    
    private void initValidation()
    {
        if( this.valres == null )
        {
            final Status.CompositeStatusFactory factory = Status.factoryForComposite();
            
            for( ValidationService svc : parent().services( this.property, ValidationService.class ) )
            {
                try
                {
                    factory.merge( svc.validate() );
                }
                catch( Exception e )
                {
                    LoggingService.log( e );
                }
            }
            
            this.valres = factory.create();
        }
    }

    @Override
    public IModelElement parent()
    {
        return (IModelElement) super.parent();
    }
    
    public TransientProperty property()
    {
        return this.property;
    }
    
    public T content()
    {
        return this.content;
    }
    
    public Status validate()
    {
        initValidation();
        return this.valres;
    }
    
    @Override
    public boolean equals( final Object val )
    {
        if( this == val )
        {
            return true;
        }
        
        if( val == null )
        {
            return false;
        }
        
        init();
        
        final Transient<?> value = (Transient<?>) val;
        
        return ( parent() == value.parent() ) && ( this.property == value.property ) &&
               ( MiscUtil.equal( this.content, value.content ) ) && this.valres.equals( value.valres ); 
    }
    
    @Override
    public int hashCode()
    {
        int hashCode = parent().hashCode();
        hashCode = hashCode ^ this.property.hashCode();
        hashCode = hashCode ^ ( this.content == null ? 1 : this.content.hashCode() );
        
        return hashCode;
    }
    
    @Override
    public String toString()
    {
        return ( this.content == null ? "<null>" : this.content.toString() );
    }
    
}
