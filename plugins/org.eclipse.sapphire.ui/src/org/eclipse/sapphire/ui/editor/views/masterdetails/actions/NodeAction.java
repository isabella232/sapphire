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

import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.actions.Action;
import org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsContentNode;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class NodeAction

    extends Action
    
{
    private boolean mergingAllowed = false;
    private MasterDetailsContentNode node = null;
    
    @Override
    public void setPart( final ISapphirePart part )
    {
        super.setPart( part );
        this.node = part.getNearestPart( MasterDetailsContentNode.class );        
    }

    public MasterDetailsContentNode getNode()
    {
        return this.node;
    }

    public boolean isMergingAllowed()
    {
        return this.mergingAllowed;
    }
    
    public void setMergingAllowed( final boolean mergingAllowed )
    {
        this.mergingAllowed = mergingAllowed;
    }
    
}
