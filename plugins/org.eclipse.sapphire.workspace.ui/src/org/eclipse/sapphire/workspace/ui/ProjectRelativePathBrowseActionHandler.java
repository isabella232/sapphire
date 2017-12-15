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

package org.eclipse.sapphire.workspace.ui;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.def.ISapphireActionHandlerDef;
import org.eclipse.sapphire.ui.swt.renderer.actions.RelativePathBrowseActionHandler;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ProjectRelativePathBrowseActionHandler 

    extends RelativePathBrowseActionHandler
    
{
    @Override
    public void init( final SapphireAction action,
                      final ISapphireActionHandlerDef def )
    {
        super.init( action, def );
        
        setLabel( Resources.label );
        addImage( PlatformUI.getWorkbench().getSharedImages().getImageDescriptor( SharedImages.IMG_OBJ_PROJECT ) );
    }

    @Override
    protected List<Path> getBasePaths()
    {
        final IProject project = getPart().getModelElement().adapt( IProject.class );
        
        if( project == null )
        {
            return Collections.emptyList();
        }
        else
        {
            return Collections.singletonList( new Path( project.getLocation().toPortableString() ) );
        }
    }
    
    private static final class Resources extends NLS 
    {
        public static String label;

        static 
        {
            initializeMessages( ProjectRelativePathBrowseActionHandler.class.getName(), Resources.class );
        }
    }

}