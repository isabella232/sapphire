/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.services.internal;

import java.util.SortedSet;

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.FileSystemResourceType;
import org.eclipse.sapphire.modeling.annotations.ValidFileSystemResourceType;
import org.eclipse.sapphire.services.FactsService;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * Creates fact statements about valid file system resource type (file or folder) for property's value 
 * by using semantical information specified by @ValidFileSystemResourceType annotation.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ValidFileSystemResourceTypeFactsService extends FactsService
{
    @Text( "Must be a file" )
    private static LocalizableText statementForFile;
    
    @Text( "Must be a folder" )
    private static LocalizableText statementForFolder;
    
    static
    {
        LocalizableText.init( ValidFileSystemResourceTypeFactsService.class );
    }

    @Override
    protected void facts( final SortedSet<String> facts )
    {
        final ValidFileSystemResourceType a = context( PropertyDef.class ).getAnnotation( ValidFileSystemResourceType.class );
        final FileSystemResourceType type = a.value();
        
        if( type == FileSystemResourceType.FILE )
        {
            facts.add( statementForFile.text() );
        }
        else if( type == FileSystemResourceType.FOLDER )
        {
            facts.add( statementForFolder.text() );
        }
        else
        {
            throw new IllegalStateException();
        }
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            return ( property != null && property.hasAnnotation( ValidFileSystemResourceType.class ) );
        }
    }
    
}
