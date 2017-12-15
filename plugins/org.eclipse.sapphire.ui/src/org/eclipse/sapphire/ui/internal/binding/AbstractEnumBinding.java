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

package org.eclipse.sapphire.ui.internal.binding;

import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.swt.widgets.Control;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class AbstractEnumBinding 

    extends AbstractBinding
    
{
    protected Enum<?>[] enumValues;
    
    public AbstractEnumBinding( final IModelElement modelElement,
                                final ValueProperty property,
                                final SapphireRenderingContext context,
                                final Control control )
    {
        super( modelElement, property, context, control );
    }
    
    @Override
    
    protected void initialize( final IModelElement modelElement,
                               final ModelProperty property,
                               final SapphireRenderingContext context,
                               final Control control )
    {
        super.initialize( modelElement, property, context, control );
        
        this.enumValues = (Enum<?>[]) getProperty().getTypeClass().getEnumConstants();
    }
    
    @Override
    
    public ValueProperty getProperty()
    {
        return (ValueProperty) super.getProperty();
    }

    @Override
    @SuppressWarnings( "unchecked" )
    
    public Value<Enum<?>> getPropertyValue()
    {
        return (Value<Enum<?>>) super.getPropertyValue();
    }
    
    @Override
    
    protected final void doUpdateModel()
    {
        final int index = getSelectionIndex();
        
        if( index >= 0 && index < this.enumValues.length )
        {
            final Enum<?> newValue = this.enumValues[ index ];
            getModelElement().service().write( getProperty(), newValue );
            removeMalformedItem();
        }
    }

    @Override
    
    protected final void doUpdateTarget()
    {
        final int existingSelection = getSelectionIndex();
        final Value<Enum<?>> value = getPropertyValue();

        int newSelection = this.enumValues.length;
        
        if( ! value.isMalformed() )
        {
            final Enum<?> newValueEnum = value.getContent( true );
            
            for( int i = 0, n = this.enumValues.length; i < n; i++ )
            {
                if( this.enumValues[ i ] == newValueEnum )
                {
                    newSelection = i;
                    break;
                }
            }
        }
        
        if( newSelection == this.enumValues.length )
        {
            final String newValueString = value.getText( true );
            final String label = ( newValueString == null ? Resources.nullValueLabel : newValueString );

            createMalformedItem( label );
        }
        else
        {
            removeMalformedItem();
        }
        
        if( existingSelection != newSelection )
        {
            setSelectionIndex( newSelection );
        }
    }
    
    protected abstract int getSelectionIndex();
    protected abstract void setSelectionIndex( int index );
    protected abstract void createMalformedItem( String label );
    protected abstract void removeMalformedItem();
    
    protected static final class Resources
        
        extends NLS
        
    {
        public static String nullValueLabel;
    
        static
        {
            initializeMessages( AbstractEnumBinding.class.getName(), 
                                Resources.class );
        }
    }

}
