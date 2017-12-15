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

import javax.xml.namespace.QName;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Resource;
import org.eclipse.sapphire.modeling.xml.ChildXmlResource;
import org.eclipse.sapphire.modeling.xml.StandardXmlListBindingImpl;
import org.eclipse.sapphire.modeling.xml.XmlElement;
import org.eclipse.sapphire.modeling.xml.XmlResource;
import org.eclipse.sapphire.ui.def.ISapphireActionLocationHintAfter;
import org.eclipse.sapphire.ui.def.ISapphireActionLocationHintBefore;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class LocationHintsBinding

    extends StandardXmlListBindingImpl
    
{
    private static final String EL_LOCATION = "location";
    public static final String BEFORE_PREFIX = "before:";
    public static final String AFTER_PREFIX = "after:";
    
    @Override
    protected void initBindingMetadata( final IModelElement element,
                                        final ModelProperty property,
                                        final String[] params )
    {
        this.xmlElementNames = new QName[] { new QName( EL_LOCATION ) };
    }
    
    @Override
    public ModelElementType type( final Resource resource )
    {
        final String text = ( (XmlResource) resource ).getXmlElement().getText();
        final ModelElementType type;
        
        if( text != null && text.toLowerCase().startsWith( AFTER_PREFIX ) )
        {
            type = ISapphireActionLocationHintAfter.TYPE;
        }
        else
        {
            type = ISapphireActionLocationHintBefore.TYPE;
        }
        
        return type;
    }

    @Override
    protected Object addUnderlyingObject( final ModelElementType type )
    {
        final XmlElement xmlElement = getXmlElement( true ).addChildElement( EL_LOCATION );
        final String prefix = ( type == ISapphireActionLocationHintAfter.TYPE ? AFTER_PREFIX : BEFORE_PREFIX );
        xmlElement.setText( prefix );
        
        return xmlElement;
    }

    @Override
    protected Resource createResource( final Object obj )
    {
        return new ChildXmlResource( (XmlResource) element().resource(), (XmlElement) obj );
    }
    
}
