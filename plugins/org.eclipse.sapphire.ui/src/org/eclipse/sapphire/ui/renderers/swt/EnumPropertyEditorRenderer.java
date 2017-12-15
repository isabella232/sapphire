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

package org.eclipse.sapphire.ui.renderers.swt;

import static org.eclipse.sapphire.ui.SapphirePropertyEditor.DATA_ASSIST_DECORATOR;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.DATA_BINDING;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_PREFER_COMBO;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_PREFER_RADIO_BUTTONS;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_PREFER_VERTICAL_RADIO_BUTTONS;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_SHOW_LABEL;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.RELATED_CONTROLS;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdvindent;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdwhint;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glspacing;

import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.ui.SapphirePropertyEditor;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.def.ISapphirePartDef;
import org.eclipse.sapphire.ui.internal.binding.ComboBinding;
import org.eclipse.sapphire.ui.internal.binding.RadioButtonsGroup;
import org.eclipse.sapphire.ui.internal.binding.RadioButtonsGroupBinding;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class EnumPropertyEditorRenderer

    extends ValuePropertyEditorRenderer
    
{
    private Control control;

    public EnumPropertyEditorRenderer( final SapphireRenderingContext context,
                                       final SapphirePropertyEditor part )
    {
        super( context, part );
    }

    @Override
    protected void createContents( final Composite parent )
    {
        final SapphirePropertyEditor part = getPart();
        final ValueProperty property = (ValueProperty) part.getProperty();
        
        boolean showLabel = part.getRenderingHint( HINT_SHOW_LABEL, true );
        final int baseIndent = part.getLeftMarginHint();
        final boolean preferVerticalRadioButtonBinding = part.getRenderingHint( HINT_PREFER_VERTICAL_RADIO_BUTTONS, false );
        final boolean preferRadioButtonBinding = part.getRenderingHint( HINT_PREFER_RADIO_BUTTONS, false );
        final boolean preferComboBinding = part.getRenderingHint( HINT_PREFER_COMBO, false );
        
        final int hspan = ( showLabel && ! preferVerticalRadioButtonBinding ) ? 1 : 2;
        
        final Enum<?>[] enumValues = (Enum<?>[]) property.getTypeClass().getEnumConstants();
        
        Control labelControl = null;
        PropertyEditorAssistDecorator decorator = null;
        
        if( showLabel )
        {
            final String labelText = property.getLabel( false, CapitalizationType.FIRST_WORD_ONLY, true );

            if( preferVerticalRadioButtonBinding )
            {
                final Composite composite = new Composite( parent, SWT.NONE );
                composite.setLayoutData( gdhspan( gdhfill(), hspan ) );
                composite.setLayout( glspacing( glayout( 2, 0, 0 ), 2 ) );
                this.context.adapt( composite );
                
                decorator = createDecorator( composite );
                decorator.getControl().setLayoutData( gdvalign( gd(), SWT.CENTER ) );
                
                final Label label = new Label( composite, SWT.WRAP );
                label.setLayoutData( gd() );
                label.setText( labelText );
                
                labelControl = label;

                decorator.addEditorControl( composite );
                decorator.addEditorControl( label );
            }
            else
            {
                final Label label = new Label( parent, SWT.NONE );
                label.setLayoutData( gdhindent( gd(), baseIndent + 9 ) );
                label.setText( labelText + ":" );
                this.context.adapt( label );
                
                labelControl = label;
            }
        }
        
        if( preferVerticalRadioButtonBinding )
        {
            Composite p = parent;
            
            if( ! showLabel )
            {
                final Composite composite = new Composite( p, SWT.NULL );
                composite.setLayoutData( gdhspan( gdhfill(), 2 ) );
                composite.setLayout( glspacing( glayout( 2, 0, 0 ), 2 ) );
                this.context.adapt( composite );
                
                decorator = createDecorator( composite );
                decorator.getControl().setLayoutData( gdvindent( gdvalign( gd(), SWT.TOP ), 4 ) );
                decorator.addEditorControl( composite );
                
                p = composite;
            }
            
            final RadioButtonsGroup buttonsGroup = new RadioButtonsGroup( this.context, p, true );
            
            if( showLabel )
            {
                buttonsGroup.setLayoutData( gdhindent( gdhspan( gdhfill(), hspan ), baseIndent + 20 ) );
            }
            else
            {
                buttonsGroup.setLayoutData( gdhfill() );
            }
            
            this.context.adapt( buttonsGroup );
            this.control = buttonsGroup;
        }
        else
        {
            final Composite composite = new Composite( parent, SWT.NULL );
            composite.setLayout( glspacing( glayout( 2, 0, 0 ), 2 ) );
            this.context.adapt( composite );
            
            decorator = createDecorator( composite );
            decorator.addEditorControl( composite );
            
            if( preferRadioButtonBinding || ( enumValues.length <= 3 && ! preferComboBinding ) )
            {
                composite.setLayoutData( gdhspan( gd(), hspan ) );

                decorator.getControl().setLayoutData( gdvalign( gd(), SWT.CENTER ) );

                final RadioButtonsGroup buttonsGroup = new RadioButtonsGroup( this.context, composite, false );
                buttonsGroup.setLayoutData( gdhfill() );
                this.context.adapt( buttonsGroup );
                
                this.control = buttonsGroup;
            }
            else
            {
                final int whint = part.getRenderingHint( ISapphirePartDef.HINT_WIDTH, SWT.DEFAULT );
                composite.setLayoutData( gdhspan( whint == SWT.DEFAULT ? gdhfill() : gdwhint( gd(), whint ), hspan ) );
                
                decorator.getControl().setLayoutData( gdvalign( gd(), SWT.TOP ) );

                final Combo c = new Combo( composite, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY );
                c.setLayoutData( gdhfill() );
                c.setVisibleItemCount( 10 );
                this.context.adapt( c );
                
                this.control = c;
            }
        }
    
        this.control.setData( DATA_ASSIST_DECORATOR, decorator );
        
        if( labelControl != null )
        {
            this.control.setData( RELATED_CONTROLS, labelControl );
        }
        
        if( this.control instanceof RadioButtonsGroup )
        {
            this.binding = new RadioButtonsGroupBinding( getPart(), this.context, (RadioButtonsGroup) this.control );            
        }
        else
        {
            this.binding = new ComboBinding( getPart(), this.context, (Combo) this.control );
        }
    
        this.control.setData( DATA_BINDING, this.binding );
        decorator.addEditorControl( this.control );
    
        addControl( this.control );
    }
    
    @Override
    protected void handleFocusReceivedEvent()
    {
        this.control.setFocus();
    }

    public static final class Factory
    
        extends PropertyEditorRendererFactory
        
    {
        @Override
        public boolean isApplicableTo( final SapphirePropertyEditor propertyEditorDefinition )
        {
            final ModelProperty property = propertyEditorDefinition.getProperty();
            return ( property instanceof ValueProperty && property.isOfType( Enum.class ) );
        }
        
        @Override
        public PropertyEditorRenderer create( final SapphireRenderingContext context,
                                              final SapphirePropertyEditor part )
        {
            return new EnumPropertyEditorRenderer( context, part );
        }
    }

}
