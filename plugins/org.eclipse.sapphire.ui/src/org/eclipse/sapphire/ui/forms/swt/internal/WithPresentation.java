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
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glspacing;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.PropertyEvent;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.EditFailedException;
import org.eclipse.sapphire.services.PossibleTypesService;
import org.eclipse.sapphire.ui.SapphireActionGroup;
import org.eclipse.sapphire.ui.SapphirePart.LabelChangedEvent;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.WithDef;
import org.eclipse.sapphire.ui.forms.WithPart;
import org.eclipse.sapphire.ui.forms.WithPart.Style;
import org.eclipse.sapphire.ui.forms.swt.RadioButtonsGroup;
import org.eclipse.sapphire.ui.forms.swt.SapphireActionPresentationManager;
import org.eclipse.sapphire.ui.forms.swt.SapphireKeyboardActionPresentation;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class WithPresentation extends PageBookPresentation
{
    @Text( "None" )
    private static LocalizableText noneSelection;
    
    @Text( "Enable {0}" )
    private static LocalizableText enableElementLabel; 
    
    static
    {
        LocalizableText.init( WithPresentation.class );
    }

    public WithPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
    {
        super( part, parent, composite );
    }

    @Override
    public WithPart part()
    {
        return (WithPart) super.part();
    }
    
    @Override
    public void render()
    {
        final WithPart part = part();
        final WithDef def = part.definition();
        final ElementHandle<?> property = part.property();
        
        final Composite composite = new Composite( composite(), SWT.NONE );
        composite.setLayoutData( gdhspan( ( part.getScaleVertically() ? gdfill() : gdhfill() ), 2 ) );
        composite.setLayout( glayout( 1, 0, 0 ) );
        
        register( composite );
        
        final PossibleTypesService possibleTypesService = property.service( PossibleTypesService.class );

        final Composite typeSelectorComposite = new Composite( composite, SWT.NONE );
        typeSelectorComposite.setLayoutData( gdhfill() );
        
        final Runnable renderTypeSelectorOp = new Runnable()
        {
            public void run()
            {
                for( Control control : typeSelectorComposite.getChildren() )
                {
                    control.dispose();
                }
                
                final SortedSet<ElementType> allPossibleTypes = possibleTypesService.types();
                final int allPossibleTypesCount = allPossibleTypes.size();
                final Runnable updateUserInterfaceOp;
                
                final Style defaultStyle;
                
                if( allPossibleTypesCount == 1 )
                {
                    defaultStyle = Style.CHECKBOX;
                }
                else if( allPossibleTypesCount <= 3 )
                {
                    defaultStyle = Style.RADIO_BUTTONS;
                }
                else
                {
                    defaultStyle = Style.DROP_DOWN_LIST;
                }
                
                Style style = Style.decode( def.getHint( WithDef.HINT_STYLE ) );
                
                if( style == null || ( style == Style.CHECKBOX && allPossibleTypesCount != 1 ) )
                {
                    style = defaultStyle;
                }
                
                final SapphireActionGroup actions = part.getActions();
                final SapphireActionPresentationManager actionPresentationManager = new SapphireActionPresentationManager( WithPresentation.this, actions );
                final SapphireKeyboardActionPresentation actionPresentationKeyboard = new SapphireKeyboardActionPresentation( actionPresentationManager );
                
                final boolean showLabel = ( part.label() != null );

                if( style == Style.CHECKBOX )
                {
                    typeSelectorComposite.setLayout( glspacing( glayout( 2, 0, 0 ), 2 ) );
                    
                    final PropertyEditorAssistDecorator decorator = new PropertyEditorAssistDecorator( part, property, typeSelectorComposite );
                    decorator.control().setLayoutData( gd() );
                    
                    final Button masterCheckBox = new Button( typeSelectorComposite, SWT.CHECK );
                    masterCheckBox.setLayoutData( gd() );
                    decorator.addEditorControl( masterCheckBox );
                    actionPresentationKeyboard.attach( masterCheckBox );
                    attachHelp( masterCheckBox, property );
                    
                    if( showLabel )
                    {
                        masterCheckBox.setText( part.label( CapitalizationType.FIRST_WORD_ONLY, true ) );
                        
                        attachPartListener
                        (
                            new FilteredListener<LabelChangedEvent>()
                            {
                                @Override
                                protected void handleTypedEvent( final LabelChangedEvent event )
                                {
                                    masterCheckBox.setText( part.label( CapitalizationType.FIRST_WORD_ONLY, true ) );
                                    layout();
                                }
                            }
                        );
                    }
                    
                    updateUserInterfaceOp = new Runnable()
                    {
                        public void run()
                        {
                            if( Display.getCurrent() == null )
                            {
                                masterCheckBox.getDisplay().asyncExec( this );
                                return;
                            }
                            
                            final Element subModelElement = ( (ElementHandle<?>) property ).content();
                            
                            masterCheckBox.setSelection( subModelElement != null );
                            masterCheckBox.setEnabled( property.enabled() );
                        }
                    };
                            
                    masterCheckBox.addSelectionListener
                    (
                        new SelectionAdapter()
                        {
                            @Override
                            public void widgetSelected( final SelectionEvent event )
                            {
                                try
                                {
                                    final ElementHandle<?> handle = (ElementHandle<?>) property;
                                    
                                    if( masterCheckBox.getSelection() == true )
                                    {
                                        handle.content( true );
                                    }
                                    else
                                    {
                                        handle.clear();
                                    }
                                }
                                catch( Exception e )
                                {
                                    // Note that the EditFailedException is ignored here because the user has already
                                    // been notified and likely has taken action that led to the exception (such as
                                    // declining to make a file writable).
                                    
                                    final EditFailedException editFailedException = EditFailedException.findAsCause( e );
                                    
                                    if( editFailedException == null )
                                    {
                                        Sapphire.service( LoggingService.class ).log( e );
                                    }
                                }
                            }
                        }
                    );
                }
                else
                {
                    typeSelectorComposite.setLayout( glspacing( glayout( 3, 0, 0 ), 2 ) );
                    
                    if( showLabel )
                    {
                        final Label label = new Label( typeSelectorComposite, SWT.NONE );
                        label.setLayoutData( gdhindent( gd(), 9 ) );
                        label.setText( part.label( CapitalizationType.FIRST_WORD_ONLY, true ) + ":" );
                        
                        attachPartListener
                        (
                            new FilteredListener<LabelChangedEvent>()
                            {
                                @Override
                                protected void handleTypedEvent( final LabelChangedEvent event )
                                {
                                    label.setText( part.label( CapitalizationType.FIRST_WORD_ONLY, true ) + ":" );
                                    layout();
                                }
                            }
                        );
                    }
                    
                    final PropertyEditorAssistDecorator decorator = new PropertyEditorAssistDecorator( part, property, typeSelectorComposite );
                    decorator.control().setLayoutData( gdhindent( gdvalign( gd(), ( style == Style.DROP_DOWN_LIST ? SWT.TOP : SWT.CENTER ) ), ( showLabel ? 3 : 0 ) ) );
                    
                    if( style == Style.RADIO_BUTTONS )
                    {
                        final RadioButtonsGroup radioButtonsGroup = new RadioButtonsGroup( typeSelectorComposite, false );
                        radioButtonsGroup.setLayoutData( gdhfill() );
                        
                        final Map<ElementType,Button> typeToButton = new HashMap<ElementType,Button>();
                        final Map<Button,ElementType> buttonToType = new HashMap<Button,ElementType>();
                        
                        for( final ElementType type : allPossibleTypes )
                        {
                            final String label = type.getLabel( true, CapitalizationType.FIRST_WORD_ONLY, false );
                            final Button button = radioButtonsGroup.addRadioButton( label );
                            typeToButton.put( type, button );
                            buttonToType.put( button, type );
                            decorator.addEditorControl( button );
                            actionPresentationKeyboard.attach( button );
                            attachHelp( button, property );
                        }
                        
                        final Button noneButton = radioButtonsGroup.addRadioButton( noneSelection.text() );
                        noneButton.setVisible( false );
                        decorator.addEditorControl( noneButton );
                        actionPresentationKeyboard.attach( noneButton );
                        attachHelp( noneButton, property );
                        
                        updateUserInterfaceOp = new Runnable()
                        {
                            public void run()
                            {
                                if( Display.getCurrent() == null )
                                {
                                    radioButtonsGroup.getDisplay().asyncExec( this );
                                    return;
                                }
                                
                                final Element subModelElement = ( (ElementHandle<?>) property ).content();
                                final Button button;
                                
                                if( subModelElement == null )
                                {
                                    button = noneButton;
                                    noneButton.setVisible( true );
                                }
                                else
                                {
                                    button = typeToButton.get( subModelElement.type() );
                                    noneButton.setVisible( false );
                                }
                                
                                if( radioButtonsGroup.getSelection() != button )
                                {
                                    radioButtonsGroup.setSelection( button );
                                }
                                
                                radioButtonsGroup.setEnabled( property.enabled() );
                            }
                        };
                                
                        radioButtonsGroup.addSelectionListener
                        (
                            new SelectionAdapter()
                            {
                                @Override
                                public void widgetSelected( final SelectionEvent event )
                                {
                                    try
                                    {
                                        final ElementHandle<?> handle = (ElementHandle<?>) property;
                                        final Button button = radioButtonsGroup.getSelection();
                                        
                                        if( button == noneButton )
                                        {
                                            handle.clear();
                                        }
                                        else
                                        {
                                            final ElementType type = buttonToType.get( button );
                                            handle.content( true, type );
                                        }
                                    }
                                    catch( Exception e )
                                    {
                                        // Note that the EditFailedException is ignored here because the user has already
                                        // been notified and likely has taken action that led to the exception (such as
                                        // declining to make a file writable).
                                        
                                        final EditFailedException editFailedException = EditFailedException.findAsCause( e );
                                        
                                        if( editFailedException == null )
                                        {
                                            Sapphire.service( LoggingService.class ).log( e );
                                        }
                                    }
                                }
                            }
                        );
                    }
                    else if( style == Style.DROP_DOWN_LIST )
                    {
                        final Combo combo = new Combo( typeSelectorComposite, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY );
                        combo.setLayoutData( gdhfill() );
                        decorator.addEditorControl( combo );
                        actionPresentationKeyboard.attach( combo );
                        attachHelp( combo, property );
                        
                        final Map<ElementType,Integer> typeToIndex = new HashMap<ElementType,Integer>();
                        final Map<Integer,ElementType> indexToType = new HashMap<Integer,ElementType>();
                        
                        int index = 0;
                        
                        for( ElementType type : allPossibleTypes )
                        {
                            final String label = type.getLabel( true, CapitalizationType.FIRST_WORD_ONLY, false );
                            combo.add( label );
                            typeToIndex.put( type, index );
                            indexToType.put( index, type );
                            
                            index++;
                        }
                        
                        updateUserInterfaceOp = new Runnable()
                        {
                            public void run()
                            {
                                if( Display.getCurrent() == null )
                                {
                                    combo.getDisplay().asyncExec( this );
                                    return;
                                }
                                
                                final Element subModelElement = ( (ElementHandle<?>) property ).content();
                                final int index;
                                
                                if( subModelElement == null )
                                {
                                    index = -1;
                                }
                                else
                                {
                                    index = typeToIndex.get( subModelElement.type() );
                                }
                                
                                if( combo.getSelectionIndex() != index )
                                {
                                    if( index == -1 )
                                    {
                                        combo.deselectAll();
                                    }
                                    else
                                    {
                                        combo.select( index );
                                    }
                                }
                                
                                combo.setEnabled( property.enabled() );
                            }
                        };
    
                        combo.addSelectionListener
                        (
                            new SelectionAdapter()
                            {
                                @Override
                                public void widgetSelected( final SelectionEvent event )
                                {
                                    try
                                    {
                                        final ElementHandle<?> handle = (ElementHandle<?>) property;
                                        final int index = combo.getSelectionIndex();
                                        
                                        if( index == -1 )
                                        {
                                            handle.clear();
                                        }
                                        else
                                        {
                                            final ElementType type = indexToType.get( index );
                                            handle.content( true, type );
                                        }
                                    }
                                    catch( Exception e )
                                    {
                                        // Note that the EditFailedException is ignored here because the user has already
                                        // been notified and likely has taken action that led to the exception (such as
                                        // declining to make a file writable).
                                        
                                        final EditFailedException editFailedException = EditFailedException.findAsCause( e );
                                        
                                        if( editFailedException == null )
                                        {
                                            Sapphire.service( LoggingService.class ).log( e );
                                        }
                                    }
                                }
                            }
                        );
                    }
                    else
                    {
                        throw new IllegalStateException();
                    }
                }
                
                actionPresentationKeyboard.render();
                
                updateUserInterfaceOp.run();
                
                final Listener modelPropertyListener = new FilteredListener<PropertyEvent>()
                {
                    @Override
                    protected void handleTypedEvent( final PropertyEvent event )
                    {
                        updateUserInterfaceOp.run();
                    }
                };
                
                property.attach( modelPropertyListener );
                
                typeSelectorComposite.layout( true, true );
                
                typeSelectorComposite.getChildren()[ 0 ].addDisposeListener
                (
                    new DisposeListener()
                    {
                        public void widgetDisposed( final DisposeEvent event )
                        {
                            property.detach( modelPropertyListener );
                            actionPresentationManager.dispose();
                            actionPresentationKeyboard.dispose();
                        }
                    }
                );
            }
        };
        
        renderTypeSelectorOp.run();
        
        final Listener possibleTypesServiceListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                renderTypeSelectorOp.run();
            }
        };
        
        possibleTypesService.attach( possibleTypesServiceListener );
        
        typeSelectorComposite.addDisposeListener
        (
            new DisposeListener()
            {
                public void widgetDisposed( final DisposeEvent event )
                {
                    possibleTypesService.detach( possibleTypesServiceListener );
                }
            }
        );
    
        final Composite separatorComposite = new Composite( composite, SWT.NONE );
        separatorComposite.setLayoutData( gdhindent( gdhspan( gdhfill(), 2 ), 9 ) );
        separatorComposite.setLayout( glayout( 1, 0, 5 ) );
        
        final Label separator = new Label( separatorComposite, SWT.SEPARATOR | SWT.HORIZONTAL );
        separator.setLayoutData( gdhfill() );
        
        super.render( composite );
    }

}
