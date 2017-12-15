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

import org.eclipse.sapphire.modeling.DerivedValueService;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyService;
import org.eclipse.sapphire.modeling.ModelPropertyServiceFactory;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DerivedValue;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class DerivedValueServiceFactory

    extends ModelPropertyServiceFactory
    
{
    @Override
    public boolean applicable( final IModelElement element,
                               final ModelProperty property,
                               final Class<? extends ModelPropertyService> service )
    {
        return ( property instanceof ValueProperty && property.hasAnnotation( DerivedValue.class ) );
    }

    @Override
    public ModelPropertyService create( final IModelElement element,
                                        final ModelProperty property,
                                        final Class<? extends ModelPropertyService> service )
    {
        DerivedValueService derivedValueProviderImpl = null;
        final DerivedValue derivedValueAnnotation = property.getAnnotation( DerivedValue.class );
        
        if( derivedValueAnnotation != null )
        {
            try
            {
                derivedValueProviderImpl = derivedValueAnnotation.service().newInstance();
                derivedValueProviderImpl.init( element, property, derivedValueAnnotation.params() );
            }
            catch( Exception e )
            {
                SapphireModelingFrameworkPlugin.log( e );
                derivedValueProviderImpl = null;
            }
        }
        
        return derivedValueProviderImpl;
    }
    
}
