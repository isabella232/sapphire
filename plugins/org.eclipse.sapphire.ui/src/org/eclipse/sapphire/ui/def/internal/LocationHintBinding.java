/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.def.internal;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.xml.XmlElement;
import org.eclipse.sapphire.modeling.xml.XmlValueBindingImpl;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class LocationHintBinding

    extends XmlValueBindingImpl
    
{
    private String prefix;
    
    @Override
    public void init( final IModelElement element,
                      final ModelProperty property,
                      final String[] params )
    {
        super.init( element, property, params );
        this.prefix = params[ 0 ];
    }

    @Override
    public String read()
    {
        final XmlElement el = xml( false );
        
        if( el != null )
        {
            String text = el.getText();
            
            if( text != null )
            {
                if( text.toLowerCase().startsWith( this.prefix ) )
                {
                    if( text.length() > this.prefix.length() )
                    {
                        text = text.substring( this.prefix.length() );
                    }
                    else
                    {
                        text = null;
                    }
                }
            }
            
            return text;
        }
        
        return null;
    }

    @Override
    public void write( final String value )
    {
        final XmlElement el = xml( true );
        
        String text = this.prefix;
        
        if( value != null )
        {
            text = text + value;
        }
        
        el.setText( text );
    }
    
}
