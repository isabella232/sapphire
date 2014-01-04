/******************************************************************************
 * Copyright (c) 2014 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [329102] excess scroll space in editor sections
 *    Gregory Amerson - [418602] Radio buttons property editor should show images
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt;

import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdwhint;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.forms.swt.SwtUtil.changeRadioButtonSelection;
import static org.eclipse.sapphire.ui.forms.swt.SwtUtil.reflowOnResize;
import static org.eclipse.sapphire.ui.forms.swt.SwtUtil.setEnabledOnChildren;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public class RadioButtonsGroup extends Composite
{
    private final boolean vertical;
    private final List<Button> buttons;
    private final List<Button> buttonsReadOnly;
    private final Map<Button,Control> auxTextControls;
    private SelectionListener selectionListener;
    private Button selection;
    private List<SelectionListener> listeners;
    
    public RadioButtonsGroup( final Composite composite, final boolean vertical )
    {
        super( composite, SWT.NONE );

        this.vertical = vertical;
        this.buttons = new ArrayList<Button>();
        this.buttonsReadOnly = Collections.unmodifiableList( this.buttons );
        this.auxTextControls = new HashMap<Button,Control>();
        this.selection = null;
        this.listeners = new CopyOnWriteArrayList<SelectionListener>();
        
        if( this.vertical )
        {
            setLayout( glayout( 1, 0, 0 ) );
        }
        else
        {
            final RowLayout layout = new RowLayout();
            layout.wrap = false;
            layout.pack = true;
            layout.type = ( vertical ? SWT.VERTICAL : SWT.HORIZONTAL );
            layout.marginTop = 0;
            layout.marginBottom = 0;
            layout.marginLeft = 0;
            layout.marginRight = 0;
            layout.spacing = 5;
            
            setLayout( layout );
        }
        
        this.selectionListener = new SelectionAdapter()
        {
            @Override
            public void widgetSelected( final SelectionEvent event )
            {
                handleWidgetSelectedEvent( event );
            }
        };
    }
    
    @Override
    public void setEnabled( boolean enabled )
    {
        super.setEnabled( enabled );
        setEnabledOnChildren( this, enabled );
    }
    
    public List<Button> getRadioButtons()
    {
        return this.buttonsReadOnly;
    }
    
    public Button addRadioButton( final String text )
    {
        return addRadioButton( text, null, null );
    }
    
    public Button addRadioButton( final String text,
                                  final String auxText,
                                  final Image image )
    {
        final Button button = new Button( this, SWT.RADIO );
        button.setLayoutData( this.vertical ? gd() : null );
        button.setText( text );
        this.buttons.add( button );
        
        button.addDisposeListener
        (
            new DisposeListener()
            {
                public void widgetDisposed( final DisposeEvent event )
                {
                    RadioButtonsGroup.this.buttons.remove( button );
                    
                    final Control auxTextControl = RadioButtonsGroup.this.auxTextControls.remove( button );
                    
                    if( auxTextControl != null )
                    {
                        auxTextControl.dispose();
                    }
                }
            }
        );
        
        button.addSelectionListener( this.selectionListener );
        
        if( this.vertical && auxText != null )
        {
            final Composite auxTextComposite = new Composite( this, SWT.NONE );
            auxTextComposite.setLayoutData( gdhfill() );
            auxTextComposite.setLayout( glayout( 1, 16, 0, 0, 0 ) );
            
            final Label auxTextLabel = new Label( auxTextComposite, SWT.WRAP );
            auxTextLabel.setLayoutData( gdwhint( gdhfill(), 100 ) );
            auxTextLabel.setForeground( getDisplay().getSystemColor( SWT.COLOR_DARK_GRAY ) );
            auxTextLabel.setText( auxText );
            
            reflowOnResize( auxTextLabel );

            this.auxTextControls.put( button, auxTextComposite );
        }

        if( image != null )
        {
            button.setImage( image );
        }

        return button;
    }
    
    public void removeRadioButton( final Button button )
    {
        button.dispose();
    }
    
    public Button getSelection()
    {
        return this.selection;
    }
    
    public void setSelection( final Button button )
    {
        this.selection = button;
        
        changeRadioButtonSelection( this.buttons, button );
    }

    public int getSelectionIndex()
    {
        final Button selection = getSelection();
        return ( selection == null ? -1 : this.buttons.indexOf( selection ) );
    }
    
    public void setSelectionIndex( final int selection )
    {
        setSelection( this.buttons.get( selection ) );
    }
    
    public void addSelectionListener( final SelectionListener listener ) 
    {
        this.listeners.add( listener );
    }
    
    public void removeSelectionListener( final SelectionListener listener )
    {
        this.listeners.remove( listener );
    }
    
    private void handleWidgetSelectedEvent( final SelectionEvent event )
    {
        final Button b = (Button) event.getSource();
        
        if( b.getSelection() == true )
        {
            this.selection = b;
            
            for( SelectionListener listener : this.listeners )
            {
                try
                {
                    listener.widgetSelected( event );
                }
                catch( Exception e )
                {
                    Sapphire.service( LoggingService.class ).log( e );
                }
            }
        }
    }

}
