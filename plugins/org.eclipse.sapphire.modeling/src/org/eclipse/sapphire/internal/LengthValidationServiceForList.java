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

import static org.eclipse.sapphire.internal.LengthFactsServiceForList.atLeastOneStatement;
import static org.eclipse.sapphire.internal.LengthFactsServiceForList.maxLengthStatement;
import static org.eclipse.sapphire.internal.LengthFactsServiceForList.minLengthStatement;

import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.Length;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ValidationService;

/**
 * An implementation of ValidationService that produces a validation error when a list property's item count
 * is outside of the constraints specified by the @Length annotation.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class LengthValidationServiceForList extends ValidationService
{
    private int min;
    private int max;
    
    @Override
    
    protected void initValidationService()
    {
        final PropertyDef property = context( PropertyDef.class );
        final Length length = property.getAnnotation( Length.class );
        
        this.min = length.min();
        this.max = length.max();
    }

    @Override
    
    protected Status compute()
    {
        final int length = context( ElementList.class ).size();
        
        if( length < this.min )
        {
            if( this.min == 1 )
            {
                return Status.createErrorStatus( atLeastOneStatement.text() );
            }
            else
            {
                final String msg = minLengthStatement.format( this.min );
                return Status.createErrorStatus( msg );
            }
        }
        else if( length > this.max )
        {
            final String msg = maxLengthStatement.format( this.max );
            return Status.createErrorStatus( msg );
        }
        
        return Status.createOkStatus();
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        
        public boolean applicable( final ServiceContext context )
        {
            final ListProperty property = context.find( ListProperty.class );
            
            if( property != null )
            {
                final Length length = property.getAnnotation( Length.class );
                return length != null && ( length.min() > 0 || length.max() < Integer.MAX_VALUE );
            }
            
            return false;
        }
    }
    
}