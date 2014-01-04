/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.editor;

import java.util.Collections;
import java.util.Set;

import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.diagram.def.IDiagramNodeDefaultActionDef;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class DiagramNodeDefaultActionPart extends SapphirePart 
{
    private IDiagramNodeDefaultActionDef definition;
    private SapphireActionHandler actionHandler;
    private String label;
    private String description;
    
    @Override
    protected void init()
    {
        super.init();
        
        this.definition = (IDiagramNodeDefaultActionDef)super.definition;
        final String actionId = this.definition.getActionId().content();
        final String actionHandlerId = this.definition.getActionHandlerId().content();
        final SapphireAction action = getAction( actionId );
        this.label = this.definition.getLabel().localized( CapitalizationType.FIRST_WORD_ONLY, false );
        this.description = this.definition.getLabel().localized( CapitalizationType.NO_CAPS, false );
        
        if( actionHandlerId == null )
        {
            this.actionHandler = action.getFirstActiveHandler();
        }
        else
        {
            for( SapphireActionHandler h : action.getActiveHandlers() )
            {
                if( h.getId().equalsIgnoreCase( actionHandlerId ) )
                {
                    this.actionHandler = h;
                    break;
                }
            }
        }        
    }
    
    @Override
    public Set<String> getActionContexts()
    {
        return Collections.singleton( SapphireActionSystem.CONTEXT_DIAGRAM_NODE );
    }
    
    public String getLabel()
    {
        return this.label;
    }

    public String getDescription()
    {
        return this.description;
    }
    
    public SapphireActionHandler getActionHandler()
    {
        return this.actionHandler;
    }
}
