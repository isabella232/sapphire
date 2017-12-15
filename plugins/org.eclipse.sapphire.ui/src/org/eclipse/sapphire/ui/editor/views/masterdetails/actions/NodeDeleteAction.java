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

import java.util.List;

import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.IRemovable;
import org.eclipse.sapphire.ui.SapphireImageCache;
import org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsContentNode;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class NodeDeleteAction

    extends NodeAction
    
{
    public static final String ACTION_ID = "node:delete"; //$NON-NLS-1$
    
    public NodeDeleteAction()
    {
        setId( ACTION_ID );
        setLabel( Resources.deleteActionLabel );
        setImageDescriptor( SapphireImageCache.ACTION_DELETE );
        setCommandId( "org.eclipse.ui.edit.delete" );
        setMergingAllowed( true );
    }
    
    public boolean isEnabled()
    {
        final IModelElement modelElement = getNode().getModelElement();
        return ( modelElement instanceof IRemovable );
    }
    
    @Override
    protected final Object run( final Shell shell )
    {
        final MasterDetailsContentNode node = getNode();
        MasterDetailsContentNode newSelection = null;
 
        if( node.getContentTree().getSelectedNodes().contains( node ) )
        {
            final MasterDetailsContentNode parent = node.getParentNode();
            final List<MasterDetailsContentNode> siblings = parent.getChildNodes();
            final int size = siblings.size();
            
            if( size == 1 )
            {
                newSelection = parent;
            }
            else
            {
                final int indexOfRemovedNode = siblings.indexOf( node );
                
                final int indexOfNewSelection
                    = ( indexOfRemovedNode == 0 ? 1 : indexOfRemovedNode - 1 );
                
                newSelection = siblings.get( indexOfNewSelection );
            }
        }
            
        ( (IRemovable) node.getModelElement() ).remove();
        
        if( newSelection != null )
        {
            node.getContentTree().setSelectedNode( newSelection );
        }
        
        return null;
    }
    
    private static final class Resources
    
        extends NLS
    
    {
        public static String deleteActionLabel;
        
        static
        {
            initializeMessages( NodeDeleteAction.class.getName(), Resources.class );
        }
    }
    
}
