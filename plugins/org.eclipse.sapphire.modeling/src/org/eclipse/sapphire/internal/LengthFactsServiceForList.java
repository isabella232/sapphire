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

import java.util.SortedSet;

import org.eclipse.sapphire.Length;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.annotations.CountConstraint;
import org.eclipse.sapphire.services.FactsService;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * An implementation of FactsService that creates fact statements about a list property's item count
 * constraints by using the semantical information specified by the @Length annotation or the deprecated
 * @CountConstraint annotation.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@SuppressWarnings( "deprecation" )

public final class LengthFactsServiceForList extends FactsService
{
    @Text( "Must have at least one" )
    public static LocalizableText atLeastOneStatement;
    
    @Text( "Must have at least {0} items" )
    public static LocalizableText minLengthStatement;
    
    @Text( "Must have at most {0} items" )
    public static LocalizableText maxLengthStatement;
    
    static
    {
        LocalizableText.init( LengthFactsServiceForList.class );
    }

    private int min;
    private int max;
    
    @Override
    
    protected void init()
    {
        final PropertyDef property = context( PropertyDef.class );
        final Length length = property.getAnnotation( Length.class );
        
        if( length != null )
        {
            this.min = length.min();
            this.max = length.max();
        }
        else
        {
            final CountConstraint count = property.getAnnotation( CountConstraint.class );
            
            this.min = count.min();
            this.max = count.max();
        }
    }

    @Override
    
    protected void facts( final SortedSet<String> facts )
    {
        if( this.min == 1 )
        {
            facts.add( atLeastOneStatement.text() );
        }
        else if( this.min > 1 ) 
        {
            facts.add( minLengthStatement.format( min ) );
        }
        
        if( this.max < Integer.MAX_VALUE ) 
        {
            facts.add( maxLengthStatement.format( this.max ) );
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
                final Length length = property.getAnnotation( Length.class );
                final CountConstraint count = property.getAnnotation( CountConstraint.class );
                
                return ( length != null && ( length.min() > 0 || length.max() < Integer.MAX_VALUE ) ) ||
                       ( count != null && ( count.min() > 0 || count.max() < Integer.MAX_VALUE ) );
            }
            
            return false;
        }
    }
    
}
