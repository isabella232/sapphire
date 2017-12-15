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

package org.eclipse.sapphire.samples.contacts.internal;

import org.eclipse.sapphire.modeling.annotations.ValuePropertyCustomBindingImpl;
import org.eclipse.sapphire.modeling.xml.IModelElementForXml;
import org.eclipse.sapphire.modeling.xml.XmlElement;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class LocalNumberBinding

    extends ValuePropertyCustomBindingImpl
    
{
    private static final String EL_NUMBER = "number";
    
    @Override
    public String read()
    {
        final XmlElement el = ( (IModelElementForXml) getModelElement() ).getXmlElement( false );
        
        final String pnStr = el.getChildNodeText( EL_NUMBER );
        final ParsedPhoneNumber pn = new ParsedPhoneNumber( pnStr );

        return pn.getLocalNumber();
    }

    @Override
    public void write( final String value )
    {
        final XmlElement el = ( (IModelElementForXml) getModelElement() ).getXmlElement( false );
        
        final String pnStr = el.getChildNodeText( EL_NUMBER );
        final ParsedPhoneNumber pn = new ParsedPhoneNumber( pnStr );

        pn.setLocalNumber( value );
        el.setChildNodeText( EL_NUMBER, pn.toString(), true );
    }
    
}
