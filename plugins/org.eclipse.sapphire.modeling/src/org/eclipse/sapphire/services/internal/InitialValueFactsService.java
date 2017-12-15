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

import static org.eclipse.sapphire.modeling.util.internal.SapphireCommonUtil.getValueLabel;

import java.util.List;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.FactsService;
import org.eclipse.sapphire.services.InitialValueService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;

/**
 * Creates fact statements about property's initial value by using semantical information specified 
 * by InitialValueService and @InitialValue annotation.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class InitialValueFactsService extends FactsService
{
    @Override
    protected void facts( final List<String> facts )
    {
        final IModelElement element = context( IModelElement.class );
        final ValueProperty property = context( ValueProperty.class );
        final InitialValueService initialValueService = element.service( property, InitialValueService.class );
        
        final String value = initialValueService.value();
        final String valueLabel = getValueLabel( element, property, value );
        facts.add( NLS.bind( Resources.statement, valueLabel ) );
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
                final IModelElement element = context.find( IModelElement.class );
                
                if( element.service( property, InitialValueService.class ) != null )
                {
                    return true;
                }
            }
            
            return false;
        }
    
        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new InitialValueFactsService();
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String statement;
        
        static
        {
            initializeMessages( InitialValueFactsService.class.getName(), Resources.class );
        }
    }
    
}
