/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [329114] rewrite context help binding feature
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.ui.SapphireHelpContext;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.SharedScrolledComposite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public abstract class FormComponentPresentation extends SwtPresentation
{
    private Composite composite;
    private DisposeListener compositeDisposeListener;
    private List<Control> controls;
    
    public FormComponentPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
    {
        super( part, parent, composite.getShell() );
        
        this.composite = composite;

        this.compositeDisposeListener = new DisposeListener()
        {
            @Override
            public void widgetDisposed( final DisposeEvent event )
            {
                dispose();
            }
        };
        
        this.composite.addDisposeListener( this.compositeDisposeListener );
    }

    @Override
    public FormComponentPart part()
    {
        return (FormComponentPart) super.part();
    }

    public final Composite composite()
    {
        return this.composite;
    }
    
    protected boolean isSingleLine()
    {
        return false;
    }
    
    protected static final void attachHelp( final Control control, final Property property )
    {
        final SapphireHelpContext context = new SapphireHelpContext( property.element(), property.definition() );
        if( context.getText() != null || ( context.getRelatedTopics() != null && context.getRelatedTopics().length > 0 ) )
        {
            control.addHelpListener( new HelpListener()
            {
                public void helpRequested( HelpEvent event )
                {
                    // determine a location in the upper right corner of the
                    // widget
                    Point point = HelpSystem.computePopUpLocation( event.widget.getDisplay() );
                    // display the help
                    PlatformUI.getWorkbench().getHelpSystem().displayContext( context, point.x, point.y );
                }
            } );
        }
    }   
    
    protected final void register( final Control control )
    {
        if( control == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( this.controls == null )
        {
            this.controls = new ArrayList<Control>( 2 );
        }
        
        this.controls.add( control );
    }
    
    // TODO: Make protected once binding concept is completely removed.
    
    public void layout()
    {
        Composite composite = composite();
        
        composite.getShell().layout( true, true );
        
        while( composite != null && ! ( composite instanceof ScrolledComposite ) )
        {
            composite = composite.getParent();
        }
        
        if( composite instanceof SharedScrolledComposite )
        {
            ( (SharedScrolledComposite) composite ).reflow( true );
        }
        else if( composite instanceof ScrolledComposite )
        {
            final ScrolledComposite scrolledComposite = (ScrolledComposite) composite;
            scrolledComposite.setMinSize( scrolledComposite.getContent().computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
            updatePageIncrement( scrolledComposite );
        }
    }
    
    private static void updatePageIncrement( final ScrolledComposite scomp )
    {
        final ScrollBar vbar = scomp.getVerticalBar();
        
        if( vbar != null )
        {
            vbar.setPageIncrement( scomp.getClientArea().height - 5 );
        }
        
        final ScrollBar hbar = scomp.getHorizontalBar();
        
        if( hbar != null )
        {
            hbar.setPageIncrement( scomp.getClientArea().width - 5 );
        }
    }
    
    public void refresh()
    {
        final SwtPresentation parent = parent();
        
        if( parent instanceof FormComponentPresentation )
        {
            ( (FormComponentPresentation) parent ).refresh();
        }
    }
    
    public void dispose()
    {
        super.dispose();

        if( this.composite != null )
        {
            if( ! this.composite.isDisposed() )
            {
                this.composite.removeDisposeListener( this.compositeDisposeListener );
            }
            
            this.composite = null;
            this.compositeDisposeListener = null;
        }
        
        if( this.controls != null )
        {
            for( final Control control : this.controls )
            {
                if( ! control.isDisposed() )
                {
                    control.setVisible( false );
                    control.dispose();
                }
            }
            
            this.controls = null;
        }
    }

}
