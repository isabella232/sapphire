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

package org.eclipse.sapphire.internal;

import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_5;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.sapphire.Disposable;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementImpl;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Observable;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.ReferenceValue;
import org.eclipse.sapphire.Resource;
import org.eclipse.sapphire.Transient;
import org.eclipse.sapphire.TransientProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.Derived;
import org.eclipse.sapphire.modeling.annotations.ReadOnly;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ElementCompiler
{
    private final ElementType type;
    private final Class<?> typeInterfaceClass;
    private final String typeInterfaceClassInternalName;
    private final String typeImplClassInternalName;
    private final Set<Method> implementedMethods;
    
    public ElementCompiler( final ElementType type )
    {
        this.type = type;
        this.typeInterfaceClass = this.type.getModelElementClass();
        this.typeInterfaceClassInternalName = Type.getInternalName( this.typeInterfaceClass );
        this.typeImplClassInternalName = this.typeInterfaceClassInternalName + "$Impl";
        this.implementedMethods = new HashSet<Method>();
    }
    
    public byte[] compile()
    {
        ClassWriter cw = new ClassWriter( COMPUTE_MAXS );

        cw.visit( V1_5, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, this.typeImplClassInternalName, null, Type.getInternalName( ElementImpl.class ), new String[] { this.typeInterfaceClassInternalName } );
        
        processConstructor( cw );
        
        for( PropertyDef property : this.type.properties() )
        {
            if( property instanceof ValueProperty )
            {
                processValueProperty( cw, (ValueProperty) property );
            }
            else if( property instanceof TransientProperty )
            {
                processTransientProperty( cw, (TransientProperty) property );
            }
            else if( property instanceof ListProperty )
            {
                processListProperty( cw, (ListProperty) property );
            }
            else if( property instanceof ImpliedElementProperty )
            {
                processImpliedElementProperty( cw, (ImpliedElementProperty) property );
            }
            else if( property instanceof ElementProperty )
            {
                processElementProperty( cw, (ElementProperty) property );
            }
            else
            {
                throw new IllegalStateException( property.getClass().getName() );
            }
        }
        
        processDelegatedMethods( cw );
        processUnimplementedMethods( cw );
        
        return cw.toByteArray();
    }
    
    private void processConstructor( final ClassWriter cw )
    {
        final MethodVisitor mv = cw.visitMethod
        (
            ACC_PUBLIC, 
            "<init>", 
            Type.getMethodDescriptor( Type.VOID_TYPE, new Type[] { Type.getType( Property.class ), Type.getType( Resource.class ) } ),
            null,
            null
        );
        
        mv.visitCode();
        
        mv.visitVarInsn( ALOAD, 0 );
        
        mv.visitFieldInsn
        (
            GETSTATIC, 
            this.typeInterfaceClassInternalName, 
            "TYPE",
            Type.getDescriptor( ElementType.class )
        );
        
        mv.visitVarInsn( ALOAD, 1 );
        mv.visitVarInsn( ALOAD, 2 );
        
        mv.visitMethodInsn
        (
            INVOKESPECIAL, 
            Type.getInternalName( ElementImpl.class ), 
            "<init>", 
            Type.getMethodDescriptor( Type.VOID_TYPE, new Type[] { Type.getType( ElementType.class ), Type.getType( Property.class ), Type.getType( Resource.class ) } )
        );
        
        mv.visitInsn( RETURN );
        
        mv.visitMaxs( 0, 0 );
        mv.visitEnd();
    }
    
    private void processValueProperty( final ClassWriter cw,
                                       final ValueProperty property )
    {
        final String propertyFieldName = findPropertyField( property ).getName();
        
        Method getter = findMethod( "get" + property.name() );
        
        if( getter == null )
        {
            getter = findMethod( "is" + property.name() );
        }
        
        if( getter != null )
        {
            this.implementedMethods.add( getter );
            
            final boolean reference = property.hasAnnotation( Reference.class );
            
            final MethodVisitor mv = cw.visitMethod
            (
                ACC_PUBLIC,
                getter.getName(),
                Type.getMethodDescriptor( Type.getType( reference ? ReferenceValue.class : Value.class ), new Type[ 0 ] ),
                null,
                null
            );
            
            mv.visitCode();
            
            mv.visitVarInsn( ALOAD, 0 );
            
            mv.visitFieldInsn
            (
                GETSTATIC, 
                this.typeInterfaceClassInternalName,
                propertyFieldName,
                Type.getDescriptor( ValueProperty.class )
            );
            
            mv.visitMethodInsn
            (
                INVOKEVIRTUAL, 
                this.typeImplClassInternalName,
                "property",
                Type.getMethodDescriptor( Type.getType( Value.class ), new Type[] { Type.getType( ValueProperty.class ) } )
            );
            
            if( reference )
            {
                mv.visitTypeInsn( CHECKCAST, Type.getInternalName( ReferenceValue.class ) );                
            }
            
            mv.visitInsn( ARETURN );
            
            mv.visitMaxs( 0, 0 );
            mv.visitEnd();
        }
        
        if( ! property.hasAnnotation( Derived.class ) && ! property.hasAnnotation( ReadOnly.class ) )
        {
            Method setter = findMethod( "set" + property.name(), String.class );
            
            if( setter != null )
            {
                this.implementedMethods.add( setter );
                
                final MethodVisitor mv = cw.visitMethod
                (
                    ACC_PUBLIC,
                    setter.getName(),
                    Type.getMethodDescriptor( Type.VOID_TYPE, new Type[] { Type.getType( String.class ) } ),
                    null,
                    null
                );
                
                mv.visitCode();
    
                mv.visitVarInsn( ALOAD, 0 );
                
                mv.visitFieldInsn
                (
                    GETSTATIC,
                    this.typeInterfaceClassInternalName,
                    propertyFieldName,
                    Type.getDescriptor( ValueProperty.class )
                );
                
                mv.visitMethodInsn
                (
                    INVOKEVIRTUAL,
                    this.typeImplClassInternalName,
                    "property",
                    Type.getMethodDescriptor( Type.getType( Value.class ), new Type[] { Type.getType( ValueProperty.class ) } )
                );
                
                mv.visitVarInsn( ALOAD, 1 );
                
                mv.visitMethodInsn
                (
                    INVOKEVIRTUAL,
                    Type.getInternalName( Value.class ),
                    "write",
                    Type.getMethodDescriptor( Type.VOID_TYPE, new Type[] { Type.getType( Object.class ) } )
                );
                
                mv.visitInsn( RETURN );
                
                mv.visitMaxs( 0, 0 );
                mv.visitEnd();
            }
            
            final Class<?> propertyTypeClass = property.getTypeClass();
            
            if( propertyTypeClass != String.class )
            {
                Method typedSetter = findMethod( "set" + property.name(), propertyTypeClass );
                
                if( typedSetter != null )
                {
                    this.implementedMethods.add( typedSetter );
                    
                    final MethodVisitor mv = cw.visitMethod
                    (
                        ACC_PUBLIC,
                        typedSetter.getName(),
                        Type.getMethodDescriptor( Type.VOID_TYPE, new Type[] { Type.getType( propertyTypeClass ) } ),
                        null,
                        null
                    );
                    
                    mv.visitCode();
        
                    mv.visitVarInsn( ALOAD, 0 );
                    
                    mv.visitFieldInsn
                    (
                        GETSTATIC,
                        this.typeInterfaceClassInternalName,
                        propertyFieldName,
                        Type.getDescriptor( ValueProperty.class )
                    );
                    
                    mv.visitMethodInsn
                    (
                        INVOKEVIRTUAL,
                        this.typeImplClassInternalName,
                        "property",
                        Type.getMethodDescriptor( Type.getType( Value.class ), new Type[] { Type.getType( ValueProperty.class ) } )
                    );
                    
                    mv.visitVarInsn( ALOAD, 1 );
                    
                    mv.visitMethodInsn
                    (
                        INVOKEVIRTUAL,
                        Type.getInternalName( Value.class ),
                        "write",
                        Type.getMethodDescriptor( Type.VOID_TYPE, new Type[] { Type.getType( Object.class ) } )
                    );
                    
                    mv.visitInsn( RETURN );
                    
                    mv.visitMaxs( 0, 0 );
                    mv.visitEnd();
                }
            }
        }
    }
    
    private void processTransientProperty( final ClassWriter cw,
                                           final TransientProperty property )
    {
        final String propertyFieldName = findPropertyField( property ).getName();
        final Class<?> propertyTypeClass = property.getTypeClass();
        
        final Method getter = findMethod( "get" + property.name() );
        
        if( getter != null )
        {
            this.implementedMethods.add( getter );
            
            final MethodVisitor mv = cw.visitMethod
            (
                ACC_PUBLIC,
                getter.getName(),
                Type.getMethodDescriptor( Type.getType( Transient.class ), new Type[ 0 ] ),
                null,
                null
            );
            
            mv.visitCode();
            
            mv.visitVarInsn( ALOAD, 0 );
            
            mv.visitFieldInsn
            (
                GETSTATIC, 
                this.typeInterfaceClassInternalName,
                propertyFieldName,
                Type.getDescriptor( TransientProperty.class )
            );
            
            mv.visitMethodInsn
            (
                INVOKEVIRTUAL, 
                this.typeImplClassInternalName,
                "property",
                Type.getMethodDescriptor( Type.getType( Transient.class ), new Type[] { Type.getType( TransientProperty.class ) } )
            );
            
            mv.visitInsn( ARETURN );
            
            mv.visitMaxs( 0, 0 );
            mv.visitEnd();
        }
        
        final Method setter = findMethod( "set" + property.name(), propertyTypeClass );
        
        if( setter != null )
        {
            this.implementedMethods.add( setter );
            
            final MethodVisitor mv = cw.visitMethod
            (
                ACC_PUBLIC,
                setter.getName(),
                Type.getMethodDescriptor( Type.VOID_TYPE, new Type[] { Type.getType( propertyTypeClass ) } ),
                null,
                null
            );
            
            mv.visitCode();

            mv.visitVarInsn( ALOAD, 0 );
            
            mv.visitFieldInsn
            (
                GETSTATIC,
                this.typeInterfaceClassInternalName,
                propertyFieldName,
                Type.getDescriptor( TransientProperty.class )
            );
            
            mv.visitMethodInsn
            (
                INVOKEVIRTUAL,
                this.typeImplClassInternalName,
                "property",
                Type.getMethodDescriptor( Type.getType( Transient.class ), new Type[] { Type.getType( TransientProperty.class ) } )
            );
            
            mv.visitVarInsn( ALOAD, 1 );
            
            mv.visitMethodInsn
            (
                INVOKEVIRTUAL,
                Type.getInternalName( Transient.class ),
                "write",
                Type.getMethodDescriptor( Type.VOID_TYPE, new Type[] { Type.getType( Object.class ) } )
            );
            
            mv.visitInsn( RETURN );
            
            mv.visitMaxs( 0, 0 );
            mv.visitEnd();
        }
    }

    private void processListProperty( final ClassWriter cw,
                                      final ListProperty property )
    {
        final String propertyFieldName = findPropertyField( property ).getName();
        
        final Method getter = findMethod( "get" + property.name() );
        
        if( getter != null )
        {
            this.implementedMethods.add( getter );
            
            final MethodVisitor mv = cw.visitMethod
            (
                ACC_PUBLIC,
                getter.getName(),
                Type.getMethodDescriptor( Type.getType( ElementList.class ), new Type[ 0 ] ),
                null,
                null
            );
            
            mv.visitCode();
            
            mv.visitVarInsn( ALOAD, 0 );
            
            mv.visitFieldInsn
            (
                GETSTATIC, 
                this.typeInterfaceClassInternalName,
                propertyFieldName,
                Type.getDescriptor( ListProperty.class )
            );
            
            mv.visitMethodInsn
            (
                INVOKEVIRTUAL, 
                this.typeImplClassInternalName,
                "property",
                Type.getMethodDescriptor( Type.getType( ElementList.class ), new Type[] { Type.getType( ListProperty.class ) } )
            );
            
            mv.visitInsn( ARETURN );
            
            mv.visitMaxs( 0, 0 );
            mv.visitEnd();
        }
    }

    private void processElementProperty( final ClassWriter cw,
                                         final ElementProperty property )
    {
        final String propertyFieldName = findPropertyField( property ).getName();
        
        final Method getter = findMethod( "get" + property.name() );
        
        if( getter != null )
        {
            this.implementedMethods.add( getter );
            
            final MethodVisitor mv = cw.visitMethod
            (
                ACC_PUBLIC,
                getter.getName(),
                Type.getMethodDescriptor( Type.getType( ElementHandle.class ), new Type[ 0 ] ),
                null,
                null
            );
            
            mv.visitCode();
            
            mv.visitVarInsn( ALOAD, 0 );
            
            mv.visitFieldInsn
            (
                GETSTATIC, 
                this.typeInterfaceClassInternalName,
                propertyFieldName,
                Type.getDescriptor( ElementProperty.class )
            );
            
            mv.visitMethodInsn
            (
                INVOKEVIRTUAL, 
                this.typeImplClassInternalName,
                "property",
                Type.getMethodDescriptor( Type.getType( ElementHandle.class ), new Type[] { Type.getType( ElementProperty.class ) } )
            );
            
            mv.visitInsn( ARETURN );
            
            mv.visitMaxs( 0, 0 );
            mv.visitEnd();
        }
    }

    private void processImpliedElementProperty( final ClassWriter cw,
                                                final ImpliedElementProperty property )
    {
        final String propertyFieldName = findPropertyField( property ).getName();
        final Class<?> propertyTypeClass = property.getTypeClass();
        
        final Method getter = findMethod( "get" + property.name() );
        
        if( getter != null )
        {
            this.implementedMethods.add( getter );
            
            final MethodVisitor mv = cw.visitMethod
            (
                ACC_PUBLIC,
                getter.getName(),
                Type.getMethodDescriptor( Type.getType( propertyTypeClass ), new Type[ 0 ] ),
                null,
                null
            );
            
            mv.visitCode();
            
            mv.visitVarInsn( ALOAD, 0 );
            
            mv.visitFieldInsn
            (
                GETSTATIC, 
                this.typeInterfaceClassInternalName,
                propertyFieldName,
                Type.getDescriptor( ImpliedElementProperty.class )
            );
            
            mv.visitMethodInsn
            (
                INVOKEVIRTUAL, 
                this.typeImplClassInternalName,
                "property",
                Type.getMethodDescriptor( Type.getType( ElementHandle.class ), new Type[] { Type.getType( ElementProperty.class ) } )
            );
            
            mv.visitMethodInsn
            (
                INVOKEVIRTUAL,
                Type.getInternalName( ElementHandle.class ),
                "content",
                Type.getMethodDescriptor( Type.getType( Element.class ), new Type[ 0 ] )
            );
            
            mv.visitTypeInsn( CHECKCAST, Type.getInternalName( propertyTypeClass ) );
            
            mv.visitInsn( ARETURN );
            
            mv.visitMaxs( 0, 0 );
            mv.visitEnd();
        }
    }
    
    private void processDelegatedMethods( final ClassWriter cw )
    {
        for( Method method : this.typeInterfaceClass.getMethods() )
        {
            final DelegateImplementation delegateImplementationAnnotation = method.getAnnotation( DelegateImplementation.class );
            
            if( ! this.implementedMethods.contains( method ) && delegateImplementationAnnotation != null )
            {
                this.implementedMethods.add( method );
                
                final Class<?>[] exceptionClasses = method.getExceptionTypes();
                final String[] exceptionTypeNames = new String[ exceptionClasses.length ];
                
                for( int i = 0, n = exceptionClasses.length; i < n; i++ )
                {
                    exceptionTypeNames[ i ] = Type.getInternalName( exceptionClasses[ i ] );
                }
                
                final MethodVisitor mv = cw.visitMethod
                (
                    ACC_PUBLIC,
                    method.getName(),
                    Type.getMethodDescriptor( method ),
                    null,
                    exceptionTypeNames
                );
                
                mv.visitCode();
                
                mv.visitVarInsn( ALOAD, 0 );
                
                mv.visitMethodInsn
                (
                    INVOKEVIRTUAL,
                    this.typeImplClassInternalName,
                    "assertNotDisposed",
                    "()V"
                );
                
                final Type[] methodParameterTypes = Type.getArgumentTypes( method );
                final Type[] delegateParameterTypes = new Type[ methodParameterTypes.length + 1 ];

                mv.visitVarInsn( ALOAD, 0 );
                delegateParameterTypes[ 0 ] = Type.getType( method.getDeclaringClass() );
                
                for( int i = 0, j = 1, n = methodParameterTypes.length; i < n; i++, j++ )
                {
                    final Type methodParameterType = methodParameterTypes[ i ];
                    mv.visitVarInsn( methodParameterType.getOpcode( ILOAD ), j );
                    delegateParameterTypes[ j ] = methodParameterType;
                }
                
                mv.visitMethodInsn
                (
                    INVOKESTATIC,
                    Type.getInternalName( delegateImplementationAnnotation.value() ),
                    method.getName(),
                    Type.getMethodDescriptor( Type.getReturnType( method ), delegateParameterTypes )
                );
                
                mv.visitInsn( Type.getReturnType( method ).getOpcode( IRETURN ) );

                mv.visitMaxs( 0, 0 );
                mv.visitEnd();
            }
        }
    }

    private void processUnimplementedMethods( final ClassWriter cw )
    {
        for( Method method : this.typeInterfaceClass.getMethods() )
        {
            final Class<?> cl = method.getDeclaringClass();
                    
            if( ! this.implementedMethods.contains( method ) && cl != Element.class && cl != Observable.class && cl != Disposable.class && cl != Object.class )
            {
                final MethodVisitor mv = cw.visitMethod
                (
                    ACC_PUBLIC,
                    method.getName(),
                    Type.getMethodDescriptor( method ),
                    null,
                    null
                );
                
                mv.visitCode();
                
                mv.visitTypeInsn( NEW, Type.getInternalName( UnsupportedOperationException.class ) );
                mv.visitInsn( DUP );
                mv.visitMethodInsn( INVOKESPECIAL, Type.getInternalName( UnsupportedOperationException.class ), "<init>", "()V");
                mv.visitInsn( ATHROW );

                mv.visitMaxs( 0, 0 );
                mv.visitEnd();
            }
        }
    }

    private Method findMethod( final String methodName,
                               final Class<?>... paramTypes )
    {
        for( Method method : this.typeInterfaceClass.getMethods() )
        {
            if( method.getName().equalsIgnoreCase( methodName ) )
            {
                final Class<?>[] methodParamTypes = method.getParameterTypes();
                
                if( methodParamTypes.length == paramTypes.length )
                {
                    boolean paramsMatch = true;
                    
                    for( int i = 0, n = paramTypes.length; i < n; i++ )
                    {
                        if( methodParamTypes[ i ] != paramTypes[ i ] )
                        {
                            paramsMatch = false;
                            break;
                        }
                    }
                    
                    if( paramsMatch )
                    {
                        return method;
                    }
                }
            }
        }
        
        return null;
    }
    
    private Field findPropertyField( final PropertyDef property )
    {
        for( Field field : this.typeInterfaceClass.getFields() )
        {
            try
            {
                if( field.get( null ) == property )
                {
                    return field;
                }
            }
            catch( IllegalAccessException e )
            {
                throw new IllegalStateException( e );
            }
        }
        
        return null;
    }

}
