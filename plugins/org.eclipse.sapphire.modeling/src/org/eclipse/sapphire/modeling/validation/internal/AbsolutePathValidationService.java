/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling.validation.internal;

import java.io.File;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyService;
import org.eclipse.sapphire.modeling.ModelPropertyServiceFactory;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.AbsolutePath;
import org.eclipse.sapphire.modeling.annotations.FileSystemResourceType;
import org.eclipse.sapphire.modeling.validation.PathValidationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class AbsolutePathValidationService

    extends PathValidationService
    
{
    @Override
    public Status validate()
    {
        final Value<Path> value = target();
        final Path path = value.getContent( false );
        
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
                        final String message = Resources.bind( Resources.pathIsNotFile, path.toString() );
                        return Status.createErrorStatus( message );
                    }
                }
                else if( this.validResourceType == FileSystemResourceType.FOLDER )
                {
                    if( ! f.isDirectory() )
                    {
                        final String message = Resources.bind( Resources.pathIsNotFolder, path.toString() );
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
                        final String message = Resources.bind( Resources.fileMustExist, path.toString() );
                        return Status.createErrorStatus( message );
                    }
                    else if( this.validResourceType == FileSystemResourceType.FOLDER )
                    {
                        final String message = Resources.bind( Resources.folderMustExist, path.toString() );
                        return Status.createErrorStatus( message );
                    }
                    else
                    {
                        final String message = Resources.bind( Resources.resourceMustExist, path.toString() );
                        return Status.createErrorStatus( message );
                    }
                }
            }
        }
        
        return Status.createOkStatus();
    }
    
    public static final class Factory extends ModelPropertyServiceFactory
    {
        @Override
        public boolean applicable( final IModelElement element,
                                   final ModelProperty property,
                                   final Class<? extends ModelPropertyService> service )
        {
            return ( property instanceof ValueProperty && property.hasAnnotation( AbsolutePath.class ) && Path.class.isAssignableFrom( property.getTypeClass() ) );
        }

        @Override
        public ModelPropertyService create( final IModelElement element,
                                            final ModelProperty property,
                                            final Class<? extends ModelPropertyService> service )
        {
            return new AbsolutePathValidationService();
        }
    }
    
}
