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

package org.eclipse.sapphire.modeling.internal;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.EnablerImpl;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class BooleanPropertyEnabler

    extends EnablerImpl
    
{
    private ValueProperty property;
    
    @Override
    public void init( final IModelElement element,
                      final ModelProperty property,
                      final String[] params )
    {
        super.init( element, property, params );
        
        if( params.length != 1 )
        {
            throw new IllegalArgumentException();
        }
        
        final ModelProperty prop = element.getModelElementType().getProperty( params[ 0 ] );
        
        if( prop == null )
        {
            throw new IllegalArgumentException();
        }

        if( prop.getTypeClass() != Boolean.class )
        {
            throw new IllegalArgumentException();
        }
        
        this.property = (ValueProperty) prop;
    }

    @Override
    public boolean isEnabled()
    {
        final IModelElement element = getModelElement();
        
        if( element.isPropertyEnabled( this.property ) )
        {
            final Value<Boolean> result;
            
            try
            {
                result = this.property.invokeGetterMethod( element );
            }
            catch( Exception e )
            {
                throw new RuntimeException( e );
            }
            
            final Boolean res = result.getContent( true );
            
            if( res != null && res.booleanValue() == true )
            {
                return true;
            }
        }

        return false;
    }
    
}
