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

import org.eclipse.sapphire.ui.SapphireCondition;
import org.eclipse.sapphire.ui.SapphireEditorFormPage;
import org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsContentNode;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class OutlineNodeShowInSourceActionHandlerCondition 

    extends SapphireCondition
    
{
    @Override
    protected boolean evaluate()
    {
        final MasterDetailsContentNode node = (MasterDetailsContentNode) getPart();
        final SapphireEditorFormPage page = node.getNearestPart( SapphireEditorFormPage.class );
        
        if( page.getSourceView() != null )
        {
            final MasterDetailsContentNode parent = node.getParentNode();
            
            if( parent == null || parent.getLocalModelElement() != node.getLocalModelElement() )
            {
                return true;
            }
        }
        
        return false;
    }

}