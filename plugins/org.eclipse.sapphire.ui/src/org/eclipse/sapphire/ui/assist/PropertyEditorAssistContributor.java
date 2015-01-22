/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.assist;

import org.eclipse.sapphire.Disposable;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.ListenerContext;
import org.eclipse.sapphire.ui.SapphirePart;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class PropertyEditorAssistContributor implements Disposable
{
    public static final String ID_INFO_SECTION_CONTRIBUTOR = "System.InfoSectionContributor";
    public static final String ID_FACTS_CONTRIBUTOR = "System.FactsContributor";
    public static final String ID_PROBLEMS_SECTION_CONTRIBUTOR = "System.ProblemsSectionContributor";
    public static final String ID_PROBLEMS_CONTRIBUTOR = "System.ProblemsContributor";
    public static final String ID_ACTIONS_SECTION_CONTRIBUTOR = "System.ActionsSectionContributor";
    public static final String ID_RESET_ACTIONS_CONTRIBUTOR = "System.ResetActionsContributor";
    public static final String ID_RESTORE_INITIAL_VALUE_ACTIONS_CONTRIBUTOR = "System.RestoreInitialValueActionsContributor";
    public static final String ID_SHOW_IN_SOURCE_ACTION_CONTRIBUTOR = "System.ShowInSourceActionContributor";

    public static final int PRIORITY_INFO_SECTION_CONTRIBUTOR = 100;
    public static final int PRIORITY_FACTS_CONTRIBUTOR = 110;
    public static final int PRIORITY_PROBLEMS_SECTION_CONTRIBUTOR = 200;
    public static final int PRIORITY_PROBLEMS_CONTRIBUTOR = 210;
    public static final int PRIORITY_ACTIONS_SECTION_CONTRIBUTOR = 300;
    public static final int PRIORITY_RESET_ACTIONS_CONTRIBUTOR = 310;
    public static final int PRIORITY_RESTORE_INITIAL_VALUE_ACTIONS_CONTRIBUTOR = 315;
    public static final int PRIORITY_SHOW_IN_SOURCE_ACTION_CONTRIBUTOR = 320;
    
    public static final String SECTION_ID_INFO = "info";
    public static final String SECTION_ID_PROBLEMS = "problems";
    public static final String SECTION_ID_ACTIONS = "actions";

    private String id = getClass().getName();
    private int priority = 1000;
    
    private final ListenerContext listeners = new ListenerContext();
    
    public void init( final SapphirePart part )
    {
        // Nothing to do by default.
    }
    
    public String getId()
    {
        return this.id;
    }
    
    public void setId( final String id )
    {
        this.id = id;
    }
    
    public int getPriority()
    {
        return this.priority;
    }
    
    public void setPriority( final int priority )
    {
        this.priority = priority;
    }
    
    public abstract void contribute( PropertyEditorAssistContext context );
    
    public final boolean attach( final Listener listener )
    {
        return this.listeners.attach( listener );
    }
    
    public final boolean detach( final Listener listener )
    {
        return this.listeners.detach( listener );
    }
    
    protected final void broadcast( final Event event )
    {
        this.listeners.broadcast( event );
    }

    protected final void broadcast()
    {
        broadcast( new Event() );
    }
    
    @Override
    public void dispose()
    {
        // Nothing to do by default.
    }
    
    protected static String escapeForXml( final String string )
    {
        final StringBuilder result = new StringBuilder();
        
        for( int i = 0, n = string.length(); i < n; i++ )
        {
            final char ch = string.charAt( i );
            
            if( ch == '<' )
            {
                result.append( "&lt;" );
            }
            else if( ch == '>' )
            {
                result.append( "&gt;" );
            }
            else if( ch == '&' )
            {
                result.append( "&amp;" );
            }
            else if( ch == '"' )
            {
                result.append( "&quot;" );
            }
            else if( ch == '\'' )
            {
                result.append( "&apos;" );
            }
            else
            {
                result.append( ch );
            }
        }
        
        return result.toString();
    }
    
}
