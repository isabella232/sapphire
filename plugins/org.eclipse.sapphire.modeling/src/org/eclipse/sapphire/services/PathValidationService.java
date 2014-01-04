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

package org.eclipse.sapphire.services;

import java.util.List;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.annotations.FileSystemResourceType;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.ValidFileSystemResourceType;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class PathValidationService extends ValidationService
{
    @Text( "Folder \"{0}\" does not exist" )
    protected static LocalizableText folderMustExist;
    
    @Text( "File \"{0}\" does not exist")
    protected static LocalizableText fileMustExist;
    
    @Text( "File or folder \"{0}\" does not exist" )
    protected static LocalizableText resourceMustExistMessage;
    
    @Text( "Resource at location \"{0}\" is not a file" )
    protected static LocalizableText pathIsNotFile;
    
    @Text( "Resource at location \"{0}\" is not a folder" )
    protected static LocalizableText pathIsNotFolder;
    
    @Text( "File \"{0}\" has an invalid extension. Only \"{1}\" extension is allowed" )
    protected static LocalizableText invalidFileExtensionOne;
    
    @Text( "File \"{0}\" has an invalid extension. Only extensions \"{1}\" and \"{2}\" are allowed" )
    protected static LocalizableText invalidFileExtensionTwo;
    
    @Text( "File \"{0}\" has an invalid extension. Only extensions from \"{1}\" list are allowed" )
    protected static LocalizableText invalidFileExtensionMultiple;
    
    static
    {
        LocalizableText.init( PathValidationService.class );
    }

    protected boolean resourceMustExist;
    protected FileSystemResourceType validResourceType;
    private FileExtensionsService fileExtensionsService;
    
    @Override
    protected void initValidationService()
    {
        final Property property = context( Property.class );
        
        this.resourceMustExist = property.definition().hasAnnotation( MustExist.class );
        
        final ValidFileSystemResourceType validResourceTypeAnnotation = property.definition().getAnnotation( ValidFileSystemResourceType.class );
        this.validResourceType = ( validResourceTypeAnnotation != null ? validResourceTypeAnnotation.value() : null );
        
        this.fileExtensionsService = property.service( FileExtensionsService.class );
        
        if( this.fileExtensionsService != null )
        {
            this.fileExtensionsService.attach
            (
                new Listener()
                {
                    @Override
                    public void handle( final Event event )
                    {
                        refresh();
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
                final List<String> extensions = this.fileExtensionsService.extensions();
                final int count = ( extensions == null ? 0 : extensions.size() );
                
                if( count > 0 )
                {
                    final String trimmedFileName = fileName.trim();
                    final int lastdot = trimmedFileName.lastIndexOf( '.' );
                    final String extension;
                    
                    if( lastdot == -1 )
                    {
                        extension = "";
                    }
                    else
                    {
                        extension = trimmedFileName.substring( lastdot + 1 );
                    }
                    
                    boolean match = false;
                    
                    if( extension != null && extension.length() != 0 )
                    {
                        for( String ext : extensions )
                        {
                            if( extension.equalsIgnoreCase( ext ) )
                            {
                                match = true;
                                break;
                            }
                        }
                    }
                    
                    if( ! match )
                    {
                        final String message;
                        
                        if( count == 1 )
                        {
                            message = invalidFileExtensionOne.format( trimmedFileName, extensions.get( 0 ) );
                        }
                        else if( count == 2 )
                        {
                            message = invalidFileExtensionTwo.format( trimmedFileName, extensions.get( 0 ), extensions.get( 1 ) );
                        }
                        else
                        {
                            final StringBuilder buf = new StringBuilder();
                            
                            for( String ext : extensions )
                            {
                                if( buf.length() != 0 )
                                {
                                    buf.append( ", " );
                                }
                                
                                buf.append( ext );
                            }
                            
                            message = invalidFileExtensionMultiple.format( trimmedFileName, buf.toString() ); 
                        }
                        
                        return Status.createErrorStatus( message );
                    }
                }
            }
        }
        
        return Status.createOkStatus();
    }
    
}
