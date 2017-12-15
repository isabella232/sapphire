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
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.ui.SapphireEditorFormPage;
import org.eclipse.sapphire.ui.SapphireImageCache;
import org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsContentNode;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class NodeShowInSourceAction

    extends NodeAction
    
{
    public static final String ACTION_ID = "node:show-in-source"; //$NON-NLS-1$
    
    public NodeShowInSourceAction()
    {
        setId( ACTION_ID );
        setLabel( Resources.actionLabel );
        setImageDescriptor( SapphireImageCache.ACTION_SHOW_IN_SOURCE );
        setCommandId( "sapphire.show.in.source" );
    }
    
    @Override
    protected final Object run( final Shell shell )
    {
        final MasterDetailsContentNode node = getNode();
        final SapphireEditorFormPage page = getPart().getNearestPart( SapphireEditorFormPage.class );
        
        page.showInSourceView( node.getLocalModelElement(), null );
        
        return null;
    }
    
    @Override
    public boolean isEnabled()
    {
        final MasterDetailsContentNode node = getNode();
        final SapphireEditorFormPage page = getPart().getNearestPart( SapphireEditorFormPage.class );
        final IModelElement element = node.getLocalModelElement();
        
        if( page.getSourceView() != null &&
            node.getContentTree().getRoot().getModelElement() != element &&
            page.isOptimalConversionPossible( element, null ) )
        {
            return true;
        }
        
        return false;
    }
    
    @Override
    public void setEnabled( final boolean enabled )
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean isVisible()
    {
        return ( getPart().getNearestPart( SapphireEditorFormPage.class ).getSourceView() != null );
    }
    
    @Override
    public void setVisible( final boolean visible )
    {
        throw new UnsupportedOperationException();
    }
    
    private static final class Resources
    
        extends NLS
    
    {
        public static String actionLabel;
        
        static
        {
            initializeMessages( NodeShowInSourceAction.class.getName(), Resources.class );
        }
    }
    
}
