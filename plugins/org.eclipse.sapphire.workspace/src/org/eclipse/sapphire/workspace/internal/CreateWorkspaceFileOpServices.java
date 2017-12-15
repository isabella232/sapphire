/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.workspace.internal;

import static org.eclipse.sapphire.workspace.CreateWorkspaceFileOp.PROBLEM_FILE_EXISTS;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FileName;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.PropertyContentEvent;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.FileExtensionsService;
import org.eclipse.sapphire.services.InitialValueService;
import org.eclipse.sapphire.services.InitialValueServiceData;
import org.eclipse.sapphire.services.ValidationService;
import org.eclipse.sapphire.workspace.CreateWorkspaceFileOp;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class CreateWorkspaceFileOpServices
{
    public CreateWorkspaceFileOpServices() {}
    
    public static final class FolderValidationService extends ValidationService
    {
        @Override
        public Status validate()
        {
            final Value<Path> target = context( IModelElement.class ).read( context( ValueProperty.class ) );
            final Path path = target.getContent();
            
            if( path != null && path.segmentCount() > 0 )
            {
                final String projectName = path.segment( 0 );
                final IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember( projectName );
                
                if( resource == null || ! ( resource instanceof IProject && resource.isAccessible() ) )
                {
                    final String msg = NLS.bind( Resources.projectDoesNotExist, projectName );
                    return Status.createErrorStatus( msg );
                }
            }
            
            return Status.createOkStatus();
        }
    }
    
    public static final class FolderInitialValueService extends InitialValueService 
    {
        @Override
        protected void initInitialValueService()
        {
            final Listener listener = new FilteredListener<PropertyContentEvent>()
            {
                @Override
                protected void handleTypedEvent( final PropertyContentEvent event )
                {
                    refresh();
                }
            };
            
            context( CreateWorkspaceFileOp.class ).attach( listener, CreateWorkspaceFileOp.PROP_CONTEXT );
        }
    
        @Override
        protected InitialValueServiceData compute()
        {
            final CreateWorkspaceFileOp op = context( CreateWorkspaceFileOp.class );
            
            IResource resource = op.getContext().content();
            
            if( resource instanceof IFile )
            {
                resource = resource.getParent();
            }
            
            return new InitialValueServiceData( resource == null ? null : resource.getFullPath().makeRelative().toPortableString() );
        }
    }
    
    public static final class FileNameValidationService extends ValidationService
    {
        @Override
        protected void init()
        {
            super.init();
            
            final CreateWorkspaceFileOp op = context( CreateWorkspaceFileOp.class );
            
            final Listener listener = new FilteredListener<PropertyContentEvent>()
            {
                @Override
                protected void handleTypedEvent( final PropertyContentEvent event )
                {
                    broadcast();
                }
            };
            
            op.attach( listener, CreateWorkspaceFileOp.PROP_FOLDER );
            op.attach( listener, CreateWorkspaceFileOp.PROP_OVERWRITE_EXISTING_FILE );
            
            final FileExtensionsService fileExtensionsService = op.service( CreateWorkspaceFileOp.PROP_FILE_NAME, FileExtensionsService.class );
            
            if( fileExtensionsService != null )
            {
                fileExtensionsService.attach
                (
                    new Listener()
                    {
                        @Override
                        public void handle( final Event event )
                        {
                            broadcast();
                        }
                    }
                );
            }
        }
        
        @Override
        public Status validate()
        {
            final CreateWorkspaceFileOp op = context( CreateWorkspaceFileOp.class );
            final Value<FileName> value = op.read( context( ValueProperty.class ) );
            final FileName fileName = value.getContent();
            
            if( fileName != null )
            {
                final String extension = fileName.extension();
                
                if( extension != null )
                {
                    final FileExtensionsService fileExtensionsService = op.service( CreateWorkspaceFileOp.PROP_FILE_NAME, FileExtensionsService.class );
                    
                    if( fileExtensionsService != null )
                    {
                        final List<String> extensions = fileExtensionsService.extensions();
                        final int count = extensions.size();
                        
                        if( count > 0 )
                        {
                            boolean match = false;
                            
                            for( String ext : extensions )
                            {
                                if( extension.equalsIgnoreCase( ext ) )
                                {
                                    match = true;
                                    break;
                                }
                            }
                            
                            if( ! match )
                            {
                                final String message;
                                
                                if( count == 1 )
                                {
                                    message = NLS.bind( Resources.invalidFileExtensionOne, extensions.get( 0 ) );
                                }
                                else if( count == 2 )
                                {
                                    message = NLS.bind( Resources.invalidFileExtensionTwo, extensions.get( 0 ), extensions.get( 1 ) );
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
                                    
                                    message = NLS.bind( Resources.invalidFileExtensionMultiple, buf.toString() ); 
                                }
                                
                                return Status.createWarningStatus( message );
                            }
                        }
                    }
                }
                
                final IFile fileHandle = op.getFileHandle();
                
                if( fileHandle != null && fileHandle.exists() && op.getOverwriteExistingFile().getContent() == false )
                {
                    final String msg = NLS.bind( Resources.fileExists, fileName );
                    return Status.factoryForLeaf().severity( Status.Severity.ERROR ).type( PROBLEM_FILE_EXISTS ).message( msg ).create();
                }
            }
            
            return Status.createOkStatus();
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String projectDoesNotExist;
        public static String fileExists;
        public static String invalidFileExtensionOne;
        public static String invalidFileExtensionTwo;
        public static String invalidFileExtensionMultiple;
        
        static
        {
            initializeMessages( CreateWorkspaceFileOpServices.class.getName(), Resources.class );
        }
    }
    
}
