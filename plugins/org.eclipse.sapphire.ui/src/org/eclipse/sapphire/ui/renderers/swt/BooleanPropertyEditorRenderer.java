/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.renderers.swt;

import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glspacing;

import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.ui.PropertyEditorPart;
import org.eclipse.sapphire.ui.SapphirePart.LabelChangedEvent;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.def.PropertyEditorDef;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class BooleanPropertyEditorRenderer extends ValuePropertyEditorRenderer
{
    private Button checkbox;
    
    public BooleanPropertyEditorRenderer( final SapphireRenderingContext context,
                                          final PropertyEditorPart part )
    {
        super( context, part );
    }

    @Override
    protected void createContents( final Composite parent )
    {
        final PropertyEditorPart part = getPart();
        final ValueProperty property = (ValueProperty) part.getProperty();
        
        final CheckboxLayout checkboxLayout;
        final String checkboxLayoutHint = part.definition().getHint( PropertyEditorDef.HINT_CHECKBOX_LAYOUT );
        
        if( checkboxLayoutHint == null )
        {
            checkboxLayout = CheckboxLayout.TRAILING_LABEL;
        }
        else if( checkboxLayoutHint.equalsIgnoreCase( PropertyEditorDef.HINT_VALUE_CHECKBOX_LAYOUT_LEADING_LABEL ) )
        {
            checkboxLayout = CheckboxLayout.LEADING_LABEL;
        }
        else if( checkboxLayoutHint.equalsIgnoreCase( PropertyEditorDef.HINT_VALUE_CHECKBOX_LAYOUT_TRAILING_LABEL ) )
        {
            checkboxLayout = CheckboxLayout.TRAILING_LABEL;
        }
        else if( checkboxLayoutHint.equalsIgnoreCase( PropertyEditorDef.HINT_VALUE_CHECKBOX_LAYOUT_TRAILING_LABEL_INDENTED ) )
        {
            checkboxLayout = CheckboxLayout.TRAILING_LABEL_INDENTED;
        }
        else
        {
            checkboxLayout = CheckboxLayout.TRAILING_LABEL;
        }
        
        final Composite composite = createMainComposite
        (
            parent,
            new CreateMainCompositeDelegate( part )
            {
                @Override
                public boolean getShowLabel()
                {
                    if( checkboxLayout == CheckboxLayout.TRAILING_LABEL || checkboxLayout == CheckboxLayout.TRAILING_LABEL_INDENTED )
                    {
                        return false;
                    }
                    else
                    {
                        return super.getShowLabel();
                    }
                }
                
                @Override
                public boolean getSpanBothColumns()
                {
                    if( checkboxLayout == CheckboxLayout.TRAILING_LABEL )
                    {
                        return true;
                    }
                    else
                    {
                        return super.getSpanBothColumns();
                    }
                }
            }
        );
        
        final boolean isDeprecated = property.hasAnnotation( Deprecated.class );
        
        composite.setLayout( glspacing( glayout( ( isDeprecated ? 3 : 2 ), 0, 0 ), 2 ) );

        final PropertyEditorAssistDecorator decorator = createDecorator( composite ); 
        decorator.control().setLayoutData( gdvalign( gd(), SWT.CENTER ) );
        
        this.checkbox = new Button( composite, SWT.CHECK );
        this.checkbox.setLayoutData( gd() );
        this.context.adapt( this.checkbox );
        
        if( checkboxLayout != CheckboxLayout.LEADING_LABEL )
        {
            final Runnable updateLabelOp = new Runnable()
            {
                public void run()
                {
                    BooleanPropertyEditorRenderer.this.checkbox.setText( part.getLabel( CapitalizationType.FIRST_WORD_ONLY, true ) );
                }
            };
            
            final org.eclipse.sapphire.Listener listener = new org.eclipse.sapphire.Listener()
            {
                @Override
                public void handle( final org.eclipse.sapphire.Event event )
                {
                    if( event instanceof LabelChangedEvent )
                    {
                        updateLabelOp.run();
                        BooleanPropertyEditorRenderer.this.context.layout();
                    }
                }
            };
            
            getPart().attach( listener );
            updateLabelOp.run();
            
            this.checkbox.addDisposeListener
            (
                new DisposeListener()
                {
                    public void widgetDisposed( final DisposeEvent event )
                    {
                        getPart().detach( listener );
                    }
                }
            );
        }
        
        if( isDeprecated )
        {
            final Control deprecationMarker = createDeprecationMarker( composite );
            deprecationMarker.setLayoutData( gdhindent( gdhfill(), 3 ) );
        }
        
        this.checkbox.getAccessible().addAccessibleListener
        (
            new AccessibleAdapter()
            {
                @Override
                public void getName( final AccessibleEvent event )
                {
                    event.result = property.getLabel( true, CapitalizationType.NO_CAPS, true );
                }
            }
        );
        
        this.checkbox.addSelectionListener
        (
            new SelectionAdapter()
            {
                public void widgetSelected( final SelectionEvent event ) 
                {
                    final boolean value = BooleanPropertyEditorRenderer.this.checkbox.getSelection();
                    setPropertyValue( String.valueOf( value ) );
                }
            }
        );
        
        decorator.addEditorControl( composite );
        
        addControl( this.checkbox );
    }
    
    @Override
    protected void handlePropertyChangedEvent()
    {
        super.handlePropertyChangedEvent();
        
        if( ! this.checkbox.isDisposed() )
        {
            final Value<Boolean> value = getPropertyValue();
            
            if( value != null )
            {
                final boolean existingValue = this.checkbox.getSelection();
                final boolean newSimpleValue;
                
                if( value.getText( false ) != null && value.getContent( false ) == null )
                {
                    newSimpleValue = false;
                }
                else
                {
                    final Boolean newValue = value.getContent( true );
                    newSimpleValue = ( newValue != null ? newValue.booleanValue() : false );
                }
                
                if( newSimpleValue != existingValue )
                {
                    this.checkbox.setSelection( newSimpleValue );
                }
            }
            else
            {
                this.checkbox.setSelection( false );
            }
        }
    }
    
    @Override
    protected void handleFocusReceivedEvent()
    {
        this.checkbox.setFocus();
    }

    public static final class Factory extends PropertyEditorRendererFactory
    {
        @Override
        public boolean isApplicableTo( final PropertyEditorPart propertyEditorDefinition )
        {
            final ModelProperty property = propertyEditorDefinition.getProperty();
            return ( property instanceof ValueProperty && property.isOfType( Boolean.class ) );
        }
        
        @Override
        public PropertyEditorRenderer create( final SapphireRenderingContext context,
                                              final PropertyEditorPart part )
        {
            return new BooleanPropertyEditorRenderer( context, part );
        }
    }
    
    private enum CheckboxLayout
    {
        LEADING_LABEL,
        TRAILING_LABEL,
        TRAILING_LABEL_INDENTED
    }

}
