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

package org.eclipse.sapphire.modeling;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ElementProperty 

    extends ModelProperty
    
{
    private final Method getterWithCreateParam;
    
    public ElementProperty( final ModelElementType type,
                            final String propertyName )
    {
        this( type, propertyName, null );
    }
        
    public ElementProperty( final ModelElementType type,
                            final ElementProperty baseProperty )
    {
        this( type, baseProperty.getName(), baseProperty );
    }
    
    private ElementProperty( final ModelElementType type,
                             final String propertyName,
                             final ElementProperty baseProperty )
    {
        super( type, propertyName, baseProperty );
        
        final String getterName = getGetterMethod().getName();
        
        Method getterWithCreateParam = null;
        final Class<?>[] expectedParameterTypes = new Class<?>[] { boolean.class };
        
        for( Method method : type.getModelElementClass().getMethods() )
        {
            if( method.getName().equals( getterName ) && 
                Arrays.equals( method.getParameterTypes(), expectedParameterTypes ) )
            {
                getterWithCreateParam = method;
                break;
            }
        }
        
        this.getterWithCreateParam = getterWithCreateParam;
    }
    
    public Object invokeGetterMethod( final Object model,
                                      final boolean createIfNecessary )
    {
        if( createIfNecessary == false )
        {
            return invokeGetterMethod( model );
        }
        else
        {
            try
            {
                return this.getterWithCreateParam.invoke( model, new Object[] { createIfNecessary } );
            }
            catch( Exception e )
            {
                throw new RuntimeException( e );
            }
        }
    }
    
}
