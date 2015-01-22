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

package org.eclipse.sapphire.sdk.internal;

import static org.eclipse.sapphire.java.jdt.JdtUtil.findSourceFolders;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.InitialValueService;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.sdk.CreateExtensionManifestOp;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class CreateExtensionManifestOpServices
{
    @Text( "Sapphire extension manifest should be placed in the META-INF folder" )
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
            final Path path = context( Value.of( Path.class ) ).content();
            
            if( path != null )
            {
                final String lastSegment = path.lastSegment();
                
                if( lastSegment != null && ! lastSegment.equals( "META-INF" ) )
                {
                    return Status.createWarningStatus( invalidFolder.text() );
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
        protected String compute()
        {
            final CreateExtensionManifestOp op = context( CreateExtensionManifestOp.class );
            final IResource context = op.getContext().content();
            
            IContainer result = null;
            
            if( context != null )
            {
                final IProject project = context.getProject();
                final List<IContainer> sourceFolders = findSourceFolders( project );
                final IPath resourceFullPath = context.getFullPath();
                
                IContainer contextSourceFolder = null;
                
                for( final IContainer sourceFolder : sourceFolders )
                {
                    if( sourceFolder.getFullPath().isPrefixOf( resourceFullPath ) )
                    {
                        contextSourceFolder = sourceFolder;
                        break;
                    }
                }
                
                if( contextSourceFolder != null )
                {
                    result = contextSourceFolder.getFolder( new org.eclipse.core.runtime.Path( "META-INF" ) );
                    
                    if( ! result.isAccessible() )
                    {
                        result = null;
                    }
                }
                
                if( result == null )
                {
                    result = project.getFolder( new org.eclipse.core.runtime.Path( "META-INF" ) );
                    
                    if( ! result.isAccessible() )
                    {
                        if( context instanceof IContainer )
                        {
                            result = (IContainer) context;
                        }
                        else
                        {
                            result = context.getParent();
                        }
                    }
                }
            }
            
            return result == null ? null : result.getFullPath().makeRelative().toPortableString();
        }
        
        @Override
        public void dispose()
        {
            super.dispose();
            
            if( this.listener != null )
            {
                final CreateExtensionManifestOp op = context( CreateExtensionManifestOp.class );
                
                if( ! op.disposed() )
                {
                    op.property( CreateExtensionManifestOp.PROP_CONTEXT ).detach( this.listener );
                }
            }
        }
    }
    
}
