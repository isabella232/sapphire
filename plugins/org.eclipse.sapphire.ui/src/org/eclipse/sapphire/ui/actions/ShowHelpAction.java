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

package org.eclipse.sapphire.ui.actions;

import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.ui.SapphireImageCache;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class ShowHelpAction 

    extends Action 
    
{
    public static final String ACTION_ID = "show-help"; //$NON-NLS-1$
    
    public ShowHelpAction()
    {
        setId( ACTION_ID );
        setLabel( Resources.label );
        setImageDescriptor( SapphireImageCache.ACTION_SHOW_HELP );
    }
    
    @Override
    public boolean isVisible()
    {
        return ( getPart().getHelpContextId() != null );
    }

    @Override
    public void setVisible( boolean visible )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    protected final Object run( final Shell shell )
    {
        final String helpContextId = getPart().getHelpContextId();

        if( helpContextId != null )
        {
            PlatformUI.getWorkbench().getHelpSystem().displayHelp( helpContextId );
        }
        
        return null;
    }
    
    private static final class Resources 
    
        extends NLS
        
    {
        public static String label;

        static 
        {
            initializeMessages( ShowHelpAction.class.getName(), Resources.class );
        }
    }
    
}

