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

package org.eclipse.sapphire.ui.forms.swt.internal;

import static org.eclipse.sapphire.modeling.util.MiscUtil.EMPTY_STRING;
import static org.eclipse.sapphire.modeling.util.MiscUtil.equal;
import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_ASSIST;
import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_BROWSE;
import static org.eclipse.sapphire.ui.SapphireActionSystem.createFilterByActionId;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdvfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glspacing;
import static org.eclipse.sapphire.ui.forms.swt.SwtUtil.runOnDisplayThread;

import java.util.List;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.Status.Severity;
import org.eclipse.sapphire.modeling.annotations.PossibleValues;
import org.eclipse.sapphire.services.PossibleValuesService;
import org.eclipse.sapphire.services.ValueNormalizationService;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.ui.forms.swt.PropertyEditorPresentation;
import org.eclipse.sapphire.ui.forms.swt.PropertyEditorPresentationFactory;
import org.eclipse.sapphire.ui.forms.swt.SapphireToolBarActionPresentation;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.sapphire.ui.forms.swt.ValuePropertyEditorPresentation;
import org.eclipse.sapphire.util.MutableReference;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class PopUpListFieldPropertyEditorPresentation extends ValuePropertyEditorPresentation
{
    private static final String DATA_DEFAULT_VALUE = "Sapphire.Default.Value";
    
    private final PopUpListFieldStyle style;
    private Combo combo;

    public PopUpListFieldPropertyEditorPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite, final PopUpListFieldStyle style )
    {
        super( part, parent, composite );
        
        if( style == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.style = style;
    }

    @Override
    protected void createContents( final Composite parent )
    {
        final PropertyEditorPart part = part();
        final Value<?> property = (Value<?>) part.property();
        
        final SapphireToolBarActionPresentation toolBarActionsPresentation = new SapphireToolBarActionPresentation( getActionPresentationManager() );
        toolBarActionsPresentation.addFilter( createFilterByActionId( ACTION_ASSIST ) );
        toolBarActionsPresentation.addFilter( createFilterByActionId( ACTION_BROWSE ) );
        
        final boolean isActionsToolBarNeeded = toolBarActionsPresentation.hasActions();
        
        final Composite composite = createMainComposite( parent );
        composite.setLayout( glspacing( glayout( ( isActionsToolBarNeeded ? 3 : 2 ), 0, 0 ), 2 ) );
        
        final PropertyEditorAssistDecorator decorator = createDecorator( composite );
        decorator.addEditorControl( composite );
        decorator.control().setLayoutData( gdvalign( gd(), SWT.TOP ) );

        final Combo combo = new Combo( composite, SWT.SINGLE | SWT.BORDER | ( this.style == PopUpListFieldStyle.STRICT ? SWT.READ_ONLY : SWT.NONE ) );
        combo.setLayoutData( gdhfill() );
        combo.setVisibleItemCount( 10 );

        decorator.addEditorControl( combo, true );
        addControl( combo );
        this.combo = combo;
        
        if( isActionsToolBarNeeded )
        {
            final ToolBar toolbar = new ToolBar( composite, SWT.FLAT | SWT.HORIZONTAL );
            toolbar.setLayoutData( gdhindent( gdvfill(), 3 ) );
            toolBarActionsPresentation.setToolBar( toolbar );
            toolBarActionsPresentation.render();
            addControl( toolbar );
            decorator.addEditorControl( toolbar );
        }
        
        final PossibleValuesService possibleValuesService = property.service( PossibleValuesService.class );
        final ValueNormalizationService valueNormalizationService = property.service( ValueNormalizationService.class );
        
        final MutableReference<List<PossibleValue>> possibleValuesRef = new MutableReference<List<PossibleValue>>();
        
        final Runnable updateComboSelectionOp = new Runnable()
        {
            public void run()
            {
                if( PopUpListFieldPropertyEditorPresentation.this.updatingModel )
                {
                    return;
                }
                
                final String text = valueNormalizationService.normalize( property.text() );
                
                try
                {
                    PopUpListFieldPropertyEditorPresentation.this.updatingEditor = true;

                    combo.setData( DATA_DEFAULT_VALUE, property.empty() );
                    
                    if( text == null )
                    {
                        combo.deselectAll();
                        combo.setText( EMPTY_STRING );
                    }
                    else
                    {
                        final List<PossibleValue> possibleValues = possibleValuesRef.get();
                        final int possibleValuesCount = possibleValues.size();
                        int possibleValueIndex = -1;
                        
                        for( int i = 0; i < possibleValuesCount && possibleValueIndex == -1; i++ )
                        {
                            if( equal( possibleValues.get( i ).value(), text ) )
                            {
                                possibleValueIndex = i;
                            }
                        }
    
                        if( PopUpListFieldPropertyEditorPresentation.this.style == PopUpListFieldStyle.STRICT )
                        {
                            if( possibleValueIndex == -1 )
                            {
                                if( possibleValues.size() == combo.getItemCount() )
                                {
                                    combo.add( text );
                                }
                                else
                                {
                                    final String existingNonConformingValue = combo.getItem( possibleValuesCount );
                                    
                                    if( ! existingNonConformingValue.equals( text ) )
                                    {
                                        combo.setItem( possibleValuesCount, text );
                                    }
                                }
                                
                                possibleValueIndex = possibleValuesCount;
                            }
                            else if( possibleValuesCount < combo.getItemCount() )
                            {
                                combo.remove( possibleValuesCount );
                            }
                        }
                        
                        if( possibleValueIndex != -1 )
                        {
                            if( combo.getSelectionIndex() != possibleValueIndex )
                            {
                                combo.setText( EMPTY_STRING );
                                combo.select( possibleValueIndex );
                            }
                        }
                        else
                        {
                            if( ! equal( valueNormalizationService.normalize( combo.getText() ), text ) )
                            {
                                combo.deselectAll();
                                combo.setText( text );
                            }
                        }
                    }
                }
                finally
                {
                    PopUpListFieldPropertyEditorPresentation.this.updatingEditor = false;
                }
            }
        };

        final Runnable updateComboContentOp = new Runnable()
        {
            private final PossibleValue.Factory factory = PossibleValue.factory( property );
            
            public void run()
            {
                final List<PossibleValue> possibleValues = this.factory.entries();
                possibleValuesRef.set( possibleValues );
                
                final String[] contentForCombo = new String[ possibleValues.size() ];
                
                for( int i = 0, n = possibleValues.size(); i < n; i++ )
                {
                    contentForCombo[ i ] = possibleValues.get( i ).label();
                }
                
                combo.setItems( contentForCombo );
                
                updateComboSelectionOp.run();
            }
        };
        
        updateComboContentOp.run();
        
        final Listener possibleValuesServiceListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                runOnDisplayThread( updateComboContentOp );
            }
        };
        
        possibleValuesService.attach( possibleValuesServiceListener );
        
        final Listener propertyListener = new FilteredListener<PropertyContentEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyContentEvent event )
            {
                runOnDisplayThread( updateComboSelectionOp );
            }
        };
        
        property.attach( propertyListener );
        
        combo.addModifyListener
        (
            new ModifyListener()
            {
                public void modifyText( final ModifyEvent e )
                {
                    if( PopUpListFieldPropertyEditorPresentation.this.updatingEditor )
                    {
                        return;
                    }
                    
                    try
                    {
                        PopUpListFieldPropertyEditorPresentation.this.updatingModel = true;
                        
                        String value = null;
                        
                        final int index = combo.getSelectionIndex();
                        
                        if( index != -1 )
                        {
                            final List<PossibleValue> possible = possibleValuesRef.get();
                            
                            if( index < possible.size() )
                            {
                                value = possible.get( index ).value();
                            }
                        }
                        
                        if( value == null )
                        {
                            value = combo.getText().trim();
                        }
                        
                        if( value != null && value.length() == 0 )
                        {
                            value = null;
                        }
    
                        setPropertyValue( value );
                    }
                    finally
                    {
                        PopUpListFieldPropertyEditorPresentation.this.updatingModel = false;
                    }
                }
            }
        );
        
        if( this.style == PopUpListFieldStyle.EDITABLE )
        {
            combo.addFocusListener
            (
                new FocusAdapter()
                {
                    @Override
                    public void focusLost( final FocusEvent event )
                    {
                        // If an editable pop-up list was presenting the default value and user clears it, there is
                        // no change in the model, but we need to restore the display of the default value in the UI.
                        
                        updateComboSelectionOp.run();
                    }
                }
            );
        }
        
        addOnDisposeOperation
        (
            new Runnable()
            {
                public void run()
                {
                    possibleValuesService.detach( possibleValuesServiceListener );
                    property.detach( propertyListener );
                }
            }
        );
    }
    
    @Override
    protected void handleFocusReceivedEvent()
    {
        this.combo.setFocus();
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        this.combo = null;
    }
    
    public static final class Factory extends PropertyEditorPresentationFactory
    {
        @Override
        public PropertyEditorPresentation create( final PropertyEditorPart part, final SwtPresentation parent, final Composite composite )
        {
            final String style = part.definition().getStyle().content();
            
            if( style != null && style.startsWith( "Sapphire.PropertyEditor.PopUpListField" ) )
            {
                final Property property = part.property();
                
                if( property.definition() instanceof ValueProperty && property.service( PossibleValuesService.class ) != null )
                {
                    PopUpListFieldStyle popUpListFieldPresentationStyle = null;
                    
                    if( style.equals( "Sapphire.PropertyEditor.PopUpListField" ) )
                    {
                        if( Enum.class.isAssignableFrom( property.definition().getTypeClass() ) )
                        {
                            popUpListFieldPresentationStyle = PopUpListFieldStyle.STRICT;
                        }
                        else
                        {
                            final PossibleValues possibleValuesAnnotation =property.definition().getAnnotation( PossibleValues.class );
                            
                            if( possibleValuesAnnotation != null )
                            {
                                popUpListFieldPresentationStyle 
                                    = ( possibleValuesAnnotation.invalidValueSeverity() == Severity.ERROR 
                                        ? PopUpListFieldStyle.STRICT : PopUpListFieldStyle.EDITABLE );
                            }
                            else
                            {
                                popUpListFieldPresentationStyle = PopUpListFieldStyle.EDITABLE;
                            }
                        }
                    }
                    else if( style.equals( "Sapphire.PropertyEditor.PopUpListField.Editable" ) )
                    {
                        popUpListFieldPresentationStyle = PopUpListFieldStyle.EDITABLE;
                    }
                    else if( style.equals( "Sapphire.PropertyEditor.PopUpListField.Strict" ) )
                    {
                        popUpListFieldPresentationStyle = PopUpListFieldStyle.STRICT;
                    }
                    
                    if( popUpListFieldPresentationStyle != null )
                    {
                        return new PopUpListFieldPropertyEditorPresentation( part, parent, composite, popUpListFieldPresentationStyle );
                    }
                }
            }
            
            return null;
        }
    }

}
