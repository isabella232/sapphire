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

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.form.editors.masterdetails.MasterDetailsContentNode;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class OutlineNodeDeleteActionHandlerCondition extends OutlineNodeListMemberActionHandlerCondition
{
    @Override
    protected boolean check( final MasterDetailsContentNode node )
    {
        final IModelElement element = node.getModelElement();
        final ModelProperty property = element.getParentProperty();

        if( property != null && ! property.isReadOnly() )
        {
            if( super.check( node ) )
            {
                return true;
            }
            
            if( property instanceof ElementProperty )
            {
                final ISapphirePart parentPart = node.getParentPart();
                
                if( parentPart != null && parentPart instanceof MasterDetailsContentNode )
                {
                    final MasterDetailsContentNode parentNode = (MasterDetailsContentNode) parentPart;
                    
                    return ( element != parentNode.getLocalModelElement() );
                }
                
                return true;
            }
        }
        
        return false;
    }

}