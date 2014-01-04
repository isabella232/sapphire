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

package org.eclipse.sapphire.ui.forms.swt.internal;

import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glspacing;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.ui.SapphirePart.LabelChangedEvent;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.PropertyEditorDef;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.ui.forms.swt.PropertyEditorPresentation;
import org.eclipse.sapphire.ui.forms.swt.PropertyEditorPresentationFactory;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.sapphire.ui.forms.swt.ValuePropertyEditorPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class CheckBoxPropertyEditorPresentation extends ValuePropertyEditorPresentation
{
    private Button checkbox;
    
    public CheckBoxPropertyEditorPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
    {
        super( part, parent, composite );
    }

    @Override
    protected void createContents( final Composite parent )
    {
        final PropertyEditorPart part = part();
        
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
        
        final boolean isDeprecated = property().definition().hasAnnotation( Deprecated.class );
        
        composite.setLayout( glspacing( glayout( ( isDeprecated ? 3 : 2 ), 0, 0 ), 2 ) );

        final PropertyEditorAssistDecorator decorator = createDecorator( composite ); 
        decorator.control().setLayoutData( gdvalign( gd(), SWT.CENTER ) );
        
        this.checkbox = new Button( composite, SWT.CHECK );
        this.checkbox.setLayoutData( gd() );
        
        if( checkboxLayout != CheckboxLayout.LEADING_LABEL )
        {
            final Runnable updateLabelOp = new Runnable()
            {
                public void run()
                {
                    CheckBoxPropertyEditorPresentation.this.checkbox.setText( part.label( CapitalizationType.FIRST_WORD_ONLY, true ) );
                }
            };
            
            attachPartListener
            (
                new FilteredListener<LabelChangedEvent>()
                {
                    @Override
                    protected void handleTypedEvent( final LabelChangedEvent event )
                    {
                        updateLabelOp.run();
                        layout();
                    }
                }
            );
            
            updateLabelOp.run();
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
                    event.result = property().definition().getLabel( true, CapitalizationType.NO_CAPS, true );
                }
            }
        );
        
        this.checkbox.addSelectionListener
        (
            new SelectionAdapter()
            {
                public void widgetSelected( final SelectionEvent event ) 
                {
                    final boolean value = CheckBoxPropertyEditorPresentation.this.checkbox.getSelection();
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
            final Value<?> value = property();
            
            if( value != null )
            {
                final boolean existingValue = this.checkbox.getSelection();
                final boolean newSimpleValue;
                
                if( value.text( false ) != null && value.content( false ) == null )
                {
                    newSimpleValue = false;
                }
                else
                {
                    final Boolean newValue = (Boolean) value.content( true );
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
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        this.checkbox = null;
    }

    public static final class Factory extends PropertyEditorPresentationFactory
    {
        @Override
        public PropertyEditorPresentation create( final PropertyEditorPart part, final SwtPresentation parent, final Composite composite )
        {
            final PropertyDef property = part.property().definition();
            
            if( property instanceof ValueProperty && property.isOfType( Boolean.class ) )
            {
                return new CheckBoxPropertyEditorPresentation( part, parent, composite );
            }
            
            return null;
        }
    }
    
    private enum CheckboxLayout
    {
        LEADING_LABEL,
        TRAILING_LABEL,
        TRAILING_LABEL_INDENTED
    }

}
