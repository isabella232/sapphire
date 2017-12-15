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

package org.eclipse.sapphire.ui.swt.renderer.actions.internal;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.BasePathsProvider;
import org.eclipse.sapphire.modeling.annotations.BasePathsProviderImpl;
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
    protected void refreshEnablementState()
    {
        final IModelElement element = getModelElement();
        final ValueProperty property = getProperty();
        
        final String relativePath = element.read( property ).getText( true );
        
        if( relativePath != null )
        {
            for( IPath basePath : getBasePaths( element, property ) )
            {
                final IPath absolutePath = basePath.append( relativePath );
                File absoluteFile = absolutePath.toFile();
                
                if( absoluteFile.exists() && absoluteFile.isFile() )
                {
                    setEnabled( true );
                }
            }
        }

        setEnabled( false );
    }

    @Override
    protected Object run( final SapphireRenderingContext context )
    {
        final IModelElement element = getModelElement();
        final ValueProperty property = getProperty();
        
        final String relativePath = element.read( property ).getText( true );
        
        if( relativePath != null )
        {
            File file = null;
            
            for( IPath basePath : getBasePaths( element, property ) )
            {
                final IPath absolutePath = basePath.append( relativePath );
                File absoluteFile = absolutePath.toFile();
                
                if( absoluteFile.exists() && absoluteFile.isFile() )
                {
                    file = absoluteFile;
                    break;
                }
            }
            
            if( file != null )
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
        
        return null;
    }

    private List<IPath> getBasePaths( final IModelElement modelElement,
                                      final ModelProperty property )
    {
        final BasePathsProvider basePathsProviderAnnotation = property.getAnnotation( BasePathsProvider.class );
        final Class<? extends BasePathsProviderImpl> basePathsProviderClass = basePathsProviderAnnotation.value();
        
        final BasePathsProviderImpl basePathsProvider;
        
        try
        {
            basePathsProvider = basePathsProviderClass.newInstance();
        }
        catch( Exception e )
        {
            throw new RuntimeException( e );
        }
        
        return basePathsProvider.getBasePaths( modelElement );
    }
    
}