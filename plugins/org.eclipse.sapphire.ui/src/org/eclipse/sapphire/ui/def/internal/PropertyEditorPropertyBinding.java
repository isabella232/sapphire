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

import org.eclipse.sapphire.modeling.annotations.ValuePropertyCustomBindingImpl;
import org.eclipse.sapphire.modeling.xml.IModelElementForXml;
import org.eclipse.sapphire.modeling.xml.XmlElement;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class PropertyEditorPropertyBinding

    extends ValuePropertyCustomBindingImpl

{
    private static final String EL_HINT = "hint"; //$NON-NLS-1$
    private static final String EL_PROPERTY = "property"; //$NON-NLS-1$
    
    @Override
    public String read()
    {
        final XmlElement el = ( (IModelElementForXml) getModelElement() ).getXmlElement( false );
        final XmlElement propElement = el.getChildElement( EL_PROPERTY, false );
        
        if( propElement != null )
        {
            return propElement.getText();
        }
        else
        {
            return el.getText();
        }
    }

    @Override
    public void write( final String value )
    {
        final XmlElement el = ( (IModelElementForXml) getModelElement() ).getXmlElement( false );
        
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
