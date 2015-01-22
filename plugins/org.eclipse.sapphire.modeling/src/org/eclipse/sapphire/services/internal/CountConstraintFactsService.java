/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.services.internal;

import java.util.SortedSet;

import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.annotations.CountConstraint;
import org.eclipse.sapphire.services.FactsService;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * Creates fact statements about list property's count constraint by using semantical information 
 * specified by @CountConstraint annotation.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class CountConstraintFactsService extends FactsService
{
    @Text( "Must have at least one" )
    private static LocalizableText atLeastOneStatement;
    
    @Text( "Must have at least {0} items" )
    private static LocalizableText minCountStatement;
    
    @Text( "Must have at most {0} items" )
    private static LocalizableText maxCountStatement;
    
    static
    {
        LocalizableText.init( CountConstraintFactsService.class );
    }

    @Override
    protected void facts( final SortedSet<String> facts )
    {
        final CountConstraint constraint = context( PropertyDef.class ).getAnnotation( CountConstraint.class );
        final int min = constraint.min();
        final int max = constraint.max();
        
        if( min == 1 )
        {
            facts.add( atLeastOneStatement.text() );
        }
        else if( min > 1 ) 
        {
            facts.add( minCountStatement.format( min ) );
        }
        
        if( max < Integer.MAX_VALUE ) 
        {
            facts.add( maxCountStatement.format( max ) );
        }
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final ListProperty property = context.find( ListProperty.class );
            
            if( property != null )
            {
                final CountConstraint constraint = property.getAnnotation( CountConstraint.class );
                return ( constraint != null && ( constraint.min() > 0 || constraint.max() < Integer.MAX_VALUE ) );
            }
            
            return false;
        }
    }
    
}
