/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.form.editors.masterdetails.internal;

import static org.eclipse.sapphire.ui.util.MiscUtil.findSelectionPostDelete;

import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.IModelParticle;
import org.eclipse.sapphire.modeling.ModelElementHandle;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.form.editors.masterdetails.MasterDetailsContentNode;
import org.eclipse.sapphire.ui.form.editors.masterdetails.MasterDetailsEditorPagePart;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class OutlineNodeDeleteActionHandler extends SapphireActionHandler
{
    public static final String ID = "Sapphire.Outline.Delete";
    
    public OutlineNodeDeleteActionHandler()
    {
        setId( ID );
    }
    
    @Override
    protected Object run( final SapphireRenderingContext context )
    {
        final ISapphirePart part = getPart();
        final List<MasterDetailsContentNode> nodesToDelete;
        
        if( part instanceof MasterDetailsContentNode )
        {
            nodesToDelete = Collections.singletonList( (MasterDetailsContentNode) part );
        }
        else if( part instanceof MasterDetailsEditorPagePart )
        {
            nodesToDelete = ( (MasterDetailsEditorPagePart) part ).outline().getSelectedNodes();
        }
        else
        {
            throw new IllegalStateException();
        }
        
        final MasterDetailsContentNode parent = nodesToDelete.get( 0 ).getParentNode();
        final List<MasterDetailsContentNode> allSiblingNodes = parent.nodes().visible();
        
        MasterDetailsContentNode selectionPostDelete = findSelectionPostDelete( allSiblingNodes, nodesToDelete );
        
        if( selectionPostDelete == null )
        {
            selectionPostDelete = parent;
        }
        
        for( MasterDetailsContentNode node : nodesToDelete )
        {
            final IModelElement element = node.getModelElement();
            final IModelParticle elementParent = element.parent();
            
            if( elementParent instanceof ModelElementList )
            {
                ( (ModelElementList<?>) elementParent ).remove( element );
            }
            else
            {
                final ElementProperty property = (ElementProperty) element.getParentProperty();
                final ModelElementHandle<?> handle = ( (IModelElement) elementParent ).read( property );
                
                if( handle.element() == element )
                {
                    handle.remove();
                }
            }
        }
        
        if( selectionPostDelete != null )
        {
            selectionPostDelete.getContentTree().setSelectedNode( selectionPostDelete );
        }
        
        return null;
    }
    
}
