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

package org.eclipse.sapphire.ui.assist;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class EclipseWorkspacePathValueBrowseHandler 

    extends RelativePathValueBrowseHandler
    
{
    @Override
    public String getLabel()
    {
        return Resources.label;
    }
    
    @Override
    public ImageDescriptor getImageDescriptor()
    {
        return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor( SharedImages.IMG_OBJ_PROJECT );
    }
    
    @Override
    protected List<IPath> getBasePaths()
    {
        return Collections.singletonList( ResourcesPlugin.getWorkspace().getRoot().getLocation() );
    }
    
    private static final class Resources extends NLS 
    {
        public static String label;

        static 
        {
            initializeMessages( EclipseWorkspacePathValueBrowseHandler.class.getName(), Resources.class );
        }
    }

}