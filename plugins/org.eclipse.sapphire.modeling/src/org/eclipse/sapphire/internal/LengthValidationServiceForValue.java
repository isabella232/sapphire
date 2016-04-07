/******************************************************************************
 * Copyright (c) 2016 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.internal;

import static org.eclipse.sapphire.internal.LengthFactsServiceForValue.maxLengthStatement;
import static org.eclipse.sapphire.internal.LengthFactsServiceForValue.minLengthStatement;

import org.eclipse.sapphire.Length;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ValidationService;

/**
 * An implementation of ValidationService that produces a validation error when a value property's text length
 * is outside of the constraints specified by the @Length annotation.

 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class LengthValidationServiceForValue extends ValidationService
{
    private int min;
    private int max;
    
    @Override
    
    protected void initValidationService()
    {
        final PropertyDef property = context( PropertyDef.class );
        final Length lengthAnnotation = property.getAnnotation( Length.class );
        
        this.min = lengthAnnotation.min();
        this.max = lengthAnnotation.max();
    }

    @Override
    
    protected Status compute()
    {
        final String text = context( Value.class ).text();
        
        if( text != null )
        {
            final int length = text.length();
            
            if( length < this.min )
            {
                final String msg = minLengthStatement.format( this.min );
                return Status.createErrorStatus( msg );                
            }
            else if( length > this.max )
            {
                final String msg = maxLengthStatement.format( this.max );
                return Status.createErrorStatus( msg );                
            }
        }
        
        return Status.createOkStatus();
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        
        public boolean applicable( final ServiceContext context )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            
            if( property != null )
            {
                final Length annotation = property.getAnnotation( Length.class );
                return annotation != null && ( annotation.min() > 0 || annotation.max() < Integer.MAX_VALUE );
            }
            
            return false;
        }
    }

}
