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

import org.eclipse.sapphire.services.ReferenceService;


/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ReferenceValue<R,T>

    extends Value<R>
    
{
    private final ReferenceService service;
    
    public ReferenceValue( final IModelElement parent,
                           final ValueProperty property,
                           final String value )
    {
        super( parent, property, value );
        
        this.service = parent.service( property, ReferenceService.class );
    }
    
    @SuppressWarnings( "unchecked" )
    
    public T resolve()
    {
        T result = null;
        
        if( this.service != null )
        {
            final String ref = getText();
            
            if( ref != null )
            {
                try
                {
                    result = (T) this.service.resolve( ref );
                }
                catch( Exception e )
                {
                    LoggingService.log( e );
                }
            }
        }
        
        return result;
    }
    
}
