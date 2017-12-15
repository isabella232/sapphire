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

package org.eclipse.sapphire.services.internal;

import static org.eclipse.sapphire.modeling.util.internal.SapphireCommonUtil.getDefaultValueLabel;

import java.util.SortedSet;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.SensitiveData;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.FactsService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;

/**
 * Creates fact statements about property's default value by using semantical information specified 
 * by DefaultValueService and @DefaultValue annotation.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class DefaultValueFactsService extends FactsService
{
    @Override
    protected void facts( final SortedSet<String> facts )
    {
        final IModelElement element = context( IModelElement.class );
        final ValueProperty property = context( ValueProperty.class );
        
        final String defaultValue = getDefaultValueLabel( element, property );
        
        if( defaultValue != null )
        {
            if( property.hasAnnotation( SensitiveData.class ) )
            {
                facts.add( Resources.statementForSensitive );
            }
            else
            {
                facts.add( NLS.bind( Resources.statement, defaultValue ) );
            }
        }
    }
    
    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            return ( context.find( ValueProperty.class ) != null );
        }
    
        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new DefaultValueFactsService();
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String statement;
        public static String statementForSensitive;
        
        static
        {
            initializeMessages( DefaultValueFactsService.class.getName(), Resources.class );
        }
    }
    
}
