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
import org.eclipse.sapphire.modeling.scripting.Script;
import org.eclipse.sapphire.modeling.scripting.ScriptsManager;
import org.eclipse.sapphire.modeling.scripting.VariableResolver;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ScriptBasedEnabler

    extends EnablerImpl
    
{
    private Script script;
    private VariableResolver variableResolver;
    
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
        
        this.script = ScriptsManager.loadScript( params[ 0 ] );
        
        this.variableResolver = new VariableResolver()
        {
            @Override
            public Object resolve( final String name )
            {
                final ModelProperty property = element.getModelElementType().getProperty( name );
                
                if( property != null && property instanceof ValueProperty )
                {
                    final ValueProperty prop = (ValueProperty) property;
                    final String val = ( (Value<?>) prop.invokeGetterMethod( element ) ).getText();
                    return ( val != null ? val : "" );
                }
                
                return name;
            }
        };
    }

    @Override
    public boolean isEnabled()
    {
        final Object result = this.script.execute( this.variableResolver );
        
        if( result instanceof Boolean )
        {
            return (Boolean) result;
        }
        
        return false;
    }
    
}
