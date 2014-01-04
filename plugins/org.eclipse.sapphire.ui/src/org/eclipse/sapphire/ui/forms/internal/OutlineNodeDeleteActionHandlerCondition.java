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

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.forms.MasterDetailsContentNodePart;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class OutlineNodeDeleteActionHandlerCondition extends OutlineNodeListMemberActionHandlerCondition
{
    @Override
    protected boolean check( final MasterDetailsContentNodePart node )
    {
        final Element element = node.getModelElement();
        final Property property = element.parent();

        if( property != null && ! property.definition().isReadOnly() )
        {
            if( super.check( node ) )
            {
                return true;
            }
            
            if( property instanceof ElementHandle )
            {
                final ISapphirePart parentPart = node.parent();
                
                if( parentPart != null && parentPart instanceof MasterDetailsContentNodePart )
                {
                    final MasterDetailsContentNodePart parentNode = (MasterDetailsContentNodePart) parentPart;
                    
                    return ( element != parentNode.getLocalModelElement() );
                }
                
                return true;
            }
        }
        
        return false;
    }

}