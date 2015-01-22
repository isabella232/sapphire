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

package org.eclipse.sapphire.services;

import static org.eclipse.sapphire.modeling.util.MiscUtil.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class FileExtensionsServiceData
{
    private final List<String> extensions;
    
    public FileExtensionsServiceData( final Collection<String> extensions )
    {
        final List<String> clean = new ArrayList<String>();
        
        for( String extension : extensions )
        {
            if( extension != null )
            {
                extension = extension.trim();
                
                if( extension.length() > 0 )
                {
                    clean.add( extension );
                }
            }
        }

        this.extensions = Collections.unmodifiableList( clean );
    }
    
    public FileExtensionsServiceData( final String... extensions )
    {
        this( list( extensions ) );
    }
    
    public List<String> extensions()
    {
        return this.extensions;
    }
    
    @Override
    public boolean equals( final Object obj )
    {
        if( obj instanceof FileExtensionsServiceData )
        {
            final FileExtensionsServiceData data = (FileExtensionsServiceData) obj;
            return this.extensions.equals( data.extensions );
        }
        
        return false;
    }
    
    @Override
    public int hashCode()
    {
        return this.extensions.hashCode();
    }
    
}
