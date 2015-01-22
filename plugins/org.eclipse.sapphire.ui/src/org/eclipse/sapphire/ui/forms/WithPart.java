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

package org.eclipse.sapphire.ui.forms;

import java.util.Collections;
import java.util.Set;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.PropertyEvent;
import org.eclipse.sapphire.PropertyValidationEvent;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.el.AndFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.localization.LabelTransformer;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.sapphire.ui.forms.swt.internal.WithPresentation;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class WithPart extends PageBookPart
{
    private ModelPath path;
    private ElementHandle<?> property;
    private Element elementForChildParts;
    private Listener listener;
    private FunctionResult labelFunctionResult;
    
    @Override
    protected void init()
    {
        final WithDef def = (WithDef) this.definition;
        
        this.path = new ModelPath( substituteParams( def.getPath().text() ) );
        this.property = (ElementHandle<?>) getModelElement().property( this.path );
        
        if( this.property == null )
        {
            throw new IllegalStateException();
        }
        
        super.init();
        
        setExposePageValidationState( true );
        
        this.listener = new FilteredListener<PropertyEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyEvent event )
            {
                if( event instanceof PropertyContentEvent )
                {
                    updateCurrentPage( false );
                }
                else if( event instanceof PropertyValidationEvent )
                {
                    refreshValidation();
                }
            }
        };
        
        this.property.attach( this.listener );
        
        updateCurrentPage( true );
    }
    
    @Override
    protected Function initVisibleWhenFunction()
    {
        return AndFunction.create
        (
            super.initVisibleWhenFunction(),
            createVersionCompatibleFunction( this.property )
        );
    }
    
    @Override
    public WithDef definition()
    {
        return (WithDef) super.definition();
    }

    public ModelPath getPath()
    {
        return this.path;
    }
    
    @Override
    public Element getLocalModelElement()
    {
        return this.property.element();
    }
    
    public ElementHandle<?> property()
    {
        return this.property;
    }
    
    public String label()
    {
        return label( CapitalizationType.NO_CAPS, true );
    }
    
    public String label( final CapitalizationType capitalizationType, final boolean includeMnemonic )
    {
        final WithDef def = definition();
        
        if( def.getShowLabel().content() )
        {
            if( this.labelFunctionResult == null )
            {
                this.labelFunctionResult = initExpression
                (
                    def.getLabel().content(), 
                    String.class,
                    Literal.create( this.property.definition().getLabel( false, CapitalizationType.NO_CAPS, true ) ),
                    new Runnable()
                    {
                        public void run()
                        {
                            broadcast( new LabelChangedEvent( WithPart.this ) );
                        }
                    }
                );
            }
            
            final String label = (String) this.labelFunctionResult.value();
            return LabelTransformer.transform( label, capitalizationType, includeMnemonic );
        }
        
        return null;
    }
    
    @Override
    protected Status computeValidation()
    {
        Status state = super.computeValidation();
        
        if( this.property != null )
        {
            final Status.CompositeStatusFactory factory = Status.factoryForComposite();
            factory.merge( ( (ElementHandle<?>) this.property ).validation() );
            factory.merge( state );
            
            state = factory.create();
        }
        
        return state;
    }

    private void updateCurrentPage( final boolean force )
    {
        final Element child = ( (ElementHandle<?>) this.property ).content();
        
        if( force == true || this.elementForChildParts != child )
        {
            this.elementForChildParts = child;
            changePage( this.elementForChildParts );
        }
    }
    
    @Override
    public boolean setFocus( final ModelPath path )
    {
        if( this.path.isPrefixOf( path ) )
        {
            final ModelPath tail = path.makeRelativeTo( this.path );
            
            if( this.property == null || this.property.enabled() )
            {
                return super.setFocus( tail );
            }
        }
        
        return false;
    }
    
    @Override
    public Set<String> getActionContexts()
    {
        return Collections.singleton( SapphireActionSystem.CONTEXT_WITH_DIRECTIVE );
    }
    
    @Override
    public FormComponentPresentation createPresentation( final SwtPresentation parent, final Composite composite )
    {
        return new WithPresentation( this, parent, composite );
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.listener != null )
        {
            this.property.detach( this.listener );
        }
    }
    
    public enum Style
    {
        CHECKBOX( "checkbox" ),
        RADIO_BUTTONS( "radio.buttons" ),
        DROP_DOWN_LIST( "drop.down.list" );
        
        public static Style decode( final String text )
        {
            if( text != null )
            {
                for( Style style : Style.values() )
                {
                    if( style.text.equals( text ) )
                    {
                        return style;
                    }
                }
            }
            
            return null;
        }

        private final String text;
        
        private Style( final String text )
        {
            this.text = text;
        }
        
        @Override
        public String toString()
        {
            return this.text;
        }
    }

}
