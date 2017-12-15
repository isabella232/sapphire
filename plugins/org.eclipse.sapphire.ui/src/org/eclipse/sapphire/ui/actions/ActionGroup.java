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

package org.eclipse.sapphire.ui.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.ui.ISapphirePart;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ActionGroup
{
    private final List<Action> actions;
    private final List<Action> actionsReadOnly;
    
    public ActionGroup()
    {
        this.actions = new ArrayList<Action>();
        this.actionsReadOnly = Collections.unmodifiableList( this.actions );
    }
    
    public ActionGroup( final Action action )
    {
        this();
        this.actions.add( action );
    }
    
    public ActionGroup( final Collection<Action> actions )
    {
        this();
        
        for( Action action : actions )
        {
            this.actions.add( action );
        }
    }
    
    public boolean isVisible()
    {
        for( Action action : this.actions )
        {
            if( action.isVisible() )
            {
                return true;
            }
        }
        
        return false;
    }
    
    public List<Action> getActions()
    {
        return this.actionsReadOnly;
    }
    
    public Action getAction( final String id )
    {
        for( Action action : this.actionsReadOnly )
        {
            if( action.getId().equals( id ) )
            {
                return action;
            }
            
            for( ActionGroup group : action.getChildActionGroups() )
            {
                final Action childAction = group.getAction( id );
                
                if( childAction != null )
                {
                    return childAction;
                }
            }
        }
        
        return null;
    }
    
    public void addAction( final Action action )
    {
        this.actions.add( action );
    }
    
    public void replaceAction( final Action actionToReplace,
                               final Action actionToReplaceWith )
    {
        final int index = this.actions.indexOf( actionToReplace );
        
        if( index != -1 )
        {
            this.actions.set( index, actionToReplaceWith );
        }
    }
    
    public void setPart( final ISapphirePart part )
    {
        for( Action action : this.actions )
        {
            action.setPart( part );
        }
    }
    
    public void dispose()
    {
        for( Action action : this.actions )
        {
            action.dispose();
        }
    }
}
