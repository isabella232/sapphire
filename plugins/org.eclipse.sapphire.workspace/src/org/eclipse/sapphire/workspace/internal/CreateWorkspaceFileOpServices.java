/******************************************************************************
 * Copyright (c) 2015 Oracle
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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FileName;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.InitialValueService;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.ReferenceValue;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.platform.PathBridge;
import org.eclipse.sapphire.services.FileExtensionsService;
import org.eclipse.sapphire.services.ReferenceService;
import org.eclipse.sapphire.services.RelativePathService;
import org.eclipse.sapphire.services.ValidationService;
import org.eclipse.sapphire.util.ListFactory;
import org.eclipse.sapphire.workspace.CreateWorkspaceFileOp;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class CreateWorkspaceFileOpServices
{
    @Text( "Folder must be specified" )
    private static LocalizableText folderMustBeSpecified;

    @Text( "Project \"{0}\" does not exist or is not accessible" )
    private static LocalizableText projectDoesNotExist;
    
    @Text( "File \"{0}\" already exists" )
    private static LocalizableText fileExists;
    
    @Text( "File extension should be \"{0}\"" )
    private static LocalizableText invalidFileExtensionOne;
    
    @Text( "File extension should be \"{0}\" or \"{1}\"" )
    private static LocalizableText invalidFileExtensionTwo;
    
    @Text( "File extension should be one of \"{0}\"" )
    private static LocalizableText invalidFileExtensionMultiple;

    static
    {
        LocalizableText.init( CreateWorkspaceFileOpServices.class );
    }

    public CreateWorkspaceFileOpServices() {}
    
    public static final class RootReferenceService extends ReferenceService<IContainer>
    {
        @Override
        protected IContainer compute()
        {
            final String reference = context( Value.class ).text();
            final IWorkspaceRoot wsroot = ResourcesPlugin.getWorkspace().getRoot();
            
            if( reference == null )
            {
                return wsroot;
            }
            else
            {
                final Path root = new Path( reference );
                
                if( root.segmentCount() == 1 )
                {
                    return wsroot.getProject( root.segment( 0 ) );
                }
                else
                {
                    return wsroot.getFolder( PathBridge.create( root ) );
                }
            }
        }
    }
    
    public static final class FolderReferenceService extends ReferenceService<IContainer>
    {
        @Override
        protected void initReferenceService()
        {
            context( CreateWorkspaceFileOp.class ).getRoot().service( ReferenceService.class ).attach
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

        @Override
        protected IContainer compute()
        {
            final String reference = context( Value.class ).text();
            final CreateWorkspaceFileOp op = context( CreateWorkspaceFileOp.class );
            final IContainer root = op.getRoot().target();
            
            if( reference == null )
            {
                return root;
            }
            else
            {
                final IPath path = new org.eclipse.core.runtime.Path( reference );
                
                if( root instanceof IWorkspaceRoot && path.segmentCount() == 1 )
                {
                    return ( (IWorkspaceRoot) root ).getProject( path.segment( 0 ) );
                }
                else
                {
                    return root.getFolder( path );
                }
            }
        }
    }

    public static final class FolderRelativePathService extends RelativePathService
    {
        @Override
        public List<Path> roots()
        {
            final CreateWorkspaceFileOp op = context( CreateWorkspaceFileOp.class );
            final IContainer root = op.getRoot().target();
            
            if( root == null )
            {
                return ListFactory.empty();
            }
            else
            {
                return ListFactory.singleton( new Path( root.getLocation().toString() ) );
            }
        }
        
        @Override
        public Path convertToRelative( final Path path )
        {
            final CreateWorkspaceFileOp op = context( CreateWorkspaceFileOp.class );
            final IContainer root = op.getRoot().target();
            
            if( root instanceof IWorkspaceRoot )
            {
                for( final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects() )
                {
                    final Path location = new Path( project.getLocation().toPortableString() );
                    
                    if( location.isPrefixOf( path ) )
                    {
                        return new Path( project.getName() ).append( path.makeRelativeTo( location ) );
                    }
                }
            }
            else
            {
                super.convertToRelative( path );
            }
            
            return null;
        }

        @Override
        public Path convertToAbsolute( final Path path )
        {
            final CreateWorkspaceFileOp op = context( CreateWorkspaceFileOp.class );
            final IContainer root = op.getRoot().target();
            
            if( root instanceof IWorkspaceRoot )
            {
                if( path.segmentCount() > 0 )
                {
                    final IProject project = ( (IWorkspaceRoot) root ).getProject( path.segment( 0 ) );
                    return new Path( project.getLocation().toString() ).append( path.removeFirstSegments( 1 ) );
                }
            }
            else
            {
                super.convertToAbsolute( path );
            }
            
            return null;
        }
    }
    
    public static final class FolderValidationService extends ValidationService
    {
        @Override
        protected void initValidationService()
        {
            context( Value.class ).service( ReferenceService.class ).attach
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

        @Override
        protected Status compute()
        {
            final ReferenceValue<Path,IContainer> value = context( ReferenceValue.of( Path.class, IContainer.class ) );
            final CreateWorkspaceFileOp op = value.nearest( CreateWorkspaceFileOp.class );
            
            if( value.empty() && op.getRoot().empty() )
            {
                return Status.createErrorStatus( folderMustBeSpecified.text() );
            }
            
            final IContainer folder = context( ReferenceValue.of( Path.class, IContainer.class ) ).target();
            
            if( folder != null )
            {
                final IProject project = folder.getProject();
                
                if( project != null && ! project.isAccessible() )
                {
                    final String msg = projectDoesNotExist.format( project.getName() );
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
            
            context( CreateWorkspaceFileOp.class ).getContext().attach( listener );
        }
    
        @Override
        protected String compute()
        {
            final CreateWorkspaceFileOp op = context( CreateWorkspaceFileOp.class );
            
            IResource resource = op.getContext().content();
            
            if( resource instanceof IFile )
            {
                resource = resource.getParent();
            }
            
            return resource == null ? null : resource.getFullPath().makeRelative().toPortableString();
        }
    }
    
    public static final class FileReferenceService extends ReferenceService<IFile>
    {
        @Override
        protected void initReferenceService()
        {
            context( CreateWorkspaceFileOp.class ).getFolder().service( ReferenceService.class ).attach
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

        @Override
        protected IFile compute()
        {
            final String reference = context( Value.class ).text();
            final CreateWorkspaceFileOp op = context( CreateWorkspaceFileOp.class );
            final IContainer folder = op.getFolder().target();
            
            if( reference == null || folder == null || folder instanceof IWorkspaceRoot )
            {
                return null;
            }
            else
            {
                return folder.getFile(  new org.eclipse.core.runtime.Path( reference ) );
            }
        }
    }

    public static final class FileValidationService extends ValidationService
    {
        @Override
        protected void initValidationService()
        {
            final Value<?> value = context( Value.class );
            final CreateWorkspaceFileOp op = value.nearest( CreateWorkspaceFileOp.class );

            op.getOverwriteExistingFile().attach
            (
                new FilteredListener<PropertyContentEvent>()
                {
                    @Override
                    protected void handleTypedEvent( final PropertyContentEvent event )
                    {
                        refresh();
                    }
                }
            );
            
            value.service( ReferenceService.class ).attach
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
            
            final FileExtensionsService fileExtensionsService = value.service( FileExtensionsService.class );
            
            if( fileExtensionsService != null )
            {
                fileExtensionsService.attach
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
        
        @Override
        protected Status compute()
        {
            final Value<?> value = context( Value.class );
            final CreateWorkspaceFileOp op = value.nearest( CreateWorkspaceFileOp.class );
            final FileName fileName = (FileName) value.content();
            
            if( fileName != null )
            {
                final String extension = fileName.extension();
                
                if( extension != null )
                {
                    final FileExtensionsService fileExtensionsService = value.service( FileExtensionsService.class );
                    
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
                                    message = invalidFileExtensionOne.format( extensions.get( 0 ) );
                                }
                                else if( count == 2 )
                                {
                                    message = invalidFileExtensionTwo.format( extensions.get( 0 ), extensions.get( 1 ) );
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
                                    
                                    message = invalidFileExtensionMultiple.format( buf.toString() ); 
                                }
                                
                                return Status.createWarningStatus( message );
                            }
                        }
                    }
                }
                
                final IFile fileHandle = op.getFile().target();
                
                if( fileHandle != null && fileHandle.exists() && op.getOverwriteExistingFile().content() == false )
                {
                    final String msg = fileExists.format( fileName );
                    return Status.factoryForLeaf().severity( Status.Severity.ERROR ).type( PROBLEM_FILE_EXISTS ).message( msg ).create();
                }
            }
            
            return Status.createOkStatus();
        }
    }
    
}
