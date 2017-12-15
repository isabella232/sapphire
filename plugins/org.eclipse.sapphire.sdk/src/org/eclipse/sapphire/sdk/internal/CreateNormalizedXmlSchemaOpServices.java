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

package org.eclipse.sapphire.sdk.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.PropertyContentEvent;
import org.eclipse.sapphire.platform.PathBridge;
import org.eclipse.sapphire.sdk.CreateNormalizedXmlSchemaOp;
import org.eclipse.sapphire.services.InitialValueService;
import org.eclipse.sapphire.services.InitialValueServiceData;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class CreateNormalizedXmlSchemaOpServices
{
    public CreateNormalizedXmlSchemaOpServices() {}
    
    public static final class SourceFileInitialValueService extends InitialValueService 
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
            
            context( CreateNormalizedXmlSchemaOp.class ).attach( this.listener, CreateNormalizedXmlSchemaOp.PROP_CONTEXT );
        }
    
        @Override
        protected InitialValueServiceData compute()
        {
            final CreateNormalizedXmlSchemaOp op = context( CreateNormalizedXmlSchemaOp.class );
            final IResource resource = op.getContext().content();
            
            if( resource instanceof IFile && resource.getName().toLowerCase().endsWith( ".xsd" ) )
            {
                return new InitialValueServiceData( resource.getFullPath().makeRelative().toPortableString() );
            }
            
            return new InitialValueServiceData( null );
        }
        
        @Override
        public void dispose()
        {
            super.dispose();
            
            if( this.listener != null )
            {
                context( CreateNormalizedXmlSchemaOp.class ).detach( this.listener, CreateNormalizedXmlSchemaOp.PROP_CONTEXT );
            }
        }
    }
    
    public static final class SourceFileListener extends FilteredListener<PropertyContentEvent>
    {
        @Override
        protected void handleTypedEvent( final PropertyContentEvent event )
        {
            final CreateNormalizedXmlSchemaOp op = (CreateNormalizedXmlSchemaOp) event.element();
            final Path sourceFilePath = op.getSourceFile().getContent();
            
            String folder = null;
            String fileName = null;
            
            if( sourceFilePath != null && sourceFilePath.segmentCount() >= 2 )
            {
                final IFile sourceFile = ResourcesPlugin.getWorkspace().getRoot().getFile( PathBridge.create( sourceFilePath ) );
                
                if( sourceFile.exists() )
                {
                    folder = sourceFile.getParent().getFullPath().makeRelative().toPortableString();
                    
                    fileName = sourceFile.getName();
                    
                    if( fileName.toLowerCase().endsWith( ".xsd" ) )
                    {
                        fileName = fileName.substring( 0, fileName.length() - 4 ) + "-normalized.xsd";
                    }
                    else
                    {
                        fileName = null;
                    }
                }
            }
            
            op.setFolder( folder );
            op.setFileName( fileName );
        }
    }
    
    public static final class FolderInitialValueService extends InitialValueService 
    {
        @Override
        protected InitialValueServiceData compute()
        {
            return new InitialValueServiceData( null );
        }
    }
    
}
