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

import java.io.File;

import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.annotations.AbsolutePath;
import org.eclipse.sapphire.modeling.annotations.FileSystemResourceType;
import org.eclipse.sapphire.services.PathValidationService;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class AbsolutePathValidationService extends PathValidationService
{
    @Override
    protected Status compute()
    {
        final Value<?> value = context( Value.class );
        final Path path = (Path) value.content( false );
        
        if( path != null )
        {
            final File f = path.toFile();
            
            if( f.exists() )
            {
                if( this.validResourceType == FileSystemResourceType.FILE )
                {
                    if( f.isFile() )
                    {
                        return validateExtensions( path );
                    }
                    else
                    {
                        final String message = pathIsNotFile.format( path.toString() );
                        return Status.createErrorStatus( message );
                    }
                }
                else if( this.validResourceType == FileSystemResourceType.FOLDER )
                {
                    if( ! f.isDirectory() )
                    {
                        final String message = pathIsNotFolder.format( path.toString() );
                        return Status.createErrorStatus( message );
                    }
                }
            }
            else
            {
                if( this.resourceMustExist )
                {
                    if( this.validResourceType == FileSystemResourceType.FILE )
                    {
                        final String message = fileMustExist.format( path.toString() );
                        return Status.createErrorStatus( message );
                    }
                    else if( this.validResourceType == FileSystemResourceType.FOLDER )
                    {
                        final String message = folderMustExist.format( path.toString() );
                        return Status.createErrorStatus( message );
                    }
                    else
                    {
                        final String message = resourceMustExistMessage.format( path.toString() );
                        return Status.createErrorStatus( message );
                    }
                }
            }
        }
        
        return Status.createOkStatus();
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            return ( property != null && property.hasAnnotation( AbsolutePath.class ) && Path.class.isAssignableFrom( property.getTypeClass() ) );
        }
    }
    
}
