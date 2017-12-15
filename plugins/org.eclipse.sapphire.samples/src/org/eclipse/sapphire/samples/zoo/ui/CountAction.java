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

package org.eclipse.sapphire.samples.zoo.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsContentNode;
import org.eclipse.sapphire.ui.editor.views.masterdetails.actions.NodeAction;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class CountAction

    extends NodeAction
    
{
    public static final String ACTION_ID = "node:count"; //$NON-NLS-1$
    
    public CountAction()
    {
        setId( ACTION_ID );
        setLabel( Resources.countActionLabel );
    }
    
    @Override
    protected Object run( final Shell shell )
    {
        final MasterDetailsContentNode node = getNode();
        final int count = node.getChildNodes().size();
        
        final String message 
            = NLS.bind( Resources.countDialogMessage, node.getLabel(), String.valueOf( count ) );
        
        MessageDialog.openInformation( shell, Resources.countDialogTitle, message );
        
        return null;
    }
    
    private static final class Resources
    
        extends NLS
    
    {
        public static String countActionLabel;
        public static String countDialogTitle;
        public static String countDialogMessage;
        
        static
        {
            initializeMessages( CountAction.class.getName(), Resources.class );
        }
    }
    
}
