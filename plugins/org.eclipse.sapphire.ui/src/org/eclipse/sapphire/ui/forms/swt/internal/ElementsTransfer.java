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

package org.eclipse.sapphire.ui.forms.swt.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.util.ListFactory;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ElementsTransfer extends ByteArrayTransfer
{
    private static final String TYPE_NAME = "Sapphire.Elements";
    private static final int TYPE_ID = registerType( TYPE_NAME );
    
    private final ClassLoader classLoader;

    public ElementsTransfer( final ClassLoader classLoader )
    {
        this.classLoader = classLoader;
    }

    @Override
    protected int[] getTypeIds()
    {
        return new int[] { TYPE_ID };
    }
    
    @Override
    protected String[] getTypeNames()
    {
        return new String[] { TYPE_NAME };
    }

    @Override
    @SuppressWarnings( "unchecked" )
    
    protected void javaToNative( final Object data, 
                                 final TransferData transferData )
    {
        final List<Element> elements = (List<Element>) data;

        try
        {
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final DataOutputStream out = new DataOutputStream( byteArrayOutputStream );
            
            javaToNative( elements, out );

            out.close();
            byteArrayOutputStream.close();
            
            final byte[] bytes = byteArrayOutputStream.toByteArray();
            super.javaToNative( bytes, transferData );
        }
        catch( IOException e )
        {
            // Send nothing if there are any problems.
        }
    }
    
    private void javaToNative( final List<Element> elements,
                               final DataOutputStream out )
    
        throws IOException
        
    {
        out.writeInt( elements.size() );
        
        for( Element element : elements )
        {
            javaToNative( element, out );
        }
    }
    
    private void javaToNative( final Element element,
                               final DataOutputStream out )
    
        throws IOException
        
    {
        out.writeUTF( element.type().getQualifiedName() );
        
        for( Property property : element.properties() )
        {
            if( ! property.definition().isReadOnly() )
            {
                if( property instanceof Value<?> )
                {
                    final String value = ( (Value<?>) property ).text( false );
                    
                    if( value != null )
                    {
                        out.writeByte( 1 );
                        out.writeUTF( property.name() );
                        out.writeUTF( value );
                    }
                }
                else if( property instanceof ElementHandle<?> )
                {
                    final Element child = ( (ElementHandle<?>) property ).content();
                    
                    if( child != null )
                    {
                        out.writeByte( 1 );
                        out.writeUTF( property.name() );
                        javaToNative( child, out );
                    }
                }
                else if( property.definition() instanceof ListProperty )
                {
                    final List<Element> list = element.property( (ListProperty) property.definition() );
                    
                    if( ! list.isEmpty() )
                    {
                        out.writeByte( 1 );
                        out.writeUTF( property.name() );
                        javaToNative( list, out );
                    }
                }
            }
        }
        
        out.writeByte( 0 );
    }

    @Override
    protected Object nativeToJava( final TransferData transferData )
    {
        final byte[] bytes = (byte[]) super.nativeToJava( transferData );
        
        if( bytes == null )
        {
            return null;
        }
        
        final DataInputStream in = new DataInputStream( new ByteArrayInputStream( bytes ) );
        
        try
        {
            final ListFactory<Element> elementsListFactory = ListFactory.start();
            final int size = in.readInt();
            
            for( int i = 0; i < size; i++ )
            {
                final String qualifiedTypeName = in.readUTF();
                final ElementType type = ElementType.read( this.classLoader, qualifiedTypeName );
                final Element element = type.instantiate();
                nativeToJava( in, element );
                elementsListFactory.add( element );
            }
            
            return elementsListFactory.result();
        }
        catch( IOException e )
        {
            return null;
        }
    }
    
    private void nativeToJava( final DataInputStream in,
                               final Element element )
    
        throws IOException
        
    {
        while( in.readByte() != 0 )
        {
            final String propertyName = in.readUTF();
            final Property property = element.property( propertyName );

            if( property != null )
            {
                if( property instanceof Value )
                {
                    final String value = in.readUTF();
                    ( (Value<?>) property ).write( value );
                }
                else if( property instanceof ElementHandle )
                {
                    final String qualifiedTypeName = in.readUTF();
                    final ElementType type = ElementType.read( this.classLoader, qualifiedTypeName );
                    final Element child = ( (ElementHandle<?>) property ).content( true, type );
                    nativeToJava( in, child );
                }
                else if( property instanceof ElementList )
                {
                    final ElementList<?> list = (ElementList<?>) property;
                    final int size = in.readInt();
                    
                    for( int i = 0; i < size; i++ )
                    {
                        final String qualifiedTypeName = in.readUTF();
                        final ElementType type = ElementType.read( this.classLoader, qualifiedTypeName );
                        final Element child = list.insert( type );
                        nativeToJava( in, child );
                    }
                }
            }
        }
    }
    
}
