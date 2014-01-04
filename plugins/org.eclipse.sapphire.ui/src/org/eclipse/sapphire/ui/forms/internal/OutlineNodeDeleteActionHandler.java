/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.internal;

import static org.eclipse.sapphire.ui.util.MiscUtil.findSelectionPostDelete;

import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.forms.MasterDetailsContentNodePart;
import org.eclipse.sapphire.ui.forms.MasterDetailsEditorPagePart;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class OutlineNodeDeleteActionHandler extends SapphireActionHandler
{
    public static final String ID = "Sapphire.Outline.Delete";
    
    public OutlineNodeDeleteActionHandler()
    {
        setId( ID );
    }
    
    @Override
    protected Object run( final Presentation context )
    {
        final ISapphirePart part = getPart();
        final List<MasterDetailsContentNodePart> nodesToDelete;
        
        if( part instanceof MasterDetailsContentNodePart )
        {
            nodesToDelete = Collections.singletonList( (MasterDetailsContentNodePart) part );
        }
        else if( part instanceof MasterDetailsEditorPagePart )
        {
            nodesToDelete = ( (MasterDetailsEditorPagePart) part ).outline().getSelectedNodes();
        }
        else
        {
            throw new IllegalStateException();
        }
        
        final MasterDetailsContentNodePart parent = nodesToDelete.get( 0 ).getParentNode();
        final List<MasterDetailsContentNodePart> allSiblingNodes = parent.nodes().visible();
        
        MasterDetailsContentNodePart selectionPostDelete = findSelectionPostDelete( allSiblingNodes, nodesToDelete );
        
        if( selectionPostDelete == null )
        {
            selectionPostDelete = parent;
        }
        
        for( MasterDetailsContentNodePart node : nodesToDelete )
        {
            final Element element = node.getModelElement();
            final Property elementParent = element.parent();
            
            if( elementParent.definition() instanceof ListProperty )
            {
                ( (ElementList<?>) elementParent ).remove( element );
            }
            else
            {
                final ElementHandle<?> handle = (ElementHandle<?>) elementParent;
                
                if( handle.content() == element )
                {
                    handle.clear();
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
