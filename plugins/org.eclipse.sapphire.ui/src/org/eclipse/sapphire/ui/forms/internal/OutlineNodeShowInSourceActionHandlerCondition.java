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
import org.eclipse.sapphire.ui.SapphireCondition;
import org.eclipse.sapphire.ui.SourceEditorService;
import org.eclipse.sapphire.ui.forms.MasterDetailsContentNodePart;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class OutlineNodeShowInSourceActionHandlerCondition extends SapphireCondition
{
    @Override
    protected boolean evaluate()
    {
        final MasterDetailsContentNodePart node = (MasterDetailsContentNodePart) getPart();
        final Element element = node.getLocalModelElement();
        
        if( element.adapt( SourceEditorService.class ) != null )
        {
            final MasterDetailsContentNodePart parent = node.getParentNode();
            
            if( parent == null || parent.getLocalModelElement() != node.getLocalModelElement() )
            {
                return true;
            }
        }
            
        return false;
    }

}