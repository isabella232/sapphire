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

package org.eclipse.sapphire.modeling.validators;

import static org.eclipse.sapphire.modeling.internal.SapphireModelingFrameworkPlugin.PLUGIN_ID;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.BasePathsProvider;
import org.eclipse.sapphire.modeling.annotations.BasePathsProviderImpl;
import org.eclipse.sapphire.modeling.annotations.FileSystemResourceType;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class RelativePathValueValidator

    extends PathValueValidator
    
{
    private final BasePathsProviderImpl basePathsProvider;
    
    public RelativePathValueValidator( final ValueProperty property )
    {
        super( property );
        
        final BasePathsProvider basePathsProviderAnnotation = property.getAnnotation( BasePathsProvider.class );
        final Class<? extends BasePathsProviderImpl> basePathsProviderClass = basePathsProviderAnnotation.value();
        
        try
        {
            this.basePathsProvider = basePathsProviderClass.newInstance();
        }
        catch( Exception e )
        {
            throw new RuntimeException( e );
        }
    }
    
    @Override
    public IStatus validate( final Value<IPath> value )
    {
        final IPath path = value.getContent();
        
        if( path != null )
        {
            final List<IPath> basePaths = this.basePathsProvider.getBasePaths( value.parent() );
            
            for( IPath basePath : basePaths )
            {
                final IPath absolutePath = basePath.append( path );
                final File absolutePathFile = absolutePath.toFile();
                
                if( absolutePathFile.exists() )
                {
                    if( this.validResourceType == FileSystemResourceType.FILE )
                    {
                        if( absolutePathFile.isFile() )
                        {
                            return validateExtensions( path );
                        }
                        else
                        {
                            final String message = NLS.bind( Resources.pathIsNotFile, absolutePath.toPortableString() );
                            return new Status( Status.ERROR, PLUGIN_ID, message );
                        }
                    }
                    else if( this.validResourceType == FileSystemResourceType.FOLDER )
                    {
                        if( ! absolutePathFile.isDirectory() )
                        {
                            final String message = NLS.bind( Resources.pathIsNotFolder, absolutePath.toPortableString() );
                            return new Status( Status.ERROR, PLUGIN_ID, message );
                        }
                    }
                    
                    return Status.OK_STATUS;
                }
            }
            
            if( this.resourceMustExist )
            {
                if( this.validResourceType == FileSystemResourceType.FILE )
                {
                    final String message = Resources.bind( Resources.fileMustExist, path.toString() );
                    return new Status( Status.ERROR, PLUGIN_ID, message );
                }
                else if( this.validResourceType == FileSystemResourceType.FOLDER )
                {
                    final String message = Resources.bind( Resources.folderMustExist, path.toString() );
                    return new Status( Status.ERROR, PLUGIN_ID, message );
                }
                else
                {
                    final String message = Resources.bind( Resources.resourceMustExist, path.toString() );
                    return new Status( Status.ERROR, PLUGIN_ID, message );
                }
            }
        }
        
        return Status.OK_STATUS;
    }
    
}
