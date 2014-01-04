/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.def.internal;

import java.util.List;

import javax.xml.namespace.QName;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Resource;
import org.eclipse.sapphire.modeling.xml.ChildXmlResource;
import org.eclipse.sapphire.modeling.xml.StandardXmlListBindingImpl;
import org.eclipse.sapphire.modeling.xml.XmlElement;
import org.eclipse.sapphire.modeling.xml.XmlResource;
import org.eclipse.sapphire.ui.def.ActionLocationHintAfter;
import org.eclipse.sapphire.ui.def.ActionLocationHintBefore;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class LocationHintsBinding extends StandardXmlListBindingImpl
{
    private static final String EL_LOCATION = "location";
    public static final String BEFORE_PREFIX = "before:";
    public static final String AFTER_PREFIX = "after:";
    
    @Override
    protected void initBindingMetadata()
    {
        this.xmlElementNames = new QName[] { new QName( EL_LOCATION ) };
    }
    
    @Override
    public ElementType type( final Resource resource )
    {
        final String text = ( (XmlResource) resource ).getXmlElement().getText();
        final ElementType type;
        
        if( text != null && text.toLowerCase().startsWith( AFTER_PREFIX ) )
        {
            type = ActionLocationHintAfter.TYPE;
        }
        else
        {
            type = ActionLocationHintBefore.TYPE;
        }
        
        return type;
    }

    @Override
    protected Resource resource( final Object obj )
    {
        return new ChildXmlResource( (XmlResource) property().element().resource(), (XmlElement) obj );
    }

    @Override
    protected Object insertUnderlyingObject( final ElementType type,
                                             final int position )
    {
        final List<?> list = readUnderlyingList();
        final XmlElement refXmlElement = (XmlElement) ( position < list.size() ? list.get( position ) : null );
        final XmlElement xmlElement = getXmlElement( true ).addChildElement( EL_LOCATION, refXmlElement );
        final String prefix = ( type == ActionLocationHintAfter.TYPE ? AFTER_PREFIX : BEFORE_PREFIX );
        xmlElement.setText( prefix );
        
        return xmlElement;
    }
    
}
