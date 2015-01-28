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

import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdvindent;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdwhint;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glspacing;
import static org.eclipse.sapphire.ui.forms.swt.SwtUtil.changeRadioButtonSelection;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.EditFailedException;
import org.eclipse.sapphire.modeling.annotations.NamedValues;
import org.eclipse.sapphire.modeling.annotations.NamedValues.NamedValue;
import org.eclipse.sapphire.modeling.util.MiscUtil;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.ui.forms.swt.AbstractBinding;
import org.eclipse.sapphire.ui.forms.swt.PropertyEditorPresentation;
import org.eclipse.sapphire.ui.forms.swt.PropertyEditorPresentationFactory;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.sapphire.ui.forms.swt.ValuePropertyEditorPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class NamedValuesPropertyEditorPresentation extends ValuePropertyEditorPresentation
{
    private Composite rootComposite;
    private boolean updating;
    private String defaultArbitraryValue;
    private Label overallLabelControl;
    private Button arbitraryValueRadioButton;
    private Text arbitraryValueTextField;
    private NamedValueLocal[] namedValues;
    private Button[] namedValuesRadioButtons;
    private List<Button> radioButtonGroup;
    
    public NamedValuesPropertyEditorPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
    {
        super( part, parent, composite );
    }
    
    protected void createContents( final Composite parent )
    {
        final PropertyEditorPart part = part();
        final ValueProperty property = (ValueProperty) part.property().definition();
        
        this.rootComposite = new Composite( parent, SWT.NONE )
        {
            @Override
            public void setEnabled( final boolean enabled )
            {
                super.setEnabled( enabled );
                
                NamedValuesPropertyEditorPresentation.this.overallLabelControl.setEnabled( enabled );
                NamedValuesPropertyEditorPresentation.this.arbitraryValueRadioButton.setEnabled( enabled );
                NamedValuesPropertyEditorPresentation.this.arbitraryValueTextField.setEnabled( enabled && ( NamedValuesPropertyEditorPresentation.this.arbitraryValueRadioButton.getSelection() == true ) );
                
                for( Button b : NamedValuesPropertyEditorPresentation.this.namedValuesRadioButtons )
                {
                    b.setEnabled( enabled );
                }
            }
        };
        
        this.rootComposite.setLayout( glayout( 1, 0, 0 ) );
        
        final int baseIndent = part.getMarginLeft();
        this.rootComposite.setLayoutData( gdvindent( gdhindent( gdhspan( gdhfill(), 2 ), baseIndent ), 5 ) );
        
        final NamedValues namedValuesAnnotation = property.getAnnotation( NamedValues.class );
        final NamedValue[] namedValueAnnotations = namedValuesAnnotation.namedValues();
        
        this.namedValues = new NamedValueLocal[ namedValueAnnotations.length ];
        
        for( int i = 0, n = namedValueAnnotations.length; i < n; i++ )
        {
            final NamedValue x = namedValueAnnotations[ i ];
            
            final String namedValueLabel 
                = property.getLocalizationService().text( x.label(), CapitalizationType.FIRST_WORD_ONLY, true );
            
            this.namedValues[ i ] = new NamedValueLocal( x.value(), namedValueLabel );
        }
        
        this.updating = false;
        
        this.defaultArbitraryValue = namedValuesAnnotation.defaultArbitraryValue();
        this.defaultArbitraryValue = property.encodeKeywords( this.defaultArbitraryValue );
        
        final Composite composite = new Composite( this.rootComposite, SWT.NONE );
        composite.setLayoutData( gdhfill() );
        composite.setLayout( glspacing( glayout( 2, 0, 0 ), 2 ) );
        
        final PropertyEditorAssistDecorator decorator = new PropertyEditorAssistDecorator( part, composite );
        
        decorator.control().setLayoutData( gdvalign( gd(), SWT.CENTER ) );
        decorator.addEditorControl( this.rootComposite );
        decorator.addEditorControl( composite );
        
        this.overallLabelControl = new Label( composite, SWT.WRAP );
        this.overallLabelControl.setLayoutData( gd() );
        this.overallLabelControl.setText( property.getLabel( true, CapitalizationType.FIRST_WORD_ONLY, true ) );
        decorator.addEditorControl( this.overallLabelControl );
        
        final SelectionListener selectionListener = new SelectionAdapter() 
        {
            @Override
            public void widgetSelected( final SelectionEvent event ) 
            {
                handleRadioButtonSelectedEvent( event );
            }
        };
        
        final Composite radioButtonsComposite = new Composite( this.rootComposite, SWT.NONE );
        radioButtonsComposite.setLayoutData( gdhindent( gdhfill(), 20 ) );
        radioButtonsComposite.setLayout( glayout( 2, 0, 0, 0, 0 ) );
        decorator.addEditorControl( radioButtonsComposite );
        
        this.radioButtonGroup = new ArrayList<Button>();
        
        final String arbitraryValueLabel 
            = property.getLocalizationService().text( namedValuesAnnotation.arbitraryValueLabel(), CapitalizationType.FIRST_WORD_ONLY, true );
    
        this.arbitraryValueRadioButton = createRadioButton( radioButtonsComposite, arbitraryValueLabel, true );
        this.arbitraryValueRadioButton.setLayoutData( gd() );
        this.arbitraryValueRadioButton.addSelectionListener( selectionListener );
        this.radioButtonGroup.add( this.arbitraryValueRadioButton );
        decorator.addEditorControl( this.arbitraryValueRadioButton );
        
        this.arbitraryValueTextField = new Text( radioButtonsComposite, SWT.BORDER );
        this.arbitraryValueTextField.setLayoutData( gdwhint( gd(), 150 ) );
        decorator.addEditorControl( this.arbitraryValueTextField );
        
        this.arbitraryValueTextField.addModifyListener
        (
            new ModifyListener()
            {
                public void modifyText( final ModifyEvent event )
                {
                    handleArbitraryValueTextFieldChangedEvent( event );
                }
            }
        );
        
        final TextOverlayPainter.Controller textOverlayPainterController = new TextOverlayPainter.Controller()
        {
            @Override
            public String overlay()
            {
                return ( (Value<?>) property() ).getDefaultText();
            }
        };
    
        TextOverlayPainter.install( this.arbitraryValueTextField, textOverlayPainterController );
        
        final StringBuilder arbitraryValueAccessibleName = new StringBuilder();
        
        arbitraryValueAccessibleName.append( property().definition().getLabel( true, CapitalizationType.NO_CAPS, false ) );
        arbitraryValueAccessibleName.append( ' ' );
        arbitraryValueAccessibleName.append( arbitraryValueLabel );
        
        attachAccessibleName( this.arbitraryValueTextField, arbitraryValueAccessibleName.toString() );
        
        this.namedValuesRadioButtons = new Button[ this.namedValues.length ];
        
        for( int i = 0; i < this.namedValues.length; i++ )
        {
            final Button rb = createRadioButton( radioButtonsComposite, this.namedValues[ i ].valueName, false );
            rb.addSelectionListener( selectionListener );
            decorator.addEditorControl( rb );
            this.namedValuesRadioButtons[ i ] = rb;
            this.radioButtonGroup.add( rb );
        }
        
        this.rootComposite.addFocusListener
        (
            new FocusAdapter()
            {
                @Override
                public void focusGained( final FocusEvent event )
                {
                    for( final Button radioButton : NamedValuesPropertyEditorPresentation.this.radioButtonGroup )
                    {
                        if( radioButton.getSelection() == true )
                        {
                            radioButton.setFocus();
                            return;
                        }
                    }
                }
            }
        );

        this.rootComposite.setData( "peditor", this );
        
        this.binding = new NamedValuesBinding();
        
        this.rootComposite.setData( PropertyEditorPart.DATA_BINDING, this.binding );
        
        addControl( this.rootComposite );
    }
    
    private Button createRadioButton( final Composite parent, final String label, final boolean arbitrary ) 
    {
        final Button b = new Button( parent, SWT.RADIO );
        b.setLayoutData( gdhspan( gd(), 2 ) );
        b.setText( label + ( arbitrary ? ":" : "" ) );
        
        final StringBuilder buf = new StringBuilder();
        
        buf.append( property().definition().getLabel( true, CapitalizationType.NO_CAPS, false ) );
        buf.append( ' ' );
        buf.append( label );
        
        attachAccessibleName( b, buf.toString() );
        
        return b;
    }
    
    private void handleRadioButtonSelectedEvent( final SelectionEvent event )
    {
        final Button b = (Button) event.getSource();
        
        if( b == this.arbitraryValueRadioButton )
        {
            setPropertyValue2( this.defaultArbitraryValue );
        }
        else
        {
            for( int i = 0; i < this.namedValuesRadioButtons.length; i++ )
            {
                if( b == this.namedValuesRadioButtons[ i ] )
                {
                    setPropertyValue2( this.namedValues[ i ].value );
                    break;
                }
            }
        }
    }
    
    private void handleArbitraryValueTextFieldChangedEvent( final ModifyEvent event )
    {
        if( this.updating )
        {
            return;
        }
        
        setPropertyValue2( this.arbitraryValueTextField.getText() );
    }
    
    private void update()
    {
        if( this.updating )
        {
            return;
        }
        
        this.updating = true;
        
        try
        {
            final Value<?> val = property();
            final String valueWithDefault = val.text( true );
            NamedValueLocal namedValue = null;

            if( valueWithDefault != null )
            {
                for( int i = 0; i < this.namedValues.length; i++ )
                {
                    final NamedValueLocal nm = this.namedValues[ i ];
                    
                    if( valueWithDefault.equals( nm.value ) )
                    {
                        namedValue = nm;
                        break;
                    }
                }
            }
            
            if( namedValue != null )
            {
                Button buttonToSelect = null;
                
                for( int i = 0; i < this.namedValues.length; i++ )
                {
                    if( namedValue == this.namedValues[ i ] )
                    {
                        buttonToSelect = this.namedValuesRadioButtons[ i ];
                        break;
                    }
                }
                
                if( buttonToSelect == null )
                {
                    throw new IllegalStateException();
                }

                changeRadioButtonSelection( this.radioButtonGroup, buttonToSelect );

                this.arbitraryValueTextField.setEnabled( false );
                this.arbitraryValueTextField.setText( MiscUtil.EMPTY_STRING );
            }
            else
            {
                changeRadioButtonSelection( this.radioButtonGroup, this.arbitraryValueRadioButton );
                
                this.arbitraryValueTextField.setEnabled( true );
                
                final String existingValue = this.arbitraryValueTextField.getText();
                String valueWithoutDefault = val.text( false );
                valueWithoutDefault = ( valueWithoutDefault == null ? "" : valueWithoutDefault );

                if( ! existingValue.equals( valueWithoutDefault ) )
                {
                    this.arbitraryValueTextField.setText( valueWithoutDefault );
                }
            }
        }
        finally
        {
            this.updating = false;
        }
    }
    
    private void setPropertyValue2( final String value )
    {
        try
        {
            property().write( value, true );
        }
        catch( Exception e )
        {
            final EditFailedException editFailedException = EditFailedException.findAsCause( e );
            
            if( editFailedException != null )
            {
                update();
            }
            else
            {
                Sapphire.service( LoggingService.class ).log( e );
            }
        }
    }
    
    @Override
    protected void handleFocusReceivedEvent()
    {
        this.namedValuesRadioButtons[ 0 ].setFocus();
    }

    public static final class Factory extends PropertyEditorPresentationFactory
    {
        @Override
        public PropertyEditorPresentation create( final PropertyEditorPart part, final SwtPresentation parent, final Composite composite )
        {
            final PropertyDef property = part.property().definition();
            
            if( property instanceof ValueProperty && property.hasAnnotation( NamedValues.class ) )
            {
                return new NamedValuesPropertyEditorPresentation( part, parent, composite );
            }
            
            return null;
        }
    }
    
    private static class NamedValueLocal
    {
        public String value;
        public final String valueName;
        
        public NamedValueLocal( final String value,
                                final String valueName )
        {
            this.value = value;
            this.valueName = valueName;
        }
    }
    
    private final class NamedValuesBinding extends AbstractBinding
    {
        public NamedValuesBinding()
        {
            super( NamedValuesPropertyEditorPresentation.this, NamedValuesPropertyEditorPresentation.this.rootComposite );
        }
        
        @Override
        protected void doUpdateModel()
        {
        }
        
        @Override
        protected void doUpdateTarget()
        {
            update();
        }
    }
    
}
