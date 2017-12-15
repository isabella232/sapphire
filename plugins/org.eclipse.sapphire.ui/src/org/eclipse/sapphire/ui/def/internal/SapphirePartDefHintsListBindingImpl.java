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

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.xml.StandardXmlListBindingImpl;
import org.eclipse.sapphire.modeling.xml.XmlElement;
import org.eclipse.sapphire.ui.def.ISapphireHint;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphirePartDefHintsListBindingImpl

    extends StandardXmlListBindingImpl

{
    private static final String EL_HINT = "hint";
    private static final String EL_PROPERTY = "property";

    @Override
    protected void initBindingMetadata( final IModelElement element,
                                        final ModelProperty property,
                                        final String[] params )
    {
        this.path = null;
        this.xmlElementNames = new String[] { EL_HINT };
        this.modelElementTypes = new ModelElementType[] { ISapphireHint.TYPE };
    }

    @Override
    protected Object addUnderlyingObject( final ModelElementType type )
    {
        final XmlElement parentXmlElement = getXmlElement( true );
        final String propertyName = parentXmlElement.getText();
        
        if( propertyName.length() > 0 )
        {
            parentXmlElement.setText( null );
        }
        
        final XmlElement hintXmlElement = parentXmlElement.addChildElement( EL_HINT );
        
        if( propertyName.length() > 0 )
        {
            parentXmlElement.format();
            parentXmlElement.setChildNodeText( EL_PROPERTY, propertyName, true );
        }
        
        return hintXmlElement;
    }
    
}
