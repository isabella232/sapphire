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

package org.eclipse.sapphire.ui.swt.renderer.actions.internal;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsContentNode;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class OutlineNodeMoveUpActionHandler

    extends OutlineNodeMoveActionHandler
    
{
    public static final String ID = "Sapphire.Outline.Move.Up";
    
    public OutlineNodeMoveUpActionHandler()
    {
        setId( ID );
    }
    
    @SuppressWarnings( "unchecked" )
    @Override
    
    protected Object run( final SapphireRenderingContext context )
    {
        final MasterDetailsContentNode node = (MasterDetailsContentNode) getPart();
        final IModelElement element = node.getModelElement();
        final ModelElementList<IModelElement> list = (ModelElementList<IModelElement>) element.parent();
        
        list.moveUp( element );
        node.getContentTree().notifyOfNodeStructureChange( node.getParentNode() );
        
        return null;
    }
    
    @Override
    protected boolean computeEnabledState()
    {
        boolean enabled = super.computeEnabledState();
        
        if( enabled )
        {
            final IModelElement element = getModelElement();
            final ModelElementList<?> list = (ModelElementList<?>) element.parent();
            enabled = ( list.indexOf( element ) > 0 );
        }
        
        return enabled;
    }
    
}
