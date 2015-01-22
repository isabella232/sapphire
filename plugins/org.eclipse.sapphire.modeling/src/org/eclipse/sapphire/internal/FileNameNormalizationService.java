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

package org.eclipse.sapphire.internal;

import java.util.List;

import org.eclipse.sapphire.FileName;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.services.FileExtensionsService;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ValueNormalizationService;

/**
 * Implementation of ValueNormalizationService for value properties of type FileName.
 * 
 * <p>The following normalization is performed:</p>
 * 
 * <ol>
 * 
 *   <li>Leading whitespace is removed.</li>
 *   
 *   <li>Trailing whitespace and dots are removed.</li>
 *   
 *   <li>Extension is added if file name does not have one already and if the property
 *   has a FileExtensionsService (usually via @FileExtensions annotation).</li>
 *   
 * </ol>
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class FileNameNormalizationService extends ValueNormalizationService
{
    @Override
    public String normalize( final String str )
    {
        String normalized = str;
        
        if( normalized != null )
        {
            normalized = normalized.trim();
            
            // Remove trailing dots, except in the case where the entire file name is dots.
            
            boolean hasDotChars = false;
            boolean hasSignificantChars = false;
            
            for( int i = 0, n = normalized.length(); i < n && ! ( hasDotChars && hasSignificantChars ); i++ )
            {
                final char ch = normalized.charAt( i );
                
                if( ch == '.' )
                {
                    hasDotChars = true;
                }
                else if( ! Character.isWhitespace( ch ) )
                {
                    hasSignificantChars = true;
                }
            }
            
            if( hasDotChars && hasSignificantChars )
            {
                while( normalized.endsWith( "." ) || normalized.endsWith( " " ) )
                {
                    normalized = normalized.substring( 0, normalized.length() - 1 );
                }
            }
            
            // Add file extension if not specified.
            
            if( hasSignificantChars )
            {
                int segments = 0;
                
                for( String segment : normalized.split( "\\." ) )
                {
                    if( segment.trim().length() > 0 )
                    {
                        segments++;
                        
                        if( segments > 1 )
                        {
                            break; // Only need to know if count is anything other than one.
                        }
                    }
                }
                
                if( segments == 1 )
                {
                    final FileExtensionsService fileExtensionsService = context( Property.class ).service( FileExtensionsService.class );
                    
                    if( fileExtensionsService != null )
                    {
                        final List<String> extensions = fileExtensionsService.extensions();
                        
                        if( ! extensions.isEmpty() )
                        {
                            final String extension = extensions.get( 0 );
                            normalized = normalized + "." + extension.toLowerCase();
                        }
                    }
                }
            }
        }
        
        return normalized;
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final ValueProperty property = context.find( ValueProperty.class ); 
            return ( property != null && property.isOfType( FileName.class ) );
        }
    }
    
}
