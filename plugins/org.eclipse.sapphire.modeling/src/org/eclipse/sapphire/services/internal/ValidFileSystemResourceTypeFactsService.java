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

import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.FileSystemResourceType;
import org.eclipse.sapphire.modeling.annotations.ValidFileSystemResourceType;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.FactsService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;

/**
 * Creates fact statements about valid file system resource type (file or folder) for property's value 
 * by using semantical information specified by @ValidFileSystemResourceType annotation.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ValidFileSystemResourceTypeFactsService extends FactsService
{
    @Override
    protected void facts( final List<String> facts )
    {
        final ValidFileSystemResourceType a = context( ModelProperty.class ).getAnnotation( ValidFileSystemResourceType.class );
        final FileSystemResourceType type = a.value();
        
        if( type == FileSystemResourceType.FILE )
        {
            facts.add( Resources.statementForFile );
        }
        else if( type == FileSystemResourceType.FOLDER )
        {
            facts.add( Resources.statementForFolder );
        }
        else
        {
            throw new IllegalStateException();
        }
    }
    
    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            return ( property != null && property.hasAnnotation( ValidFileSystemResourceType.class ) );
        }
    
        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new ValidFileSystemResourceTypeFactsService();
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String statementForFile;
        public static String statementForFolder;
        
        static
        {
            initializeMessages( ValidFileSystemResourceTypeFactsService.class.getName(), Resources.class );
        }
    }
    
}
