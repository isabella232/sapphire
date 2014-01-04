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

import org.eclipse.sapphire.MasterConversionService;
import org.eclipse.sapphire.modeling.xml.StandardXmlNamespaceResolver;
import org.eclipse.sapphire.modeling.xml.XmlElement;
import org.eclipse.sapphire.modeling.xml.XmlNode;
import org.eclipse.sapphire.modeling.xml.XmlPath;
import org.eclipse.sapphire.modeling.xml.XmlValueBindingImpl;
import org.eclipse.sapphire.ui.def.HtmlContentSourceType;
import org.eclipse.sapphire.ui.forms.HtmlPanelDef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SapphireHtmlPanelDefSourceBinding extends XmlValueBindingImpl
{
    private static final StandardXmlNamespaceResolver NAMESPACE_RESOLVER = new StandardXmlNamespaceResolver( HtmlPanelDef.TYPE );
    private static final XmlPath PATH_URL = new XmlPath( "url", NAMESPACE_RESOLVER );
    private static final XmlPath PATH_CONTENT = new XmlPath( "content", NAMESPACE_RESOLVER );
    
    @Override
    public String read()
    {
        final XmlElement element = xml();
        HtmlContentSourceType type = null;
        
        if( element.getChildNode( PATH_URL, false ) != null )
        {
            type = HtmlContentSourceType.REMOTE;
        }
        else if( element.getChildNode( PATH_CONTENT, false ) != null )
        {
            type = HtmlContentSourceType.EMBEDDED;
        }
        
        return property().element().property( HtmlPanelDef.PROP_CONTENT_SOURCE_TYPE ).service( MasterConversionService.class ).convert( type, String.class );
    }

    @Override
    public void write( final String value )
    {
        final XmlElement element = xml();
        final HtmlContentSourceType type = property().element().property( HtmlPanelDef.PROP_CONTENT_SOURCE_TYPE ).service( MasterConversionService.class ).convert( value, HtmlContentSourceType.class );
        
        if( value == null )
        {
            element.removeChildNode( PATH_URL );
            element.removeChildNode( PATH_CONTENT );
        }
        else if( type == HtmlContentSourceType.REMOTE )
        {
            element.removeChildNode( PATH_CONTENT );
            element.getChildNode( PATH_URL, true );
        }
        else if( type == HtmlContentSourceType.EMBEDDED )
        {
            element.removeChildNode( PATH_URL );
            element.getChildNode( PATH_CONTENT, true );
        }
        else
        {
            throw new IllegalStateException();
        }
    }

    @Override
    public XmlNode getXmlNode()
    {
        final XmlElement element = xml();
        
        XmlNode node = element.getChildNode( PATH_URL, false );
        
        if( node != null )
        {
            return node;
        }
        
        node = element.getChildNode( PATH_CONTENT, false );
        
        if( node != null )
        {
            return node;
        }
        
        return super.getXmlNode();
    }
    
}
