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

package org.eclipse.sapphire.modeling.xml;

import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.modeling.xml.XmlNode.Event;
import org.eclipse.sapphire.modeling.xml.XmlNode.EventType;
import org.eclipse.sapphire.modeling.xml.annotations.CustomXmlValueBinding;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class FoldingXmlValueBindingImpl extends XmlValueBindingImpl
{
    private String elementName;
    private XmlElement.Listener listener;
    
    @Override
    public void init( final Property property )
    {
        super.init( property );
        
        final XmlElement root = xml();
        
        this.elementName = property.definition().getAnnotation( CustomXmlValueBinding.class ).params()[ 0 ];
        final String finalElementName = this.elementName;
        
        this.listener = new XmlElement.Listener()
        {
            @Override
            public void handle( final Event event )
            {
                final EventType type = event.getType();
                
                if( type == EventType.PRE_CHILD_ELEMENT_ADD )
                {
                    XmlElement propElement = root.getChildElement( finalElementName, false );
                    
                    if( propElement == null )
                    {
                        final String propName = root.getText();
                        
                        if( propName.length() > 0 )
                        {
                            root.setText( null );
                            propElement = root.getChildElement( finalElementName, true );
                            propElement.setText( propName );
                        }
                    }
                }
                else if( type == EventType.POST_CHILD_ELEMENT_REMOVE )
                {
                    final int childElementsCount = root.getChildElements().size();
                    
                    if( childElementsCount == 1 )
                    {
                        final XmlElement propElement = root.getChildElement( finalElementName, false );
                        
                        if( propElement != null )
                        {
                            final String propName = propElement.getText();
                            
                            propElement.remove();
                            
                            if( propName.length() > 0 )
                            {
                                root.setText( propName );
                            }
                        }
                    }
                }
            }
        };
        
        root.addListener( this.listener );
    }

    @Override
    public String read()
    {
        String value = null;
        final XmlElement el = xml( false );
        
        if( el != null )
        {
            final XmlElement propElement = el.getChildElement( this.elementName, false );
            
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
        boolean foundChildContent = false;
        
        for( final XmlElement child : el.getChildElements() )
        {
            if( ! child.getLocalName().equals( this.elementName ) )
            {
                foundChildContent = true;
                break;
            }
        }
        
        if( foundChildContent )
        {
            el.setChildNodeText( this.elementName, value, true );
        }
        else
        {
            el.setText( value );
        }
    }

    @Override
    public void dispose()
    {
        super.dispose();
        xml().removeListener( this.listener );
    }

}
