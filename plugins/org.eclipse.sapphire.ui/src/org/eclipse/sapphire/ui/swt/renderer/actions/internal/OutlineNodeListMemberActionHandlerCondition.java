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

import java.util.List;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.SapphireCondition;
import org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsContentNode;
import org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsPage;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class OutlineNodeListMemberActionHandlerCondition 

    extends SapphireCondition
    
{
    @Override
    protected boolean evaluate()
    {
        final ISapphirePart part = getPart();
        
        if( part instanceof MasterDetailsContentNode )
        {
            return check( (MasterDetailsContentNode) part );
        }
        else if( part instanceof MasterDetailsPage )
        {
            final MasterDetailsPage page = (MasterDetailsPage) part;
            final List<MasterDetailsContentNode> nodes = page.getContentTree().getSelectedNodes();
            
            if( ! nodes.isEmpty() )
            {
                MasterDetailsContentNode parent = null;
                
                for( MasterDetailsContentNode node : page.getContentTree().getSelectedNodes() )
                {
                    if( parent == null )
                    {
                        parent = node.getParentNode();
                    }
                    else if( parent != node.getParentNode() )
                    {
                        return false;
                    }
                }
                
                for( MasterDetailsContentNode node : page.getContentTree().getSelectedNodes() )
                {
                    if( ! check( node ) )
                    {
                        return false;
                    }
                }
                
                return true;
            }
        }
        
        return false;
    }
    
    private static boolean check( final MasterDetailsContentNode node )
    {
        final IModelElement element = node.getModelElement();
        
        if( element.parent() instanceof ModelElementList<?> )
        {
            final ISapphirePart parentPart = node.getParentPart();
            
            if( parentPart != null && parentPart instanceof MasterDetailsContentNode )
            {
                final MasterDetailsContentNode parentNode = (MasterDetailsContentNode) parentPart;
                
                return ( element != parentNode.getLocalModelElement() );
            }
            
            return true;
        }
        
        return false;
    }

}