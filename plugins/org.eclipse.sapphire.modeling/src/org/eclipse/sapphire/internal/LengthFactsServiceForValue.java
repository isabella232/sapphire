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
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.services.FactsService;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * An implementation of FactsService that creates fact statements about a value property's text length
 * constraints by using the semantical information specified by the @Length annotation.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class LengthFactsServiceForValue extends FactsService
{
    @Text( "Minimum length is {0}" )
    public static LocalizableText minLengthStatement;
    
    @Text( "Maximum length is {0}" )
    public static LocalizableText maxLengthStatement;
    
    static
    {
        LocalizableText.init( LengthFactsServiceForValue.class );
    }

    private int min;
    private int max;
    
    @Override
    
    protected void init()
    {
        final PropertyDef property = context( PropertyDef.class );
        final Length annotation = property.getAnnotation( Length.class );
        
        this.min = annotation.min();
        this.max = annotation.max();
    }

    @Override
    
    protected void facts( final SortedSet<String> facts )
    {
        if( this.min != 0 )
        {
            facts.add( minLengthStatement.format( this.min ) );
        }
        
        if( this.max != Integer.MAX_VALUE )
        {
            facts.add( maxLengthStatement.format( this.max ) );
        }
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
