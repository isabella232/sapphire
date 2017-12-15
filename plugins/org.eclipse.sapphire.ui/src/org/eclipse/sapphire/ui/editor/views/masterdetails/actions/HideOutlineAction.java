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

package org.eclipse.sapphire.ui.editor.views.masterdetails.actions;

import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.SapphireImageCache;
import org.eclipse.sapphire.ui.actions.Action;
import org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsPage;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class HideOutlineAction

    extends Action
    
{
    public static final String ACTION_ID = "page:hide-outline"; //$NON-NLS-1$
    
    public HideOutlineAction()
    {
        setId( ACTION_ID );
        setType( Type.TOGGLE );
        setLabel( Resources.label );
        setImageDescriptor( SapphireImageCache.ACTION_HIDE_OUTLINE );
    }

    @Override
    protected Object run( final Shell shell )
    {
        final MasterDetailsPage page = getPart().getNearestPart( MasterDetailsPage.class );        
        page.setDetailsMaximized( isChecked() );
        
        return null;
    }
    
    @Override
    public void setPart( final ISapphirePart part )
    {
        super.setPart( part );
        
        final MasterDetailsPage page = part.getNearestPart( MasterDetailsPage.class );        
        setChecked( page.isDetailsMaximized() );
    }

    private static final class Resources
    
        extends NLS
    
    {
        public static String label;
        
        static
        {
            initializeMessages( HideOutlineAction.class.getName(), Resources.class );
        }
    }
    
}
