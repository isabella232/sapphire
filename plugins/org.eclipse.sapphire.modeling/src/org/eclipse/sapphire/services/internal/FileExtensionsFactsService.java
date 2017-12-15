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

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.FactsService;
import org.eclipse.sapphire.services.FileExtensionsService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;

/**
 * Creates fact statements about valid file extensions for property's value by using semantical 
 * information specified by @FileExtensions annotation.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class FileExtensionsFactsService extends FactsService
{
    @Override
    protected void facts( final List<String> facts )
    {
        final FileExtensionsService service = context( IModelElement.class ).service( context( ModelProperty.class ), FileExtensionsService.class );
        final List<String> extensions = service.extensions();
        final int count = extensions.size();
        
        if( count > 0 )
        {
            if( count == 1 )
            {
                facts.add( NLS.bind( Resources.statementForOne, extensions.get( 0 ) ) );
            }
            else if( count == 2 )
            {
                facts.add( NLS.bind( Resources.statementForTwo, extensions.get( 0 ), extensions.get( 1 ) ) );
            }
            else if( count == 3 )
            {
                facts.add( NLS.bind( Resources.statementForThree, extensions.get( 0 ), extensions.get( 1 ), extensions.get( 2 ) ) );
            }
            else
            {
                final StringBuilder buf = new StringBuilder();
                
                for( String extension : extensions )
                {
                    if( buf.length() > 0 )
                    {
                        buf.append( ", " );
                    }
                    
                    buf.append( '"' );
                    buf.append( extension );
                    buf.append( '"' );
                }
                
                facts.add( NLS.bind( Resources.statementForMany, buf.toString() ) );
            }
        }
    }
    
    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            return ( property != null && context.find( IModelElement.class ).service( property, FileExtensionsService.class ) != null );
        }
    
        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new FileExtensionsFactsService();
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String statementForOne;
        public static String statementForTwo;
        public static String statementForThree;
        public static String statementForMany;
        
        static
        {
            initializeMessages( FileExtensionsFactsService.class.getName(), Resources.class );
        }
    }
    
}
