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

package org.eclipse.sapphire.modeling;

import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.ReferenceResolverImpl;
import org.eclipse.sapphire.modeling.internal.SapphireModelingFrameworkPlugin;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ReferenceValue<T>

    extends Value<String>
    
{
    private final ReferenceResolverImpl<T> resolver;
    
    @SuppressWarnings( "unchecked" )
    
    public ReferenceValue( final IModelElement parent,
                           final ValueProperty property,
                           final String value )
    {
        super( parent, property, value );
        
        final Reference referenceAnnotation = property.getAnnotation( Reference.class );
        
        if( referenceAnnotation != null )
        {
            final Class<? extends ReferenceResolverImpl<T>> referenceResolverClass 
                = (Class<? extends ReferenceResolverImpl<T>>) referenceAnnotation.resolver();
            
            try
            {
                this.resolver = referenceResolverClass.newInstance();
                this.resolver.init( getParent(), property, referenceAnnotation.params() );
            }
            catch( Exception e )
            {
                SapphireModelingFrameworkPlugin.log( e );
                throw new RuntimeException( e );
            }
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }
    
    public T resolve()
    {
        final String ref = getText();
        T result = null;
        
        if( ref != null )
        {
            try
            {
                result = this.resolver.resolve( ref );
            }
            catch( Exception e )
            {
                SapphireModelingFrameworkPlugin.log( e );
            }
        }
        
        return result;
    }
    
}
