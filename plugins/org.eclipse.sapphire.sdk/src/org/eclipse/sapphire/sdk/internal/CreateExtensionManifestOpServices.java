/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.sdk.internal;

import static org.eclipse.sapphire.java.jdt.JdtUtil.findSourceFolder;
import static org.eclipse.sapphire.java.jdt.JdtUtil.findSourceFolders;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.sdk.CreateExtensionManifestOp;
import org.eclipse.sapphire.services.InitialValueService;
import org.eclipse.sapphire.services.InitialValueServiceData;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class CreateExtensionManifestOpServices
{
    @Text( "Sapphire extension manifest should be placed in a META-INF folder under a Java source folder. Folder \"{0}\" is invalid" )
    private static LocalizableText invalidFolder;  
    
    static
    {
        LocalizableText.init( CreateExtensionManifestOpServices.class );
    }

    private CreateExtensionManifestOpServices() {}
    
    public static final class FolderValidationService extends ValidationService
    {
        @Override
        protected Status compute()
        {
            final Value<Path> target = context( Element.class ).property( context( ValueProperty.class ) );
            final Path path = target.content();
            
            if( path != null )
            {
                final IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember( path.toPortableString() );
                
                if( resource != null )
                {
                    final IPath pathRelativeToWorkspace = resource.getFullPath();
                    boolean locatedInSourceFolder = false;
                    
                    for( IContainer sourceFolder : findSourceFolders( resource ) )
                    {
                        final IPath sourceFolderFullPath = sourceFolder.getFullPath();
                        
                        if( sourceFolderFullPath.isPrefixOf( pathRelativeToWorkspace ) )
                        {
                            locatedInSourceFolder = true;
                            
                            final IPath pathRelativeToSourceFolder = pathRelativeToWorkspace.makeRelativeTo( sourceFolderFullPath );
                            
                            if( ! pathRelativeToSourceFolder.equals( new org.eclipse.core.runtime.Path( "META-INF" ) ) )
                            {
                                final String msg = invalidFolder.format( path.toPortableString() );
                                return Status.createWarningStatus( msg );
                            }
                        }
                    }
                        
                    if( ! locatedInSourceFolder )
                    {
                        final String msg = invalidFolder.format( path.toPortableString() );
                        return Status.createWarningStatus( msg );
                    }
                }
            }
            
            return Status.createOkStatus();
        }
    }
    
    public static final class FolderInitialValueService extends InitialValueService 
    {
        private Listener listener;
        
        @Override
        protected void initInitialValueService()
        {
            this.listener = new FilteredListener<PropertyContentEvent>()
            {
                @Override
                protected void handleTypedEvent( final PropertyContentEvent event )
                {
                    refresh();
                }
            };
            
            context( CreateExtensionManifestOp.class ).property( CreateExtensionManifestOp.PROP_CONTEXT ).attach( this.listener );
        }
    
        @Override
        protected InitialValueServiceData compute()
        {
            final CreateExtensionManifestOp op = context( CreateExtensionManifestOp.class );
    
            IResource folder = op.getContext().content();
            
            if( folder != null )
            {
                final IContainer sourceFolder = findSourceFolder( folder );
                
                if( sourceFolder != null )
                {
                    folder = sourceFolder.getFolder( new org.eclipse.core.runtime.Path( "META-INF" ) );
                }
            }
            
            return new InitialValueServiceData( folder == null ? null : folder.getFullPath().makeRelative().toPortableString() );
        }
        
        @Override
        public void dispose()
        {
            super.dispose();
            
            if( this.listener != null )
            {
                context( CreateExtensionManifestOp.class ).property( CreateExtensionManifestOp.PROP_CONTEXT ).detach( this.listener );
            }
        }
    }
    
}
