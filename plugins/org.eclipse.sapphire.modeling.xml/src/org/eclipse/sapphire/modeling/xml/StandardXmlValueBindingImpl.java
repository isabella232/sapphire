/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling.xml;

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlValueBinding;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class StandardXmlValueBindingImpl extends XmlValueBindingImpl
{
    @Text( "{0}.{1} : {2}" )
    private static LocalizableText failure; 
    
    static
    {
        LocalizableText.init( StandardXmlValueBindingImpl.class );
    }

    protected XmlPath path;
    protected boolean treatExistanceAsValue;
    protected String valueWhenPresent;
    protected String valueWhenNotPresent;
    protected boolean removeNodeOnSetIfNull;
    
    @Override
    public final void init( final Property property )
    {
        super.init( property );
        
        try
        {
            initBindingMetadata();
        }
        catch( Exception e )
        {
            final String msg = failure.format( property.element().type().getSimpleName(), property.name(), e.getMessage() );
            throw new RuntimeException( msg, e );
        }
    }
    
    protected void initBindingMetadata()
    {
        final Value<?> property = (Value<?>) property();
        final ValueProperty pdef = property.definition();
        final XmlNamespaceResolver xmlNamespaceResolver = resource().getXmlNamespaceResolver();
        
        final XmlBinding genericBindingAnnotation = pdef.getAnnotation( XmlBinding.class );
        
        if( genericBindingAnnotation != null )
        {
            this.path = new XmlPath( genericBindingAnnotation.path(), xmlNamespaceResolver );
            this.removeNodeOnSetIfNull = true;
        }
        else
        {
            final XmlValueBinding bindingAnnotation = pdef.getAnnotation( XmlValueBinding.class );
            
            if( bindingAnnotation != null )
            {
                this.path = new XmlPath( bindingAnnotation.path(), xmlNamespaceResolver );
                this.removeNodeOnSetIfNull = bindingAnnotation.removeNodeOnSetIfNull();
                
                if( bindingAnnotation.mapExistanceToValue().length() > 0 )
                {
                    this.treatExistanceAsValue = true;
                    
                    final String directive = bindingAnnotation.mapExistanceToValue();
                    StringBuilder buf = new StringBuilder();
                    boolean escapeNextChar = false;
                    int separatorCount = 0;
                    
                    for( int i = 0, n = directive.length(); i < n; i++ )
                    {
                        final char ch = directive.charAt( i );
                        
                        if( escapeNextChar )
                        {
                            buf.append( ch );
                            escapeNextChar = false;
                        }
                        else if( ch == '\\' )
                        {
                            escapeNextChar = true;
                        }
                        else if( ch == ';' )
                        {
                            separatorCount++;
                            
                            this.valueWhenPresent = buf.toString();
                            buf = new StringBuilder();
                        }
                        else
                        {
                            buf.append( ch );
                        }
                    }
                    
                    if( separatorCount == 0 )
                    {
                        this.valueWhenPresent = buf.toString();
                        
                        // todo: report an error
                    }
                    else
                    {
                        this.valueWhenNotPresent = buf.toString();
                        
                        if( separatorCount > 1 )
                        {
                            // todo: report an error;
                        }
                    }
                }
            }
            else
            {
                this.path = new XmlPath( property.name(), xmlNamespaceResolver );
            }
        }
    }

    @Override
    public String read()
    {
        String value = null;
        
        final XmlElement element = xml( false );
        
        if( element != null )
        {
            if( this.treatExistanceAsValue )
            {
                final boolean exists = ( element.getChildNode( this.path, false ) != null );
                value = ( exists ? this.valueWhenPresent : this.valueWhenNotPresent );
            }
            else if( this.path == null )
            {
                value = element.getText();
            }
            else
            {
                value = element.getChildNodeText( this.path );
            }
        }
        
        return value;
    }

    @Override
    public void write( final String value )
    {
        if( this.treatExistanceAsValue )
        {
            final boolean nodeShouldBePresent = this.valueWhenPresent.equals( value );
            
            if( nodeShouldBePresent )
            {
                xml( true ).getChildNode( this.path, true );
            }
            else
            {
                final XmlElement element = xml( false );
                
                if( element != null )
                {
                    element.removeChildNode( this.path );
                }
            }
        }
        else if( this.path == null )
        {
            xml( true ).setText( value );
        }
        else
        {
            xml( true ).setChildNodeText( this.path, value, this.removeNodeOnSetIfNull );
        }
    }

    @Override
    public XmlNode getXmlNode()
    {
        final XmlElement element = xml( false );
        
        if( element != null )
        {
            return element.getChildNode( this.path, false );
        }
        
        return null;
    }
    
}
