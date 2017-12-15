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

package org.eclipse.sapphire.ui.internal;

import java.util.List;

import org.eclipse.sapphire.ui.actions.Action;
import org.eclipse.sapphire.ui.actions.ActionGroup;
import org.eclipse.sapphire.ui.def.IActionDef;
import org.eclipse.sapphire.ui.def.IActionGroupDef;
import org.eclipse.sapphire.ui.def.IActionOverride;
import org.eclipse.sapphire.ui.def.IActionSetDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ActionsHostUtil
{
    public static void initActions( final List<ActionGroup> actions,
                                    final IActionSetDef actionSetDef )
    {
        if( actionSetDef != null )
        {
            if( actionSetDef.getSuppressDefaultActions().getContent() == true )
            {
                actions.clear();
            }
            else
            {
                for( IActionOverride override : actionSetDef.getOverrides() )
                {
                    final Action overridingAction = createAction( override.getAction() );
                    
                    if( overridingAction != null )
                    {
                        for( ActionGroup adg : actions )
                        {
                            for( Action ad : adg.getActions() )
                            {
                                if( ad.getId().equals( override.getId().getText() ) )
                                {
                                    adg.replaceAction( ad, overridingAction );
                                }
                            }
                        }
                    }
                }
            }
            
            for( IActionGroupDef agd : actionSetDef.getGroups() )
            {
                final ActionGroup ag = new ActionGroup();
                actions.add( ag );
                
                for( IActionDef ad : agd.getActionDefs() )
                {
                    final Action a = createAction( ad );
                    
                    if( a != null )
                    {
                        ag.addAction( a );
                    }
                }
            }
        }
    }
    
    private static Action createAction( final IActionDef def )
    {
        try
        {
            final Action action = (Action) def.getImplClass().resolve().newInstance();
            
            if( def.getId().getText() != null )
            {
                action.setId( def.getId().getText() );
            }
            
            if( def.getLabel().getText() != null )
            {
                action.setLabel( def.getLabel().getText() );
            }
            
            return action;
        }
        catch( Exception e )
        {
            SapphireUiFrameworkPlugin.log( e );
        }
        
        return null;
    }
    
}
