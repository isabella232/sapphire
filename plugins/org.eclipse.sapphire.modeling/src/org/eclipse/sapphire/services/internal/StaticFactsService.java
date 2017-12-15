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

package org.eclipse.sapphire.services.internal;

import java.util.List;

import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Fact;
import org.eclipse.sapphire.modeling.annotations.Facts;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.services.FactsService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;

/**
 * Creates fact statements about property by using static content specified in @Fact and @Facts annotations.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class StaticFactsService extends FactsService
{
    @Override
    protected void facts( final List<String> facts )
    {
        final ModelProperty property = context( ModelProperty.class );
        
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
    
    private void facts( final List<String> facts,
                        final Fact fact )
    {
        final LocalizationService localization = context( ModelProperty.class ).getLocalizationService();
        facts.add( localization.text( fact.statement(), CapitalizationType.NO_CAPS, true ) );
    }
    
    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            return ( property != null && ( property.hasAnnotation( Fact.class ) || property.hasAnnotation( Facts.class ) ) );
        }
    
        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new StaticFactsService();
        }
    }
    
}
