/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.internal;

import static org.eclipse.sapphire.modeling.util.internal.SapphireCommonUtil.getDefaultValueLabel;

import java.util.SortedSet;

import org.eclipse.sapphire.PreferDefaultValue;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.FactsService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;

/**
 * Creates fact statements about property's recommended value by using semantical information specified 
 * by @PreferDefaultValue annotation.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class PreferDefaultValueFactsService extends FactsService
{
    @Override
    protected void facts( final SortedSet<String> facts )
    {
        final IModelElement element = context( IModelElement.class );
        final ValueProperty property = context( ValueProperty.class );
        
        final String defaultValue = getDefaultValueLabel( element, property );
        
        if( defaultValue != null )
        {
            facts.add( NLS.bind( Resources.fact, defaultValue ) );
        }
    }
    
    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            
            if( property != null )
            {
                return property.hasAnnotation( PreferDefaultValue.class );
            }
            
            return false;
        }
    
        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new PreferDefaultValueFactsService();
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String fact;
        
        static
        {
            initializeMessages( PreferDefaultValueFactsService.class.getName(), Resources.class );
        }
    }
    
}
