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
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.ui.SapphireCommands;
import org.eclipse.sapphire.ui.SapphireImageCache;
import org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsContentNode;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class NodeMoveDownAction

    extends NodeMoveAction
    
{
    public static final String ACTION_ID = "node:move-down"; //$NON-NLS-1$

    public NodeMoveDownAction()
    {
        setId( ACTION_ID );
        setLabel( Resources.moveDownActionLabel );
        setImageDescriptor( SapphireImageCache.ACTION_MOVE_DOWN );
        setCommandId( SapphireCommands.COMMAND_MOVE_DOWN );
    }
    
    public boolean isEnabled()
    {
        if( super.isEnabled() == false )
        {
            return false;
        }
        
        final IModelElement modelElement = getNode().getModelElement();
        final ModelElementList<?> list = (ModelElementList<?>) modelElement.getParent();
        return ( list.indexOf( modelElement ) < ( list.size() - 1 ) );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    
    protected final Object run( final Shell shell )
    {
        final MasterDetailsContentNode node = getNode();
        final IModelElement modelElement = node.getModelElement();
        final ModelElementList<IModelElement> list = (ModelElementList<IModelElement>) modelElement.getParent();
        
        list.moveDown( modelElement );
        node.getContentTree().notifyOfNodeStructureChange( node.getParentNode() );
        
        return null;
    }
    
    private static final class Resources
        
        extends NLS
    
    {
        public static String moveDownActionLabel;
        
        static
        {
            initializeMessages( NodeMoveDownAction.class.getName(), Resources.class );
        }
    }
    
}
