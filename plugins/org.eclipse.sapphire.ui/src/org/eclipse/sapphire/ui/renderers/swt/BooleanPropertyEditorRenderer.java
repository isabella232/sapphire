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

import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glspacing;

import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.ui.SapphirePropertyEditor;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class BooleanPropertyEditorRenderer

    extends ValuePropertyEditorRenderer
    
{
    private Button checkbox;
    
    public BooleanPropertyEditorRenderer( final SapphireRenderingContext context,
                                          final SapphirePropertyEditor part )
    {
        super( context, part );
    }

    @Override
    protected void createContents( final Composite parent )
    {
        final SapphirePropertyEditor part = getPart();
        final ValueProperty property = (ValueProperty) part.getProperty();
        
        String label = property.getLabel( true, CapitalizationType.FIRST_WORD_ONLY, true );
        
        if( property.hasAnnotation( Deprecated.class ) )
        {
            label = label + " " + Resources.deprecatedLabelText;
        }
        
        setSpanBothColumns( true );
        
        final Composite composite = createMainComposite( parent );
        composite.setLayout( glspacing( glayout( 2, 0, 0 ), 2 ) );

        final PropertyEditorAssistDecorator decorator = createDecorator( composite ); 
        decorator.getControl().setLayoutData( gdvalign( gd(), SWT.CENTER ) );
        
        this.checkbox = new Button( composite, SWT.CHECK );
        this.checkbox.setLayoutData( gd() );
        this.checkbox.setText( label );
        this.context.adapt( this.checkbox );
        
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
    
    @Override
    protected void handleFocusReceivedEvent()
    {
        this.checkbox.setFocus();
    }

    public static final class Factory
    
        extends PropertyEditorRendererFactory
        
    {
        @Override
        public boolean isApplicableTo( final SapphirePropertyEditor propertyEditorDefinition )
        {
            final ModelProperty property = propertyEditorDefinition.getProperty();
            return ( property instanceof ValueProperty && property.isOfType( Boolean.class ) );
        }
        
        @Override
        public PropertyEditorRenderer create( final SapphireRenderingContext context,
                                              final SapphirePropertyEditor part )
        {
            return new BooleanPropertyEditorRenderer( context, part );
        }
    }

    private static final class Resources
    
        extends NLS
    
    {
        public static String deprecatedLabelText;
        
        static
        {
            initializeMessages( BooleanPropertyEditorRenderer.class.getName(), Resources.class );
        }
    }
    
}
