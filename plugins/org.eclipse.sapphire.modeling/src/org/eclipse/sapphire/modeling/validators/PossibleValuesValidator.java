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

package org.eclipse.sapphire.modeling.validators;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.PossibleValuesService;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.ModelPropertyValidator;
import org.eclipse.sapphire.modeling.annotations.PossibleValues;
import org.eclipse.sapphire.modeling.internal.SapphireModelingFrameworkPlugin;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class PossibleValuesValidator

    extends ModelPropertyValidator<Value<?>>
    
{
    public static boolean isNecessary( final ValueProperty property )
    {
        final PossibleValues annotation = property.getAnnotation( PossibleValues.class );
        
        if( annotation != null && 
            ( ! annotation.service().equals( PossibleValuesService.class ) || annotation.invalidValueSeverity() != IStatus.OK ) )
        {
            return true;
        }
        
        return false;
    }
    
    @Override
    public IStatus validate( final Value<?> value )
    {
        final IModelElement modelElement = value.parent();
        final String valueString = value.getText( true );
        
        if( valueString != null )
        {
            final PossibleValuesService valuesProvider = modelElement.service( value.getProperty(), PossibleValuesService.class );
            
            if( valuesProvider != null )
            {
                Collection<String> values = valuesProvider.getPossibleValues();
                
                for( String v : values )
                {
                    if( v == null )
                    {
                        final String msg = NLS.bind( Resources.valuesProviderReturnedNull, valuesProvider.getClass().getName() );
                        SapphireModelingFrameworkPlugin.logError( msg, null );
                        
                        values = Collections.emptyList();
                    }
                }
                
                boolean found = false;
                
                if( valuesProvider.isCaseSensitive() )
                {
                    found = values.contains( valueString );
                }
                else
                {
                    for( String v : values )
                    {
                        if( v.equalsIgnoreCase( valueString ) )
                        {
                            found = true;
                            break;
                        }
                    }
                }

                if( ! found )
                {
                    final int severity = valuesProvider.getInvalidValueSeverity( valueString );
                    final String message = valuesProvider.getInvalidValueMessage( valueString );
                    return new Status( severity, SapphireModelingFrameworkPlugin.PLUGIN_ID, message );
                }
            }
        }
        
        return Status.OK_STATUS;
    }
    
    private static final class Resources
    
        extends NLS
    
    {
        public static String valuesProviderReturnedNull;
        
        static
        {
            initializeMessages( PossibleValuesValidator.class.getName(), Resources.class );
        }
    }
    
}
