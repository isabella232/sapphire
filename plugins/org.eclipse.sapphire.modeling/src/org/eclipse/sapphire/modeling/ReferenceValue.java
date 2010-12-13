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

import org.eclipse.sapphire.modeling.internal.SapphireModelingFrameworkPlugin;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ReferenceValue<T>

    extends Value<String>
    
{
    private final ReferenceService service;
    
    public ReferenceValue( final IModelElement parent,
                           final ValueProperty property,
                           final String value )
    {
        super( parent, property, value );
        
        this.service = parent.service( property, ReferenceService.class );
        
        if( this.service == null )
        {
            throw new IllegalArgumentException();
        }
    }
    
    @SuppressWarnings( "unchecked" )
    
    public T resolve()
    {
        final String ref = getText();
        T result = null;
        
        if( ref != null )
        {
            try
            {
                result = (T) this.service.resolve( ref );
            }
            catch( Exception e )
            {
                SapphireModelingFrameworkPlugin.log( e );
            }
        }
        
        return result;
    }
    
}
