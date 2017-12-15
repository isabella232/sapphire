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

package org.eclipse.sapphire.ui.swt.internal;

import static org.eclipse.sapphire.modeling.util.MiscUtil.EMPTY_STRING;
import static org.eclipse.sapphire.modeling.util.MiscUtil.equal;
import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_ASSIST;
import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_BROWSE;
import static org.eclipse.sapphire.ui.SapphireActionSystem.createFilterByActionId;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdvfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glspacing;

import java.util.List;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.PropertyContentEvent;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.services.PossibleValuesService;
import org.eclipse.sapphire.services.ValueNormalizationService;
import org.eclipse.sapphire.ui.PropertyEditorPart;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.renderers.swt.ValuePropertyEditorRenderer;
import org.eclipse.sapphire.ui.swt.renderer.SapphireToolBarActionPresentation;
import org.eclipse.sapphire.util.MutableReference;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class PopUpListFieldPropertyEditorPresentation extends ValuePropertyEditorRenderer
{
    private static final String DATA_DEFAULT_VALUE = "Sapphire.Default.Value";
    
    private final PopUpListFieldStyle style;
    private Combo combo;

    public PopUpListFieldPropertyEditorPresentation( final SapphireRenderingContext context,
                                                     final PropertyEditorPart part,
                                                     final PopUpListFieldStyle style )
    {
        super( context, part );
        
        if( style == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.style = style;
    }

    @Override
    protected void createContents( final Composite parent )
    {
        final PropertyEditorPart part = getPart();
        final IModelElement element = part.getLocalModelElement();
        final ValueProperty property = (ValueProperty) part.getProperty();
        
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
        this.context.adapt( combo );
        this.combo = combo;
        
        if( isActionsToolBarNeeded )
        {
            final ToolBar toolbar = new ToolBar( composite, SWT.FLAT | SWT.HORIZONTAL );
            toolbar.setLayoutData( gdhindent( gdvfill(), 3 ) );
            toolBarActionsPresentation.setToolBar( toolbar );
            toolBarActionsPresentation.render();
            addControl( toolbar );
            this.context.adapt( toolbar );
            decorator.addEditorControl( toolbar );
        }
        
        final PossibleValuesService possibleValuesService = element.service( property, PossibleValuesService.class );
        final ValueNormalizationService valueNormalizationService = element.service( property, ValueNormalizationService.class );
        
        final MutableReference<List<PossibleValue>> possibleValuesRef = new MutableReference<List<PossibleValue>>();
        final MutableReference<Boolean> updatingComboSelection = new MutableReference<Boolean>( false );
        
        final Runnable updateComboSelectionOp = new Runnable()
        {
            public void run()
            {
                final Value<?> value = element.read( property );
                final String text = valueNormalizationService.normalize( value.getText() );
                
                try
                {
                    updatingComboSelection.set( true );

                    combo.setData( DATA_DEFAULT_VALUE, value.isDefault() );
                    
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
                    updatingComboSelection.set( false );
                }
            }
        };

        final Runnable updateComboContentOp = new Runnable()
        {
            private final PossibleValue.Factory factory = PossibleValue.factory( element, property );
            
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
                updateComboContentOp.run();
            }
        };
        
        possibleValuesService.attach( possibleValuesServiceListener );
        
        final Listener propertyListener = new FilteredListener<PropertyContentEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyContentEvent event )
            {
                updateComboSelectionOp.run();
            }
        };
        
        element.attach( propertyListener, property.getName() );
        
        combo.addModifyListener
        (
            new ModifyListener()
            {
                public void modifyText( final ModifyEvent e )
                {
                    if( updatingComboSelection.get() == true )
                    {
                        return;
                    }
                    
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
                    
                    if( value != null )
                    {
                        if( value.length() == 0 )
                        {
                            value = null;
                        }
                        else if( ( (Boolean) combo.getData( DATA_DEFAULT_VALUE ) ) == true && value.equals( element.read( property ).getDefaultText() ) )
                        {
                            value = null;
                        }
                    }

                    element.write( property, value );
                    
                    // If an editable pop-up list was presenting the default value and user clears it, there is
                    // no change in the model, but we need to restore the display of the default value in the UI.
                    
                    updateComboSelectionOp.run();
                }
            }
        );
        
        addOnDisposeOperation
        (
            new Runnable()
            {
                public void run()
                {
                    possibleValuesService.detach( possibleValuesServiceListener );
                    element.detach( propertyListener, property.getName() );
                }
            }
        );
    }
    
    @Override
    protected void handleFocusReceivedEvent()
    {
        this.combo.setFocus();
    }

}
