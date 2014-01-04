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

package org.eclipse.sapphire.ui.forms;

import static org.eclipse.sapphire.ui.SapphireActionSystem.CONTEXT_EDITOR_PAGE_OUTLINE_HEADER;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.SapphireEditorPagePart;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class MasterDetailsEditorPagePart extends SapphireEditorPagePart
{
    private FunctionResult outlineHeaderTextFunctionResult;
    private MasterDetailsContentOutline contentOutline;
    
    @Override
    protected void init()
    {
        super.init();
        
        final MasterDetailsEditorPageDef def = definition();

        this.outlineHeaderTextFunctionResult = initExpression
        (
            def.getOutlineHeaderText().content(),
            String.class,
            null,
            new Runnable()
            {
                public void run()
                {
                    broadcast( new OutlineHeaderTextEvent( MasterDetailsEditorPagePart.this ) );
                }
            }
        );

        this.contentOutline = new MasterDetailsContentOutline( this );
        this.contentOutline.getRoot();
        
        this.contentOutline.attach
        (
            new Listener()
            {
               @Override
                public void handle( final Event event )
                {
                    if( event instanceof MasterDetailsContentOutline.SelectionChangedEvent )
                    {
                        final MasterDetailsContentOutline.SelectionChangedEvent evt = (MasterDetailsContentOutline.SelectionChangedEvent) event;
                        final List<MasterDetailsContentNodePart> selection = evt.selection();

                        PropertiesViewContributionPart propertiesViewContribution = null;
                        
                        if( selection.size() == 1 )
                        {
                            propertiesViewContribution = selection.get( 0 ).getPropertiesViewContribution();
                        }
                        
                        setPropertiesViewContribution( propertiesViewContribution );
                    }
                }
            }
        );
    }

    @Override
    public MasterDetailsEditorPageDef definition()
    {
        return (MasterDetailsEditorPageDef) super.definition();
    }
    
    @Override
    public MasterDetailsEditorPageState state()
    {
        return (MasterDetailsEditorPageState) super.state();
    }
    
    public final String getOutlineHeaderText()
    {
        return (String) this.outlineHeaderTextFunctionResult.value();
    }
    
    @Override
    public Set<String> getActionContexts()
    {
        final Set<String> contexts = new HashSet<String>();
        contexts.addAll( super.getActionContexts() );
        contexts.add( CONTEXT_EDITOR_PAGE_OUTLINE_HEADER );
        return contexts;
    }
    
    @Override
    public String getMainActionContext()
    {
        return SapphireActionSystem.CONTEXT_EDITOR_PAGE;
    }
    
    public final MasterDetailsContentOutline outline()
    {
        return this.contentOutline;
    }
    
    public final void expandAllNodes()
    {
        for( MasterDetailsContentNodePart node : this.contentOutline.getRoot().nodes().visible() )
        {
            node.setExpanded( true, true );
        }
    }

    public final void collapseAllNodes()
    {
        for( MasterDetailsContentNodePart node : this.contentOutline.getRoot().nodes().visible() )
        {
            node.setExpanded( false, true );
        }
    }
    
    public final void setFocusOnDetails()
    {
        broadcast( new DetailsFocusRequested( this ) );
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.outlineHeaderTextFunctionResult != null )
        {
            this.outlineHeaderTextFunctionResult.dispose();
        }
        
        if( this.contentOutline != null )
        {
            this.contentOutline.dispose();
        }
    }
    
    public static final class OutlineHeaderTextEvent extends PartEvent
    {
        public OutlineHeaderTextEvent( final MasterDetailsEditorPagePart part )
        {
            super( part );
        }
    }
    
    public static final class DetailsFocusRequested extends PartEvent
    {
        public DetailsFocusRequested( final MasterDetailsEditorPagePart part )
        {
            super( part );
        }
    }
    
}
