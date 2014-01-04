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

package org.eclipse.sapphire.services.internal;

import java.util.SortedSet;

import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.annotations.Fact;
import org.eclipse.sapphire.modeling.annotations.Facts;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.services.FactsService;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * Creates fact statements about property by using static content specified in @Fact and @Facts annotations.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StaticFactsService extends FactsService
{
    @Override
    protected void facts( final SortedSet<String> facts )
    {
        final PropertyDef property = context( PropertyDef.class );
        
        final Fact factAnnotation = property.getAnnotation( Fact.class );
        
        if( factAnnotation != null )
        {
            facts( facts, factAnnotation );
        }
        
        final Facts factsAnnotation = property.getAnnotation( Facts.class );
        
        if( factsAnnotation != null )
        {
            for( Fact a : factsAnnotation.value() )
            {
                facts( facts, a );
            }
        }
    }
    
    private void facts( final SortedSet<String> facts,
                        final Fact fact )
    {
        final LocalizationService localization = context( PropertyDef.class ).getLocalizationService();
        facts.add( localization.text( fact.statement(), CapitalizationType.NO_CAPS, true ) );
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final PropertyDef property = context.find( PropertyDef.class );
            return ( property != null && ( property.hasAnnotation( Fact.class ) || property.hasAnnotation( Facts.class ) ) );
        }
    }
    
}
