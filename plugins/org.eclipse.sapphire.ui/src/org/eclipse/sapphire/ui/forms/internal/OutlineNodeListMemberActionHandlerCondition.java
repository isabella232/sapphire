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

import java.util.List;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.SapphireCondition;
import org.eclipse.sapphire.ui.forms.MasterDetailsContentNodePart;
import org.eclipse.sapphire.ui.forms.MasterDetailsEditorPagePart;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class OutlineNodeListMemberActionHandlerCondition extends SapphireCondition
{
    @Override
    protected boolean evaluate()
    {
        final ISapphirePart part = getPart();
        
        if( part instanceof MasterDetailsContentNodePart )
        {
            return check( (MasterDetailsContentNodePart) part );
        }
        else if( part instanceof MasterDetailsEditorPagePart )
        {
            final MasterDetailsEditorPagePart page = (MasterDetailsEditorPagePart) part;
            final List<MasterDetailsContentNodePart> nodes = page.outline().getSelectedNodes();
            
            if( ! nodes.isEmpty() )
            {
                MasterDetailsContentNodePart parent = null;
                
                for( MasterDetailsContentNodePart node : page.outline().getSelectedNodes() )
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
                
                for( MasterDetailsContentNodePart node : page.outline().getSelectedNodes() )
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
    
    protected boolean check( final MasterDetailsContentNodePart node )
    {
        final Element element = node.getModelElement();
        
        if( element.parent() instanceof ElementList && ! element.parent().definition().isReadOnly() )
        {
            final ISapphirePart parentPart = node.parent();
            
            if( parentPart != null && parentPart instanceof MasterDetailsContentNodePart )
            {
                final MasterDetailsContentNodePart parentNode = (MasterDetailsContentNodePart) parentPart;
                
                return ( element != parentNode.getLocalModelElement() );
            }
            
            return true;
        }
        
        return false;
    }

}