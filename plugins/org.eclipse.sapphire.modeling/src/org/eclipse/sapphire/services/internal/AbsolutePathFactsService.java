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

import java.util.List;

import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.AbsolutePath;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.FactsService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;

/**
 * Creates fact statements about property's absolute path requirement by using semantical information 
 * specified by @AbsolutePath annotation.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class AbsolutePathFactsService extends FactsService
{
    @Override
    protected void facts( final List<String> facts )
    {
        facts.add( Resources.statement );
    }
    
    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            final ValueProperty property = context.find( ValueProperty.class ); 
            return ( property != null && property.hasAnnotation( AbsolutePath.class ) );
        }
    
        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new AbsolutePathFactsService();
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String statement;
        
        static
        {
            initializeMessages( AbsolutePathFactsService.class.getName(), Resources.class );
        }
    }
    
}
