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

package org.eclipse.sapphire.modeling.validation;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyService;
import org.eclipse.sapphire.modeling.ModelPropertyValidationService;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.annotations.FileSystemResourceType;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.ValidFileSystemResourceType;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.FileExtensionsService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class PathValidationService

    extends ModelPropertyValidationService<Value<Path>>
    
{
    protected boolean resourceMustExist;
    protected FileSystemResourceType validResourceType;
    private FileExtensionsService fileExtensionsService;
    
    @Override
    public void init( final IModelElement element,
                      final ModelProperty property,
                      final String[] params )
    {
        super.init( element, property, params );
        
        this.resourceMustExist = property.hasAnnotation( MustExist.class );
        
        final ValidFileSystemResourceType validResourceTypeAnnotation = property.getAnnotation( ValidFileSystemResourceType.class );
        this.validResourceType = ( validResourceTypeAnnotation != null ? validResourceTypeAnnotation.value() : null );
        
        this.fileExtensionsService = element.service( property, FileExtensionsService.class );
        
        if( this.fileExtensionsService != null )
        {
            this.fileExtensionsService.addListener
            (
                new ModelPropertyService.Listener()
                {
                    @Override
                    public void handleEvent( final Event event )
                    {
                        if( event instanceof FileExtensionsService.FileExtensionsChangedEvent )
                        {
                            element.refresh( property );
                        }
                    }
                }
            );
        }
    }
    
    protected final Status validateExtensions( final Path path )
    {
        if( this.fileExtensionsService != null )
        {
            final String fileName = path.lastSegment();
            
            if( fileName != null )
            {
                return PathValidation.validateExtensions( fileName, this.fileExtensionsService.extensions() );
            }
        }
        
        return Status.createOkStatus();
    }
    
    protected static final class Resources extends NLS
    {
        public static String folderMustExist;
        public static String fileMustExist;
        public static String resourceMustExist;
        public static String pathIsNotFile;
        public static String pathIsNotFolder;
        
        static
        {
            initializeMessages( PathValidationService.class.getName(), Resources.class );
        }
    }
    
}
