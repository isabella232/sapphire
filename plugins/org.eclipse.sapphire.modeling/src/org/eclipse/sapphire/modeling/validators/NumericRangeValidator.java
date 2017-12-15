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

package org.eclipse.sapphire.modeling.validators;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueKeyword;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.ModelPropertyValidator;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class NumericRangeValidator<T extends Comparable<T>>

    extends ModelPropertyValidator<Value<T>>

{
    private final T min;
    private final T max;
    
    public NumericRangeValidator( final T minValue,
                                  final T maxValue )
    {
        this.min = minValue;
        this.max = maxValue;
    }
    
    @Override
    public IStatus validate( final Value<T> value )
    {
        final T val = value.getContent( true );
        
        if( val != null )
        {
            final ValueProperty property = value.getProperty();
            
            if( this.min != null && val.compareTo( this.min ) < 0 )
            {
                final String msg 
                    = NLS.bind( Resources.smallerThanMinimumMessage, val, 
                                normalizeForDisplay( property, this.min ) );
                
                return createErrorStatus( msg );
            }
            
            if( this.max != null && val.compareTo( this.max ) > 0 )
            {
                final String msg 
                    = NLS.bind( Resources.largerThanMaxiumMessage, val, 
                                normalizeForDisplay( property, this.max ) );
                
                return createErrorStatus( msg );                
            }
        }
        
        return Status.OK_STATUS;
    }
    
    private String normalizeForDisplay( final ValueProperty property,
                                        final T value )
    {
        String result = property.encodeKeywords( value.toString() );
        
        ValueKeyword keyword = property.getKeyword( result );
        
        if( keyword != null )
        {
            result = keyword.toDisplayString();
        }
        
        return result;
    }
    
    private static final class Resources
    
        extends NLS
        
    {
        public static String smallerThanMinimumMessage;
        public static String largerThanMaxiumMessage;

        static
        {
            initializeMessages( NumericRangeValidator.class.getName(), Resources.class );
        }
    }

}
