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

package org.eclipse.sapphire.internal;

import java.util.SortedSet;

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Serialization;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.services.FactsService;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * Creates fact statements about property's serialization by using semantical information specified by
 * the @Serialization annotation.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SerializationFactsService extends FactsService
{
    @Text( "Conforms to {0}" )
    private static LocalizableText fact;
    
    static
    {
        LocalizableText.init( SerializationFactsService.class );
    }

    @Override
    protected void facts( final SortedSet<String> facts )
    {
        final Serialization serialization = context( ValueProperty.class ).getAnnotation( Serialization.class );
        
        facts.add( fact.format( serialization.primary() ) );
        
        for( final String alternative : serialization.alternative() )
        {
            facts.add( fact.format( alternative ) );
        }
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            return ( property != null && property.hasAnnotation( Serialization.class ) );
        }
    }
    
}
