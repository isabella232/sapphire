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

import java.util.Collections;

import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.xml.IModelElementForXml;
import org.eclipse.sapphire.modeling.xml.ModelElementListControllerForXml;
import org.eclipse.sapphire.modeling.xml.XmlElement;
import org.eclipse.sapphire.ui.def.ISapphireHint;
import org.eclipse.sapphire.ui.def.ISapphirePropertyEditorDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphirePartDefHintsListController

    extends ModelElementListControllerForXml<ISapphireHint>

{
    private static final String EL_HINT = "hint"; //$NON-NLS-1$
    private static final String EL_PROPERTY = "property"; //$NON-NLS-1$
    
    public SapphirePartDefHintsListController()
    {
        super( Collections.singletonList( EL_HINT ) );
    }

    @Override
    protected ISapphireHint wrap( final XmlElement element )
    {
        return new SapphireHint( getList(), ISapphirePropertyEditorDef.PROP_HINTS, element );
    }
    
    @Override
    public ISapphireHint createNewElement( final ModelElementType type )
    {
        validateEdit();
        
        final XmlElement parentXmlElement = getParentXmlElement( true );
        final String propertyName = parentXmlElement.getText();
        
        if( propertyName.length() > 0 )
        {
            parentXmlElement.setText( null );
        }
        
        final ISapphireHint hint = wrap( parentXmlElement.addChildElement( EL_HINT ) );
        
        if( propertyName.length() > 0 )
        {
            parentXmlElement.format();
            parentXmlElement.setChildNodeText( EL_PROPERTY, propertyName, true );
        }
        
        return hint;
    }

    @Override
    protected XmlElement getParentXmlElement( boolean createIfNecessary )
    {
        return ( (IModelElementForXml) getModelElement() ).getXmlElement( createIfNecessary );
    }
    
}
