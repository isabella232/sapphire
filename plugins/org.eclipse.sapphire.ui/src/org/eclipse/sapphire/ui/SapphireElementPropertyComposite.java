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

import static org.eclipse.sapphire.ui.util.SwtUtil.gdhfill;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdhindent;
import static org.eclipse.sapphire.ui.util.SwtUtil.glayout;
import static org.eclipse.sapphire.ui.util.SwtUtil.hspan;

import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.EditFailedException;
import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.IRemovable;
import org.eclipse.sapphire.modeling.LabelTransformer;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ModelPropertyListener;
import org.eclipse.sapphire.ui.def.ISapphireCompositeDef;
import org.eclipse.sapphire.ui.def.ISapphireElementPropertyCompositeDef;
import org.eclipse.sapphire.ui.def.ISapphireLabelDef;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;
import org.eclipse.sapphire.ui.def.SapphireUiDefFactory;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
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

public class SapphireElementPropertyComposite

    extends SapphirePageBook
    
{
    private IModelElement modelElementForChildParts;
    private ElementProperty property;
    private ISapphireCompositeDef elementAbsentPageDef;
    
    @Override
    protected void init()
    {
        final ISapphireElementPropertyCompositeDef def = (ISapphireElementPropertyCompositeDef) this.definition;
        
        this.property = (ElementProperty) resolve( def.getConditionalProperty().getText() );
        this.elementAbsentPageDef = createElementAbsentPageDef();

        super.init();
        
        setExposePageValidationState( true );
        updateCurrentPage( true );
    }
    
    public ElementProperty getConditionalProperty()
    {
        return this.property;
    }

    @Override
    public void render( final SapphireRenderingContext context )
    {
        final ISapphireElementPropertyCompositeDef def = (ISapphireElementPropertyCompositeDef) this.definition;
        
        final Composite composite = new Composite( context.getComposite(), SWT.NONE );
        composite.setLayoutData( hspan( gdhfill(), 2 ) );
        composite.setLayout( glayout( 1, 0, 0 ) );
        context.adapt( composite );
        
        final Button masterCheckBox = new Button( composite, SWT.CHECK );
        masterCheckBox.setLayoutData( gdhindent( hspan( gdhfill(), 2 ), 10 ) );
        masterCheckBox.setText( LabelTransformer.transform( def.getConditionalLabel().getLocalizedText(), CapitalizationType.FIRST_WORD_ONLY, true ) );
        context.adapt( masterCheckBox );
        
        final Composite separatorComposite = new Composite( composite, SWT.NONE );
        separatorComposite.setLayoutData( gdhindent( hspan( gdhfill(), 2 ), 10 ) );
        separatorComposite.setLayout( glayout( 1, 0, 5 ) );
        context.adapt( separatorComposite );
        
        final Label separator = new Label( separatorComposite, SWT.SEPARATOR | SWT.HORIZONTAL );
        separator.setLayoutData( gdhfill() );
        
        super.render( new SapphireRenderingContext( this, context, composite ) );
        
        final IModelElement modelElement = getModelElement();
        final ElementProperty property = this.property;
        
        final ModelPropertyListener modelPropertyListener = new ModelPropertyListener()
        {
            @Override
            public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
            {
                final IModelElement subModelElement = (IModelElement) property.invokeGetterMethod( modelElement );
                
                masterCheckBox.setSelection( subModelElement != null );
                masterCheckBox.setEnabled( modelElement.isPropertyEnabled( property ) );
                
                if( subModelElement != null )
                {
                    context.setHelp( masterCheckBox, subModelElement, property );
                }
            }
        };
        
        modelPropertyListener.handlePropertyChangedEvent( null );
        modelElement.addListener( modelPropertyListener, property.getName() );
        
        composite.addDisposeListener
        (
            new DisposeListener()
            {
                public void widgetDisposed( final DisposeEvent event )
                {
                    modelElement.removeListener( modelPropertyListener, property.getName() );
                }
            }
        );
            
        masterCheckBox.addSelectionListener
        (
            new SelectionAdapter()
            {
                @Override
                public void widgetSelected( final SelectionEvent event )
                {
                    try
                    {
                        if( masterCheckBox.getSelection() == true )
                        {
                            property.invokeGetterMethod( modelElement, true );
                        }
                        else
                        {
                            final IRemovable subModelElement = (IRemovable) property.invokeGetterMethod( modelElement );
                            
                            if( subModelElement != null )
                            {
                                subModelElement.remove();
                            }
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
        final ISapphireUiDef rootdef = (ISapphireUiDef) this.definition.getModel();
        final Class<?> cl = rootdef.resolveClass( pageKeyString );
        return ClassBasedKey.create( cl );
    }

    private void updateCurrentPage( final boolean force )
    {
        final IModelElement modelElement = getModelElement();
        final IModelElement subModelElement = (IModelElement) this.property.invokeGetterMethod( modelElement );
        
        if( force == true || this.modelElementForChildParts != subModelElement )
        {
            this.modelElementForChildParts = subModelElement;

            if( this.modelElementForChildParts != null )
            {
                changePage( this.modelElementForChildParts, ClassBasedKey.create( this.modelElementForChildParts ) );
            }
            else
            {
                changePage( modelElement, this.elementAbsentPageDef );
            }
        }
    }
    
    private ISapphireCompositeDef createElementAbsentPageDef()
    {
        final ISapphireUiDef root = SapphireUiDefFactory.create();
        final ISapphireCompositeDef composite = root.getCompositeDefs().addNewElement();
        final ISapphireLabelDef label = (ISapphireLabelDef) composite.getContent().addNewElement( ISapphireLabelDef.TYPE );
        label.setText( Resources.noAdditionalPropertiesMessage );
        
        return composite;
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
                
                if( element.isPropertyEnabled( this.property ) && element.service().read( this.property ) != null )
                {
                    super.setFocus( path.tail() );
                }
            }
        }
        
        return false;
    }
    
    private static final class Resources
    
        extends NLS
    
    {
        public static String noAdditionalPropertiesMessage;
        
        static
        {
            initializeMessages( SapphireElementPropertyComposite.class.getName(), Resources.class );
        }
    }

}
