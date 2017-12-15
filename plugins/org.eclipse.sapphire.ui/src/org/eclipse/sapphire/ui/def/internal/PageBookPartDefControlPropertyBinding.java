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

import org.eclipse.sapphire.modeling.xml.StandardXmlNamespaceResolver;
import org.eclipse.sapphire.modeling.xml.XmlElement;
import org.eclipse.sapphire.modeling.xml.XmlPath;
import org.eclipse.sapphire.modeling.xml.XmlValueBindingImpl;
import org.eclipse.sapphire.ui.def.ISapphirePageBookExtDef;
import org.eclipse.sapphire.ui.def.PageBookPartControlMethod;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class PageBookPartDefControlPropertyBinding

    extends XmlValueBindingImpl
    
{
    private static final StandardXmlNamespaceResolver NAMESPACE_RESOLVER = new StandardXmlNamespaceResolver( ISapphirePageBookExtDef.TYPE );
    private static final XmlPath PATH_ENUM_CONTROLLER_PROPERTY = new XmlPath( "enum-controller/property", NAMESPACE_RESOLVER );
    private static final XmlPath PATH_LIST_SELECTION_CONTROLLER_PROPERTY = new XmlPath( "list-selection-controller/property", NAMESPACE_RESOLVER );
    
    @Override
    public String read()
    {
        final XmlElement el = xml( false );
        final PageBookPartControlMethod method = ( (ISapphirePageBookExtDef) element() ).getControlMethod().getContent();
        
        if( method == PageBookPartControlMethod.ENUM_VALUE )
        {
            return el.getChildNodeText( PATH_ENUM_CONTROLLER_PROPERTY );
        }
        else if( method == PageBookPartControlMethod.LIST_SELECTION )
        {
            return el.getChildNodeText( PATH_LIST_SELECTION_CONTROLLER_PROPERTY );
        }
        
        return null;
    }

    @Override
    public void write( final String value )
    {
        final PageBookPartControlMethod method = ( (ISapphirePageBookExtDef) element() ).getControlMethod().getContent();
        final XmlElement el = xml( true );
        
        if( method == PageBookPartControlMethod.ENUM_VALUE )
        {
            el.setChildNodeText( PATH_ENUM_CONTROLLER_PROPERTY, value, true );
        }
        else if( method == PageBookPartControlMethod.LIST_SELECTION )
        {
            el.setChildNodeText( PATH_LIST_SELECTION_CONTROLLER_PROPERTY, value, true );
        }
    }

}
