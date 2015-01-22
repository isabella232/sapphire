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

package org.eclipse.sapphire.ui.forms.swt.internal;

import static org.eclipse.sapphire.ui.forms.PropertyEditorPart.DATA_BINDING;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdvindent;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glspacing;

import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.def.Orientation;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.ui.forms.swt.PropertyEditorPresentation;
import org.eclipse.sapphire.ui.forms.swt.PropertyEditorPresentationFactory;
import org.eclipse.sapphire.ui.forms.swt.RadioButtonsGroup;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.sapphire.ui.forms.swt.ValuePropertyEditorPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class RadioButtonGroupPropertyEditorPresentation extends ValuePropertyEditorPresentation
{
    private final Orientation orientation;
    private RadioButtonsGroup control;

    public RadioButtonGroupPropertyEditorPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite, final Orientation orientation )
    {
        super( part, parent, composite );
        
        if( orientation == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.orientation = orientation;
    }

    @Override
    protected void createContents( final Composite parent )
    {
        PropertyEditorAssistDecorator decorator = null;
        
        if( this.orientation == Orientation.VERTICAL )
        {
            final Composite composite = createMainComposite( parent );
            composite.setLayout( glspacing( glayout( 2, 0, 0 ), 2, 5 ) );

            decorator = createDecorator( composite );
            decorator.addEditorControl( composite );
            decorator.control().setLayoutData( gdvindent( gdvalign( gd(), SWT.TOP ), 4 ) );
            
            this.control = new RadioButtonsGroup( composite, true );
            this.control.setLayoutData( gdhfill() );
        }
        else
        {
            final Composite composite = createMainComposite( parent );
            composite.setLayout( glspacing( glayout( 2, 0, 0 ), 2 ) );
            
            decorator = createDecorator( composite );
            decorator.addEditorControl( composite );
            
            decorator.control().setLayoutData( gdvalign( gd(), SWT.CENTER ) );

            this.control = new RadioButtonsGroup( composite, false );
            this.control.setLayoutData( gdhfill() );
        }
    
        this.binding = new RadioButtonGroupBinding( this, this.control );            
    
        this.control.setData( DATA_BINDING, this.binding );
        decorator.addEditorControl( this.control, true );
    
        addControl( this.control );
    }
    
    @Override
    protected void handleFocusReceivedEvent()
    {
        this.control.setFocus();
    }
    
    @Override
    public boolean isSingleLine()
    {
        return ( this.orientation == Orientation.HORIZONTAL );
    }

    public static final class Factory extends PropertyEditorPresentationFactory
    {
        @Override
        public PropertyEditorPresentation create( final PropertyEditorPart part, final SwtPresentation parent, final Composite composite )
        {
            final String style = part.definition().getStyle().content();
            
            if( style != null && style.startsWith( "Sapphire.PropertyEditor.RadioButtonGroup" ) )
            {
                final Property property = part.property();
                
                if( property instanceof Value && property.definition().isOfType( Enum.class ) )
                {
                    Orientation orientation = null;
                    
                    if( style.equals( "Sapphire.PropertyEditor.RadioButtonGroup" ) || style.equals( "Sapphire.PropertyEditor.RadioButtonGroup.Horizontal" ) )
                    {
                        orientation = Orientation.HORIZONTAL;
                    }
                    else if( style.equals( "Sapphire.PropertyEditor.RadioButtonGroup.Vertical" ) )
                    {
                        orientation = Orientation.VERTICAL;
                    }
                    
                    if( orientation != null )
                    {
                        return new RadioButtonGroupPropertyEditorPresentation( part, parent, composite, orientation );
                    }
                }
            }
            
            return null;
        }
    }

}
