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

package org.eclipse.sapphire.ui.def.internal;

import org.eclipse.sapphire.modeling.xml.XmlElement;
import org.eclipse.sapphire.modeling.xml.XmlValueBindingImpl;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class PropertyEditorPropertyBinding

    extends XmlValueBindingImpl

{
    private static final String EL_HINT = "hint"; //$NON-NLS-1$
    private static final String EL_PROPERTY = "property"; //$NON-NLS-1$
    
    @Override
    public String read()
    {
        String value = null;
        final XmlElement el = xml( false );
        
        if( el != null )
        {
            final XmlElement propElement = el.getChildElement( EL_PROPERTY, false );
            
            if( propElement != null )
            {
                value = propElement.getText();
            }
            else
            {
                value = el.getText();
            }
        }
        
        return value;
    }

    @Override
    public void write( final String value )
    {
        final XmlElement el = xml( true );
        
        if( el.getChildElement( EL_PROPERTY, false ) != null ||
            el.getChildElement( EL_HINT, false ) != null )
        {
            el.setChildNodeText( EL_PROPERTY, value, true );
        }
        else
        {
            el.setText( value );
        }
    }

}
