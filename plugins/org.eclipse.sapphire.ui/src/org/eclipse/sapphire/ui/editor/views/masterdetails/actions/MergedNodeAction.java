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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sapphire.ui.actions.Action;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class MergedNodeAction

    extends Action
    
{
    private final List<NodeAction> actions = new ArrayList<NodeAction>();
    
    public void addAction( final NodeAction action )
    {
        this.actions.add( action );
    }

    @Override
    protected Object run( final Shell shell )
    {
        for( NodeAction action : this.actions )
        {
            action.execute( shell );
        }

        return null;
    }
    
}
