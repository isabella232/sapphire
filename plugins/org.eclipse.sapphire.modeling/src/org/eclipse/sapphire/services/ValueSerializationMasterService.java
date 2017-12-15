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

package org.eclipse.sapphire.services;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.util.NLS;

/**
 * Performs serialization of values by delegating to a ValueSerializationService, if one is available. Every
 * value property is guaranteed to have a ValueSerializationMasterService, even if it is a string property
 * or a serialization service was erroneously not provided. 
 * 
 * <p>An implementation of this service is provided with Sapphire. This service is not intended to
 * be implemented by adopters.</p>
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ValueSerializationMasterService extends Service
{
    private ValueSerializationService valueSerializationService;
    
    @Override
    protected void init()
    {
        super.init();
        
        final IModelElement element = context( IModelElement.class );
        final ValueProperty property = context( ValueProperty.class );
        final Class<?> type = property.getTypeClass();
        
        if( type != String.class )
        {
            this.valueSerializationService = context().service( ValueSerializationService.class );
            
            if( this.valueSerializationService == null )
            {
                final String message
                    = NLS.bind( Resources.noSerializationService, 
                                element.getModelElementType().getModelElementClass().getName(),
                                property.getName(), type.getName() );
                
                LoggingService.log( Status.createErrorStatus( message ) );
            }
        }
    }

    public String encode( final Object value )
    {
        String result = null;
        
        if( value != null )
        {
            if( this.valueSerializationService == null )
            {
                result = value.toString();
            }
            else
            {
                result = this.valueSerializationService.encode( value );
            }
        }
        
        return result;
    }
    
    public Object decode( String value )
    {
        Object result = null;
        
        if( value != null )
        {
            if( this.valueSerializationService == null )
            {
                result = value;
            }
            else
            {
                result = this.valueSerializationService.decode( value.trim() );
            }
        }
        
        return result;
    }
    
    private static final class Resources extends NLS
    {
        public static String noSerializationService;
        
        static
        {
            initializeMessages( ValueSerializationMasterService.class.getName(), Resources.class );
        }
    }
    
}
