/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.renderer.actions.internal;

import java.io.File;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.services.RelativePathService;
import org.eclipse.sapphire.ui.SapphireJumpActionHandler;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class RelativePathJumpActionHandler 

    extends SapphireJumpActionHandler
    
{
    public static final String ID = "Sapphire.Jump.Path.Relative";
    
    public RelativePathJumpActionHandler()
    {
        setId( ID );
    }
    
    @Override
    protected boolean computeEnablementState()
    {
        if( super.computeEnablementState() == true )
        {
            final IModelElement element = getModelElement();
            final ValueProperty property = getProperty();
            
            final String relativePath = element.read( property ).getText( true );
            
            if( relativePath != null )
            {
                final Path absolutePath = element.service( property, RelativePathService.class ).convertToAbsolute( relativePath );
                
                if( absolutePath != null )
                {
                    final File absoluteFile = absolutePath.toFile();
                    
                    if( absoluteFile.exists() && absoluteFile.isFile() )
                    {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }

    @Override
    protected Object run( final SapphireRenderingContext context )
    {
        final IModelElement element = getModelElement();
        final ValueProperty property = getProperty();
        
        final String relativePath = element.read( property ).getText( true );
        
        if( relativePath != null )
        {
            final Path absolutePath = element.service( property, RelativePathService.class ).convertToAbsolute( relativePath );
            
            if( absolutePath != null )
            {
                final File file = absolutePath.toFile();
                
                if( file.exists() && file.isFile() )
                {
                    final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    
                    if( window != null )
                    {
                        final IWorkbenchPage page = window.getActivePage();
                        IEditorDescriptor editorDescriptor = null;
                        
                        try
                        {
                            editorDescriptor = IDE.getEditorDescriptor( file.getName() );
                        }
                        catch( PartInitException e )
                        {
                            // No editor was found for this file type.
                        }
                        
                        if( editorDescriptor != null )
                        {
                            try
                            {
                                IDE.openEditor( page, file.toURI(), editorDescriptor.getId(), true );
                            } 
                            catch( PartInitException e ) 
                            {
                                SapphireUiFrameworkPlugin.log( e );
                            }
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
}