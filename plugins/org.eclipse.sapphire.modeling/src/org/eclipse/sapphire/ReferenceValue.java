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

package org.eclipse.sapphire;

import org.eclipse.sapphire.services.ReferenceService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ReferenceValue<R,T> extends Value<R>
{
    private ReferenceService service;
    
    public ReferenceValue( final Element element, final ValueProperty property )
    {
        super( element, property );
    }
    
    /**
     * Returns a reference to ReferenceValue.class that is parameterized with the given types.
     * 
     * <p>Example:</p>
     * 
     * <p><code>Class&lt;ReferenceValue&lt;JavaTypeName,JavaType>> cl = ReferenceValue.of( JavaTypeName.class, JavaType.class );</code></p>
     *  
     * @param referenceType the reference type
     * @param targetType the target type
     * @return a reference to ReferenceValue.class that is parameterized with the given types
     */
    
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    
    public static <RX,TX> Class<ReferenceValue<RX,TX>> of( final Class<RX> referenceType, final Class<TX> targetType )
    {
        return (Class) ReferenceValue.class;
    }
    
    @SuppressWarnings( "unchecked" )
    
    public T resolve()
    {
        assertNotDisposed();
        
        if( this.service == null )
        {
            this.service = service( ReferenceService.class );
        }
        
        T result = null;
        
        if( this.service != null )
        {
            final String ref = text();
            
            try
            {
                result = (T) this.service.resolve( ref );
            }
            catch( Exception e )
            {
                Sapphire.service( LoggingService.class ).log( e );
            }
        }
        
        return result;
    }
    
}
