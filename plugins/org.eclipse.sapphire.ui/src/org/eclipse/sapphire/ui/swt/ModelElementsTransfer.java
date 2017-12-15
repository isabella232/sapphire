/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.util.ReadOnlyListFactory;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ModelElementsTransfer extends ByteArrayTransfer
{
    private static final String TYPE_NAME = "Sapphire.ModelElements";
    private static final int TYPE_ID = registerType( TYPE_NAME );
    
    private final ClassLoader classLoader;

    public ModelElementsTransfer( final ClassLoader classLoader )
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
        final List<IModelElement> elements = (List<IModelElement>) data;

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
    
    private void javaToNative( final List<IModelElement> elements,
                               final DataOutputStream out )
    
        throws IOException
        
    {
        out.writeInt( elements.size() );
        
        for( IModelElement element : elements )
        {
            javaToNative( element, out );
        }
    }
    
    private void javaToNative( final IModelElement element,
                               final DataOutputStream out )
    
        throws IOException
        
    {
        out.writeUTF( element.type().getQualifiedName() );
        
        for( ModelProperty property : element.properties() )
        {
            if( ! property.isReadOnly() )
            {
                if( property instanceof ValueProperty )
                {
                    final String value = element.read( (ValueProperty) property ).getText( false );
                    
                    if( value != null )
                    {
                        out.writeByte( 1 );
                        out.writeUTF( property.getName() );
                        out.writeUTF( value );
                    }
                }
                else if( property instanceof ImpliedElementProperty )
                {
                    final IModelElement child = element.read( (ImpliedElementProperty) property );
                    
                    out.writeByte( 1 );
                    out.writeUTF( property.getName() );
                    javaToNative( child, out );
                }
                else if( property instanceof ElementProperty )
                {
                    final IModelElement child = element.read( (ElementProperty) property ).element();
                    
                    if( child != null )
                    {
                        out.writeByte( 1 );
                        out.writeUTF( property.getName() );
                        javaToNative( child, out );
                    }
                }
                else if( property instanceof ListProperty )
                {
                    final List<IModelElement> list = element.read( (ListProperty) property );
                    
                    if( ! list.isEmpty() )
                    {
                        out.writeByte( 1 );
                        out.writeUTF( property.getName() );
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
            final ReadOnlyListFactory<IModelElement> elementsListFactory = ReadOnlyListFactory.create();
            final int size = in.readInt();
            
            for( int i = 0; i < size; i++ )
            {
                final String qualifiedTypeName = in.readUTF();
                final ModelElementType type = ModelElementType.read( this.classLoader, qualifiedTypeName );
                final IModelElement element = type.instantiate();
                nativeToJava( in, element );
                elementsListFactory.add( element );
            }
            
            return elementsListFactory.export();
        }
        catch( IOException e )
        {
            return null;
        }
    }
    
    private void nativeToJava( final DataInputStream in,
                               final IModelElement element )
    
        throws IOException
        
    {
        while( in.readByte() != 0 )
        {
            final String propertyName = in.readUTF();
            final ModelProperty property = element.property( propertyName );

            if( property instanceof ValueProperty )
            {
                final String value = in.readUTF();
                element.write( (ValueProperty) property, value );
            }
            else if( property instanceof ImpliedElementProperty )
            {
                in.readUTF(); // qualified type name
                final IModelElement child = element.read( (ImpliedElementProperty) property );
                nativeToJava( in, child );
            }
            else if( property instanceof ElementProperty )
            {
                final String qualifiedTypeName = in.readUTF();
                final ModelElementType type = ModelElementType.read( this.classLoader, qualifiedTypeName );
                final IModelElement child = element.read( (ElementProperty) property ).element( true, type );
                nativeToJava( in, child );
            }
            else if( property instanceof ListProperty )
            {
                final ModelElementList<?> list = element.read( (ListProperty) property );
                final int size = in.readInt();
                
                for( int i = 0; i < size; i++ )
                {
                    final String qualifiedTypeName = in.readUTF();
                    final ModelElementType type = ModelElementType.read( this.classLoader, qualifiedTypeName );
                    final IModelElement child = list.insert( type );
                    nativeToJava( in, child );
                }
            }
        }
    }
    
}
