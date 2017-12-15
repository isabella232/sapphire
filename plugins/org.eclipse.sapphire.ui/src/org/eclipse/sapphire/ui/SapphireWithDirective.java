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

package org.eclipse.sapphire.ui;

import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.EditFailedException;
import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ModelElementHandle;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ModelPropertyListener;
import org.eclipse.sapphire.modeling.localization.LabelTransformer;
import org.eclipse.sapphire.ui.def.ISapphireCompositeDef;
import org.eclipse.sapphire.ui.def.ISapphireLabelDef;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;
import org.eclipse.sapphire.ui.def.ISapphireWithDirectiveDef;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.sapphire.ui.internal.binding.RadioButtonsGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireWithDirective

    extends SapphirePageBook
    
{
    private IModelElement modelElementForChildParts;
    private ElementProperty property;
    
    @Override
    protected void init()
    {
        final ISapphireWithDirectiveDef def = (ISapphireWithDirectiveDef) this.definition;
        
        this.property = (ElementProperty) resolve( def.getProperty().getText() );

        super.init();
        
        setExposePageValidationState( true );
        updateCurrentPage( true );
    }
    
    @Override
    protected ISapphireCompositeDef initDefaultPageDef()
    {
        final ISapphireUiDef root = ISapphireUiDef.TYPE.instantiate();
        final ISapphireCompositeDef composite = root.getCompositeDefs().addNewElement();
        final ISapphireLabelDef label = (ISapphireLabelDef) composite.getContent().addNewElement( ISapphireLabelDef.TYPE );
        label.setText( Resources.noAdditionalPropertiesMessage );
        
        return composite;
    }

    public ElementProperty getProperty()
    {
        return this.property;
    }

    @Override
    public void render( final SapphireRenderingContext context )
    {
        final ISapphireWithDirectiveDef def = (ISapphireWithDirectiveDef) this.definition;
        
        final Composite composite = new Composite( context.getComposite(), SWT.NONE );
        composite.setLayoutData( gdhspan( gdhfill(), 2 ) );
        composite.setLayout( glayout( 1, 0, 0 ) );
        context.adapt( composite );
        
        if( ! ( this.property instanceof ImpliedElementProperty ) )
        {
            final List<ModelElementType> allPossibleTypes = this.property.getAllPossibleTypes();
            final IModelElement element = getModelElement();
            final ElementProperty property = this.property;
            final ModelPropertyListener modelPropertyListener;
            
            if( allPossibleTypes.size() == 1 )
            {
                final Button masterCheckBox = new Button( composite, SWT.CHECK );
                masterCheckBox.setLayoutData( gdhindent( gdhspan( gdhfill(), 2 ), 10 ) );
                masterCheckBox.setText( LabelTransformer.transform( def.getLabel().getLocalizedText(), CapitalizationType.FIRST_WORD_ONLY, true ) );
                context.adapt( masterCheckBox );
    
                modelPropertyListener = new ModelPropertyListener()
                {
                    @Override
                    public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
                    {
                        final IModelElement subModelElement = element.read( property ).element();
                        
                        masterCheckBox.setSelection( subModelElement != null );
                        masterCheckBox.setEnabled( element.isPropertyEnabled( property ) );
                        
                        if( subModelElement != null )
                        {
                            context.setHelp( masterCheckBox, subModelElement, property );
                        }
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
                                final ModelElementHandle<?> handle = element.read( property );
                                
                                if( masterCheckBox.getSelection() == true )
                                {
                                    handle.element( true );
                                }
                                else
                                {
                                    handle.remove();
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
                                    SapphireUiFrameworkPlugin.log( e );
                                }
                            }
                        }
                    }
                );
            }
            else
            {
                final RadioButtonsGroup radioButtonsGroup = new RadioButtonsGroup( context, composite, false );
                radioButtonsGroup.setLayoutData( gdhindent( gdhspan( gdhfill(), 2 ), 10 ) );
                context.adapt( radioButtonsGroup );
                
                final Button noneButton = radioButtonsGroup.addRadioButton( Resources.noneRadioButton );
                final Map<ModelElementType,Button> typeToButton = new HashMap<ModelElementType,Button>();
                final Map<Button,ModelElementType> buttonToType = new HashMap<Button,ModelElementType>();
                
                for( ModelElementType type : allPossibleTypes )
                {
                    final String label = type.getLabel( true, CapitalizationType.FIRST_WORD_ONLY, false );
                    final Button button = radioButtonsGroup.addRadioButton( label );
                    typeToButton.put( type, button );
                    buttonToType.put( button, type );
                }
                
                modelPropertyListener = new ModelPropertyListener()
                {
                    @Override
                    public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
                    {
                        final IModelElement subModelElement = element.read( property ).element();
                        final Button button;
                        
                        if( subModelElement == null )
                        {
                            button = noneButton;
                        }
                        else
                        {
                            button = typeToButton.get( subModelElement.getModelElementType() );
                        }
                        
                        if( radioButtonsGroup.getSelection() != button )
                        {
                            radioButtonsGroup.setSelection( button );
                        }
                        
                        radioButtonsGroup.setEnabled( element.isPropertyEnabled( property ) );
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
                                final ModelElementHandle<?> handle = element.read( property );
                                final Button button = radioButtonsGroup.getSelection();
                                
                                if( handle.element() != null && button == noneButton )
                                {
                                    handle.remove();
                                }
                                else
                                {
                                    final ModelElementType type = buttonToType.get( button );
                                    handle.element( true, type );
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
                                    SapphireUiFrameworkPlugin.log( e );
                                }
                            }
                        }
                    }
                );
            }
        
            final Composite separatorComposite = new Composite( composite, SWT.NONE );
            separatorComposite.setLayoutData( gdhindent( gdhspan( gdhfill(), 2 ), 10 ) );
            separatorComposite.setLayout( glayout( 1, 0, 5 ) );
            context.adapt( separatorComposite );
            
            final Label separator = new Label( separatorComposite, SWT.SEPARATOR | SWT.HORIZONTAL );
            separator.setLayoutData( gdhfill() );
            
            modelPropertyListener.handlePropertyChangedEvent( null );
            element.addListener( modelPropertyListener, property.getName() );
            
            composite.addDisposeListener
            (
                new DisposeListener()
                {
                    public void widgetDisposed( final DisposeEvent event )
                    {
                        element.removeListener( modelPropertyListener, property.getName() );
                    }
                }
            );
        }
        
        super.render( new SapphireRenderingContext( this, context, composite ) );
    }

    @Override
    protected void handleModelElementChange( final ModelPropertyChangeEvent event )
    {
        super.handleModelElementChange( event );
        
        if( event.getProperty().getName().equals( this.property.getName() ) )
        {
            updateCurrentPage( false );
        }
    }
    
    @Override
    protected Object parsePageKey( final String pageKeyString )
    {
        final ISapphireUiDef rootdef = this.definition.nearest( ISapphireUiDef.class );
        final Class<?> cl = rootdef.resolveClass( pageKeyString );
        return ClassBasedKey.create( cl );
    }

    private void updateCurrentPage( final boolean force )
    {
        final IModelElement element = getModelElement();
        final IModelElement child;
        
        if( this.property instanceof ImpliedElementProperty )
        {
            child = element.read( (ImpliedElementProperty) this.property );
        }
        else
        {
            child = element.read( this.property ).element();
        }
        
        if( force == true || this.modelElementForChildParts != child )
        {
            this.modelElementForChildParts = child;

            if( this.modelElementForChildParts != null )
            {
                changePage( this.modelElementForChildParts, ClassBasedKey.create( this.modelElementForChildParts ) );
            }
            else
            {
                changePage( element, null );
            }
        }
    }
    
    @Override
    public boolean setFocus( final ModelPath path )
    {
        final ModelPath.Segment head = path.head();
        
        if( head instanceof ModelPath.PropertySegment )
        {
            final String propertyName = ( (ModelPath.PropertySegment) head ).getPropertyName();
            
            if( propertyName.equals( this.property.getName() ) )
            {
                final IModelElement element = getModelElement();
                
                if( element.isPropertyEnabled( this.property ) && element.read( this.property ) != null )
                {
                    super.setFocus( path.tail() );
                }
            }
        }
        
        return false;
    }
    
    private static final class Resources extends NLS
    {
        public static String noneRadioButton;
        public static String noAdditionalPropertiesMessage;
        
        static
        {
            initializeMessages( SapphireWithDirective.class.getName(), Resources.class );
        }
    }

}
