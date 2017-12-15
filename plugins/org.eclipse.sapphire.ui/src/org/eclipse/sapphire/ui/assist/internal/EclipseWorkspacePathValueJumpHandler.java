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

package org.eclipse.sapphire.ui.assist.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.EclipseWorkspacePath;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.assist.JumpHandler;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class EclipseWorkspacePathValueJumpHandler 

    extends JumpHandler
    
{
    @Override
    public boolean isApplicable( final ValueProperty property )
    {
        if( property.isOfType( IPath.class ) && property.hasAnnotation( EclipseWorkspacePath.class ) )
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public boolean canLocateJumpTarget( final SapphirePart part,
                                        final SapphireRenderingContext context,
                                        final IModelElement modelElement,
                                        final ValueProperty property )
    {
        final IPath path = ( property.<Value<IPath>>invokeGetterMethod( modelElement ) ).getContent( true );
        
        if( path != null )
        {
            final IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember( path );
            
            if( resource != null && resource.exists() && resource.getType() == IResource.FILE )
            {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public void jump( final SapphirePart part,
                      final SapphireRenderingContext context,
                      final IModelElement modelElement,
                      final ValueProperty property )
    {
        final IPath path = ( property.<Value<IPath>>invokeGetterMethod( modelElement ) ).getContent( true );
        
        if( path != null )
        {
            final IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember( path );
            
            if( resource != null && resource.exists() && resource.getType() == IResource.FILE )
            {
                final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                
                if( window != null )
                {
                    final IWorkbenchPage page = window.getActivePage();

                    try
                    {
                        IDE.openEditor( page, (IFile) resource );
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