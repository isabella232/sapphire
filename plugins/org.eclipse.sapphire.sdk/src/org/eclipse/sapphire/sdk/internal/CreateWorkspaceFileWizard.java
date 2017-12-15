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
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sapphire.platform.PathBridge;
import org.eclipse.sapphire.ui.swt.SapphireWizard;
import org.eclipse.sapphire.workspace.CreateWorkspaceFileOp;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class CreateWorkspaceFileWizard<M extends CreateWorkspaceFileOp> 

    extends SapphireWizard<M>
    implements IWorkbenchWizard
    
{
    public CreateWorkspaceFileWizard( final M modelElement,
                             final String wizardDefPath )
    {
        super( modelElement, wizardDefPath );
    }
    
    public void init( final IWorkbench workbench,
                      final IStructuredSelection selection )
    {
        if( ! selection.isEmpty() )
        {
            final Object obj = selection.getFirstElement();
            IResource resource = null;
            
            if( obj instanceof IResource )
            {
                resource = (IResource) obj;
            }
            else
            {
                resource = (IResource) Platform.getAdapterManager().getAdapter( obj, IResource.class );
            }
            
            if( resource != null )
            {
                if( resource instanceof IFile )
                {
                    resource = resource.getParent();
                }
                
                final CreateWorkspaceFileOp op = getModelElement();
                op.setFolder( PathBridge.create( resource.getFullPath().makeRelative() ) );
            }
        }
    }

    @Override
    protected void performPostFinish() 
    {
        openFileEditors( getModelElement().getFileHandle() );
    }
    
}
