/******************************************************************************
 * Copyright (c) 2015 Oracle
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
import java.util.SortedSet;

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.services.FactsService;
import org.eclipse.sapphire.services.FileExtensionsService;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * Creates fact statements about valid file extensions for property's value by using semantical 
 * information specified by @FileExtensions annotation.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class FileExtensionsFactsService extends FactsService
{
    @Text( "Must have \"{0}\" file extension" )
    private static LocalizableText statementForOne;
    
    @Text( "Must have either \"{0}\" or \"{1}\" file extension" )
    private static LocalizableText statementForTwo;
    
    @Text( "Must have either \"{0}\", \"{1}\" or \"{2}\" file extension" )
    private static LocalizableText statementForThree;
    
    @Text( "Must have one of these file extensions: {0}" )
    private static LocalizableText statementForMany;
    
    static
    {
        LocalizableText.init( FileExtensionsFactsService.class );
    }

    @Override
    protected void facts( final SortedSet<String> facts )
    {
        final FileExtensionsService service = context( Property.class ).service( FileExtensionsService.class );
        final List<String> extensions = service.extensions();
        final int count = extensions.size();
        
        if( count > 0 )
        {
            if( count == 1 )
            {
                facts.add( statementForOne.format( extensions.get( 0 ) ) );
            }
            else if( count == 2 )
            {
                facts.add( statementForTwo.format( extensions.get( 0 ), extensions.get( 1 ) ) );
            }
            else if( count == 3 )
            {
                facts.add( statementForThree.format( extensions.get( 0 ), extensions.get( 1 ), extensions.get( 2 ) ) );
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
                
                facts.add( statementForMany.format( buf.toString() ) );
            }
        }
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final Property property = context.find( Property.class );
            return ( property != null && property.service( FileExtensionsService.class ) != null );
        }
    }
    
}
