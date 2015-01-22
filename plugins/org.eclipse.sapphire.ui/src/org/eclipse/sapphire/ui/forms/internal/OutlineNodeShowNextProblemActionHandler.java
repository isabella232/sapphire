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

package org.eclipse.sapphire.ui.forms.internal;

import java.util.List;

import org.eclipse.sapphire.DisposeEvent;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.forms.FormPart;
import org.eclipse.sapphire.ui.forms.MasterDetailsContentNodePart;
import org.eclipse.sapphire.ui.forms.MasterDetailsEditorPagePart;
import org.eclipse.sapphire.ui.forms.PageBookPart;
import org.eclipse.sapphire.ui.forms.ProblemsTraversalService;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.ui.forms.SectionPart;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class OutlineNodeShowNextProblemActionHandler extends SapphireActionHandler
{
    private final Status.Severity severity;
    private ProblemsTraversalService service;
    
    protected OutlineNodeShowNextProblemActionHandler( final Status.Severity severity )
    {
        if( ! ( severity == Status.Severity.ERROR || severity == Status.Severity.WARNING ) )
        {
            throw new IllegalArgumentException();
        }
        
        this.severity = severity;
    }
    
    @Override
    public void init( final SapphireAction action,
                      final ActionHandlerDef def )
    {
        super.init( action, def );
        
        final MasterDetailsContentNodePart node = (MasterDetailsContentNodePart) getPart();
        final MasterDetailsEditorPagePart page = node.nearest( MasterDetailsEditorPagePart.class );
        
        this.service = page.service( ProblemsTraversalService.class );
        
        final Listener listener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                refreshVisibility();
            }
        };
        
        this.service.attach( listener );
        
        attach
        (
            new FilteredListener<DisposeEvent>()
            {
                @Override
                protected void handleTypedEvent( final DisposeEvent event )
                {
                    OutlineNodeShowNextProblemActionHandler.this.service.detach( listener );
                }
            }
        );
        
        refreshVisibility();
    }
    
    private void refreshVisibility()
    {
        final MasterDetailsContentNodePart node = (MasterDetailsContentNodePart) getPart();
        final MasterDetailsContentNodePart nextProblemNode = this.service.findNextProblem( node, this.severity );
        
        setVisible( nextProblemNode != null );
    }
    
    private PropertyEditorPart findFirstProblem( final List<SectionPart> sections )
    {
        for( SectionPart section : sections )
        {
            final PropertyEditorPart res = findFirstProblem( section );
            
            if( res != null )
            {
                return res;
            }
        }
        
        return null;
    }

    private PropertyEditorPart findFirstProblem( final SapphirePart part )
    {
        if( part != null )
        {
            if( part instanceof PropertyEditorPart )
            {
                if( part.validation().severity() == this.severity )
                {
                    return (PropertyEditorPart) part;
                }
            }
            else if( part instanceof FormPart )
            {
                for( SapphirePart p : ( (FormPart) part ).children().visible() )
                {
                    final PropertyEditorPart result = findFirstProblem( p );
                    
                    if( result != null )
                    {
                        return result;
                    }
                }
            }
            else if( part instanceof PageBookPart )
            {
                return findFirstProblem( ( (PageBookPart) part ).getCurrentPage() );
            }
        }
        
        return null;
    }

    @Override
    protected Object run( final Presentation context )
    {
        final MasterDetailsContentNodePart node = (MasterDetailsContentNodePart) getPart();
        final MasterDetailsContentNodePart nextProblemNode = this.service.findNextProblem( node, this.severity );
        
        if( nextProblemNode != null )
        {
            nextProblemNode.select();
            
            final PropertyEditorPart firstProblemPropertyEditor = findFirstProblem( nextProblemNode.getSections() );
            
            if( firstProblemPropertyEditor != null )
            {
                firstProblemPropertyEditor.setFocus();
            }
        }
        
        return null;
    }
   
}
