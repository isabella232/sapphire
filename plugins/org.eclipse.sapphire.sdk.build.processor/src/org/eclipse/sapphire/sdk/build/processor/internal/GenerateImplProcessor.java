/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.sdk.build.processor.internal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelParticle;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ListBindingImpl;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElement;
import org.eclipse.sapphire.modeling.ModelElementHandle;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ReferenceValue;
import org.eclipse.sapphire.modeling.Resource;
import org.eclipse.sapphire.modeling.Transient;
import org.eclipse.sapphire.modeling.TransientProperty;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.Derived;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.ReadOnly;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.util.MiscUtil;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.sdk.build.processor.internal.util.AccessModifier;
import org.eclipse.sapphire.sdk.build.processor.internal.util.Body;
import org.eclipse.sapphire.sdk.build.processor.internal.util.ClassModel;
import org.eclipse.sapphire.sdk.build.processor.internal.util.FieldModel;
import org.eclipse.sapphire.sdk.build.processor.internal.util.IndentingPrintWriter;
import org.eclipse.sapphire.sdk.build.processor.internal.util.MethodModel;
import org.eclipse.sapphire.sdk.build.processor.internal.util.MethodParameterModel;
import org.eclipse.sapphire.sdk.build.processor.internal.util.TypeReference;
import org.eclipse.sapphire.sdk.build.processor.internal.util.WildcardTypeReference;
import org.eclipse.sapphire.services.DerivedValueService;
import org.eclipse.sapphire.services.ValueNormalizationService;
import org.eclipse.sapphire.services.ValueSerializationMasterService;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Messager;
import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.InterfaceDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.TypeParameterDeclaration;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.MirroredTypeException;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.VoidType;
import com.sun.mirror.util.SourcePosition;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class GenerateImplProcessor extends SapphireAnnotationsProcessor
{
    private static final String DATA_READ_METHOD = "read.method";
    private static final String DATA_WRITE_VALUE_METHOD = "write.value.method";
    private static final String DATA_WRITE_TRANSIENT_METHOD = "write.transient.method";
    private static final String DATA_REFRESH_METHOD = "refresh.method";
    private static final String DATA_HAS_CONTENTS = "has.contents";
    
    @Override
    public void process( final AnnotationProcessorEnvironment env, 
                         final Declaration annotatedEntity,
                         final AnnotationMirror annotation ) 
    {
        try
        {
            if( annotation == null || annotatedEntity == null ) 
            {
                return;
            }
            
            if( ! ( annotatedEntity instanceof InterfaceDeclaration ) ) 
            {
                return;
            }
            
            final InterfaceDeclaration interfaceDeclaration = (InterfaceDeclaration) annotatedEntity;
            final ClassModel implClassModel = new ClassModel();
            
            process( env.getMessager(), implClassModel, interfaceDeclaration );
            
            if( ! implClassModel.isInvalid() )
            {
                final PrintWriter pw = env.getFiler().createSourceFile( implClassModel.getName().getQualifiedName() );
                
                try
                {
                    implClassModel.write( new IndentingPrintWriter( pw ) );
                }
                finally
                {
                    pw.close();
                }
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
    
    protected static boolean isInstanceOf( final TypeMirror type,
                                           final String interfaceOrClass )
    {
        if( type instanceof DeclaredType )
        {
            final DeclaredType declaredType = (DeclaredType) type;
            return isInstanceOf( declaredType.getDeclaration(), interfaceOrClass );
        }
        
        return false;
    }

    protected static boolean isInstanceOf( final TypeDeclaration type,
                                           final String interfaceOrClass )
    {
        if( type != null )
        {
            if( type.getQualifiedName().equals( interfaceOrClass ) )
            {
                return true;
            }
            
            if( type instanceof ClassDeclaration )
            {
                final ClassType superClassType = ( (ClassDeclaration) type ).getSuperclass();
                
                if( superClassType != null )
                {
                    if( isInstanceOf( superClassType.getDeclaration(), interfaceOrClass ) )
                    {
                        return true;
                    }
                }
            }
            
            for( InterfaceType superInterface : type.getSuperinterfaces() )
            {
                if( isInstanceOf( superInterface, interfaceOrClass ) )
                {
                    return true;
                }
            }
        }
        
        return false;
    }

    private void process( final Messager messager,
                          final ClassModel elImplClass,
                          final InterfaceDeclaration elInterface )
    {
        final GenerateImpl generateImplAnnotation = elInterface.getAnnotation( GenerateImpl.class );
        final String implClassQualifiedName = ModelElementType.getImplClassName( getQualifiedName( elInterface ), generateImplAnnotation );
        
        elImplClass.setName( new TypeReference( implClassQualifiedName ) );
        elImplClass.addInterface( new TypeReference( elInterface.getQualifiedName() ) );
        elImplClass.setBaseClass( new TypeReference( ModelElement.class.getName() ) );
        
        final MethodModel c1 = elImplClass.addConstructor();
        
        c1.addParameter( new MethodParameterModel( "parent", IModelParticle.class ) );
        c1.addParameter( new MethodParameterModel( "parentProperty", ModelProperty.class ) );
        c1.addParameter( new MethodParameterModel( "resource", Resource.class ) );
        c1.getBody().append( "super( TYPE, parent, parentProperty, resource );" );

        final MethodModel c2 = elImplClass.addConstructor();
        
        c2.addParameter( new MethodParameterModel( "resource", Resource.class ) );
        c2.getBody().append( "super( TYPE, null, null, resource );" );
        
        final Map<String,PropertyFieldDeclaration> propFields = new TreeMap<String,PropertyFieldDeclaration>();
        
        final Visitor<FieldDeclaration> fieldsVisitor = new Visitor<FieldDeclaration>()
        {
            public void visit( final FieldDeclaration field )
            {
                final String fieldName = field.getSimpleName();
                
                if( fieldName != null && fieldName.startsWith( "PROP_" ) )
                {
                    PropertyFieldDeclaration propFieldDeclaration = propFields.get( fieldName );
                    
                    if( propFieldDeclaration == null )
                    {
                        propFieldDeclaration = new PropertyFieldDeclaration();
                        propFieldDeclaration.name = fieldName;
                        propFieldDeclaration.propertyName = preparePropName( fieldName );
                        propFields.put( fieldName, propFieldDeclaration );
                    }
                    
                    propFieldDeclaration.declarations.addFirst( field );
                }
            }
        };

        visitAllFields( elInterface, fieldsVisitor );
        
        for( PropertyFieldDeclaration field : propFields.values() )
        {
            if( isInstanceOf( field.getType(), ValueProperty.class.getName() ) )
            {
                processValueProperty( messager, elImplClass, elInterface, field );
            }
            else if( isInstanceOf( field.getType(), ElementProperty.class.getName() ) )
            {
                processElementProperty( messager, elImplClass, elInterface, field );
            }
            else if( isInstanceOf( field.getType(), ListProperty.class.getName() ) )
            {
                processListProperty( messager, elImplClass, elInterface, field );
            }
            else if( isInstanceOf( field.getType(), TransientProperty.class.getName() ) )
            {
                processTransientProperty( messager, elImplClass, elInterface, field );
            }
        }
        
        final Visitor<MethodDeclaration> methodsVisitor = new Visitor<MethodDeclaration>()
        {
            public void visit( final MethodDeclaration method )
            {
                final DelegateImplementation delegateImplementationAnnotation
                    = method.getAnnotation( DelegateImplementation.class );
                
                if( delegateImplementationAnnotation != null )
                {
                    final String methodName = method.getSimpleName();
                    final TypeReference methodReturnType = toTypeReference( method.getReturnType() );
                    final List<TypeReference> methodParametersList = new ArrayList<TypeReference>();
                    final Map<String,TypeReference> methodParametersMap = new LinkedHashMap<String,TypeReference>();
                    
                    for( ParameterDeclaration param : method.getParameters() )
                    {
                        final TypeReference type = toTypeReference( param.getType() );
                        methodParametersList.add( type );
                        methodParametersMap.put( param.getSimpleName(), type );
                    }
                    
                    if( ! elImplClass.hasMethod( methodName, methodParametersList ) )
                    {
                        final MethodModel m = elImplClass.addMethod( methodName );
                        m.setReturnType( methodReturnType );
                        
                        for( Map.Entry<String,TypeReference> param : methodParametersMap.entrySet() )
                        {
                            final MethodParameterModel p = new MethodParameterModel();
                            p.setName( param.getKey() );
                            p.setType( param.getValue() );
                            m.addParameter( p );
                        }
                        
                        TypeReference delegate = null;
                        
                        try
                        {
                            delegateImplementationAnnotation.value();
                        }
                        catch( MirroredTypeException e )
                        {
                            final ClassDeclaration typeMirror = ( (ClassType) e.getTypeMirror() ).getDeclaration();
                            delegate = new TypeReference( typeMirror.getQualifiedName() );
                        }
                        
                        final Body mb = m.getBody();
                        
                        mb.append( "synchronized( root() )" );
                        mb.openBlock();
                        
                        final StringBuilder buf = new StringBuilder();
                        
                        if( m.getReturnType() != TypeReference.VOID_TYPE )
                        {
                            buf.append( "return " );
                        }
                        
                        buf.append( delegate.getSimpleName() ).append( '.' ).append( m.getName() ).append( "( this" );
                        
                        for( MethodParameterModel param : m.getParameters() )
                        {
                            buf.append( ", " );
                            buf.append( param.getName() );
                        }
                        
                        buf.append( " );" );
    
                        mb.append( buf.toString() );
                        mb.closeBlock();
                        
                        elImplClass.addImport( delegate );
                    }
                }
            }
        };
        
        visitAllMethods( elInterface, methodsVisitor );
        
        final MethodModel mRefresh = getRefreshMethod( elImplClass, false );
        
        if( mRefresh != null )
        {
            elImplClass.removeMethod( mRefresh );
            elImplClass.addMethod( mRefresh );
            mRefresh.getBody().closeBlock();
        }
        
        final MethodModel mRead = getReadMethod( elImplClass, false );
        
        if( mRead != null )
        {
            elImplClass.removeMethod( mRead );
            elImplClass.addMethod( mRead );
            
            mRead.getBody().appendEmptyLine();
            mRead.getBody().append( "return super.read( property );" );
        }

        final MethodModel mWriteValue = getValueWriteMethod( elImplClass, false );
        
        if( mWriteValue != null )
        {
            elImplClass.removeMethod( mWriteValue );
            elImplClass.addMethod( mWriteValue );
            
            mWriteValue.getBody().appendEmptyLine();
            mWriteValue.getBody().append( "super.write( property, value );" );
        }

        final MethodModel mWriteTransient = getTransientWriteMethod( elImplClass, false );
        
        if( mWriteTransient != null )
        {
            elImplClass.removeMethod( mWriteTransient );
            elImplClass.addMethod( mWriteTransient );
            
            mWriteTransient.getBody().appendEmptyLine();
            mWriteTransient.getBody().append( "super.write( property, object );" );
        }
    }

    private void processValueProperty( final Messager messager,
                                       final ClassModel implClassModel,
                                       final InterfaceDeclaration interfaceDeclaration,
                                       final PropertyFieldDeclaration propField )
    {
        try
        {
            processValuePropertyInternal( messager, implClassModel, interfaceDeclaration, propField );
        }
        catch( AbortException e )
        {
            implClassModel.markInvalid();
        }
        catch( RuntimeException e )
        {
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter( sw );
            
            final InterfaceDeclaration modelElementInterface = propField.getDeclaringType();
        
            pw.println();
            pw.println( "RuntimeException encountered during processValueProperty() method call." );
            pw.println();
            pw.println( "interfaceDeclaration : " + ( modelElementInterface == null ? "null" : modelElementInterface.getQualifiedName() ) );
            pw.println( "propField : " + propField.name );
            pw.println();
            e.printStackTrace( pw );
            pw.println();
            pw.flush();
            
            System.err.println( sw.getBuffer().toString() );
            
            throw e;
        }
    }

    private void processValuePropertyInternal( final Messager messager,
                                               final ClassModel implClassModel,
                                               final InterfaceDeclaration interfaceDeclaration,
                                               final PropertyFieldDeclaration propField )
    {
        // Determine the property type.

        TypeReference baseType = null; 
        TypeReference wrapperType = null;        
        
        final Type typeAnnotation = propField.getAnnotation( Type.class );
        
        if( typeAnnotation == null )
        {
            baseType = new TypeReference( String.class );
        }
        else
        {
            try
            {
                typeAnnotation.base();
            }
            catch( MirroredTypeException e )
            {
                baseType = toTypeReference( e.getTypeMirror() );
            }
        }
        
        final Reference referenceAnnotation = propField.getAnnotation( Reference.class );
        
        if( referenceAnnotation != null )
        {
            try
            {
                referenceAnnotation.target();
            }
            catch( MirroredTypeException e )
            {
                final TypeMirror mirror = e.getTypeMirror();
                TypeReference targetType = toTypeReference( mirror ); 
                
                final Collection<TypeParameterDeclaration> typeParams;

                if( mirror instanceof TypeDeclaration )
                {
                    typeParams = ( (TypeDeclaration) mirror ).getFormalTypeParameters();
                }
                else
                {
                    typeParams = ( (DeclaredType) mirror ).getDeclaration().getFormalTypeParameters();
                }
                
                if( ! typeParams.isEmpty() )
                {
                    final TypeReference[] params = new TypeReference[ typeParams.size() ];
                    Arrays.fill( params, WildcardTypeReference.INSTANCE );
                    targetType = targetType.parameterize( params );
                }
                
                wrapperType = new TypeReference( ReferenceValue.class ).parameterize( baseType, targetType );
            }
        }
        else
        {
            wrapperType = ( new TypeReference( Value.class ) ).parameterize( baseType );
        }
        
        final boolean hasDerivedValueProviderAnnotation = ( propField.getAnnotation( Derived.class ) != null );
        
        // Determine the getter method name.
        
        final String variableName = propField.propertyName.substring( 0, 1 ).toLowerCase() + propField.propertyName.substring( 1 );
        
        String getterMethodName = null;
        
        final InterfaceDeclaration modelElementInterface = propField.getDeclaringType();
        final String getterAlt1 = "get" + propField.propertyName;
        final String getterAlt2 = "is" + propField.propertyName;

        MethodDeclaration getterMethodInInterface = findMethodDeclaration( modelElementInterface, getterAlt1 );
        
        if( getterMethodInInterface == null )
        {
            getterMethodInInterface = findMethodDeclaration( modelElementInterface, getterAlt2 );
        }
        
        if( getterMethodInInterface == null )
        {
            final String msg = NLS.bind( Resources.unableToFindGetter, modelElementInterface.getSimpleName(), propField.name );
            messager.printError( propField.getSourcePosition(), msg );
            throw new AbortException();
        }
        
        getterMethodName = getterMethodInInterface.getSimpleName();
        propField.setGetterMethodName( getterMethodName );
        
        // Define the field that will hold the cached value of the property.
        
        final FieldModel field = implClassModel.addField();
        field.setName( variableName );
        field.setType( wrapperType );
        
        // Contribute content to the refresh method.
        
        final Body rb = prepareRefreshMethodBlock( implClassModel, propField );

        rb.append( "if( this.#1 != null || force == true )", variableName );
        rb.openBlock();
        rb.append( "final #2 oldValue = this.#1;", variableName, wrapperType.getSimpleName() );
        rb.appendEmptyLine();
        
        if( hasDerivedValueProviderAnnotation )
        {
            rb.append( "final String val = service( #1, DerivedValueService.class ).value();", propField.name );
            implClassModel.addImport( DerivedValueService.class );
        }
        else
        {
            rb.append( "final String val = resource().binding( #1 ).read();", propField.name );
        }
        
        rb.appendEmptyLine();
        
        rb.append( "this.#1 = new #2( this, #3, service( #3, ValueNormalizationService.class ).normalize( #3.encodeKeywords( val ) ) );\n" +
                   "this.#1.init();\n" +
                   "\n" +
                   "final EnablementRefreshResult enablementRefreshResult = refreshPropertyEnablement( #3 );\n" +
                   "\n" +
                   "if( oldValue != null )\n" +
                   "{\n" + 
                   "    if( this.#1.equals( oldValue ) )\n" +
                   "    {\n" + 
                   "        this.#1 = oldValue;\n" + 
                   "    }\n" + 
                   "    \n" +
                   "    if( this.#1 != oldValue || enablementRefreshResult.changed() )\n" +
                   "    {\n" +
                   "        notifyPropertyChangeListeners( #3, enablementRefreshResult );\n" +
                   "    }\n" +
                   "}",
                   variableName, wrapperType.getSimpleName(), propField.name );
        
        rb.closeBlock();
        rb.closeBlock();
        
        implClassModel.addImport( ValueNormalizationService.class );
        
        // Define the getter method.
        
        final MethodModel getter = implClassModel.addMethod();
        getter.setName( getterMethodName );
        getter.setReturnType( wrapperType );
        
        final Body gb = getter.getBody();
        
        gb.append( "synchronized( root() )\n" + 
                   "{\n" +
                   "    if( this.#1 == null )\n" + 
                   "    {\n" +
                   "        refresh( #2, true );\n" +
                   "    }\n" +
                   "    \n" +
                   "    return this.#1;\n" +
                   "}",
                   variableName, propField.name );
        
        // Define the setter method, if necessary.
        
        if( ! hasDerivedValueProviderAnnotation && propField.getAnnotation( ReadOnly.class ) == null )
        {
            final MethodDeclaration setterMethodInInterface 
                = findMethodDeclaration( modelElementInterface, "set" + propField.propertyName, "java.lang.String" );
            
            if( setterMethodInInterface == null )
            {
                final String msg = NLS.bind( Resources.unableToFindStringSetter, modelElementInterface.getSimpleName(), propField.name );
                messager.printError( propField.getSourcePosition(), msg );
                throw new AbortException();
            }
            
            final String setterMethodName = setterMethodInInterface.getSimpleName();
            propField.setSetterMethodName( setterMethodName );
            
            MethodModel setter = null;
            
            setter = implClassModel.addMethod();
            setter.setName( setterMethodName );
            
            final MethodParameterModel setterParam = new MethodParameterModel( "value", String.class );
            setterParam.setFinal( false );
            
            setter.addParameter( setterParam );
            
            final Body sb = setter.getBody();
            
            sb.append( "synchronized( root() )\n" +
                       "{\n" +
                       "    if( value != null && value.equals( MiscUtil.EMPTY_STRING ) )\n" +
                       "    {\n" +
                       "        value = null;\n" + 
                       "    }\n" +
                       "    \n" +
                       "    value = #1.decodeKeywords( value );\n" + 
                       "    value = service( #1, ValueNormalizationService.class ).normalize( value );\n" +
                       "    \n" +
                       "    refresh( #1, true );\n" +
                       "    \n" +
                       "    if( ! equal( this.#2.getText( false ), value ) )\n" +
                       "    {\n" +
                       "        resource().binding( #1 ).write( value );\n" +
                       "        refresh( #1, false );\n" +
                       "    }\n" +
                       "}\n",
                       propField.name, variableName );
            
            implClassModel.addImport( MiscUtil.class );
            
            if( ! baseType.getQualifiedName().equals( String.class.getName() ) )
            {
                final MethodDeclaration typedSetterMethodInInterface
                    = findMethodDeclaration( modelElementInterface, "set" + propField.propertyName, baseType.getQualifiedName() );
                
                if( typedSetterMethodInInterface == null )
                {
                    final String msg = NLS.bind( Resources.unableToFindTypedSetter, modelElementInterface.getSimpleName(), propField.name );
                    messager.printError( propField.getSourcePosition(), msg );
                    throw new AbortException();
                }
                
                final String typeSetterMethodName = typedSetterMethodInInterface.getSimpleName();
                propField.setTypedSetterMethodName( typeSetterMethodName );
                
                final MethodModel setterForTyped = implClassModel.addMethod();
                setterForTyped.setName( typeSetterMethodName );
                setterForTyped.addParameter( new MethodParameterModel( "value", baseType ) );
                
                final Body stb = setterForTyped.getBody();

                stb.append( "#1( value != null ? service( #2, ValueSerializationMasterService.class ).encode( value ) : null );", 
                            setterMethodName, propField.name );
                
                implClassModel.addImport( ValueSerializationMasterService.class );
            }
            
            contributeValueWriteMethodBlock( implClassModel, propField );
        }
        else
        {
            final MethodDeclaration setterMethodInInterface 
                = findMethodDeclaration( modelElementInterface, "set" + propField.propertyName, "java.lang.String" );
            
            if( setterMethodInInterface != null )
            {
                final String setterMethodName = setterMethodInInterface.getSimpleName();
                propField.setSetterMethodName( setterMethodName );
                
                MethodModel setter = null;
                
                setter = implClassModel.addMethod();
                setter.setName( setterMethodName );
                
                final MethodParameterModel setterParam = new MethodParameterModel( "value", String.class );
                setterParam.setFinal( false );
                
                setter.addParameter( setterParam );
                
                final Body sb = setter.getBody();
                sb.append( "throw new UnsupportedOperationException();" );
            }
            
            if( ! baseType.getQualifiedName().equals( String.class.getName() ) )
            {
                final MethodDeclaration typedSetterMethodInInterface
                    = findMethodDeclaration( modelElementInterface, "set" + propField.propertyName, baseType.getQualifiedName() );
                
                if( typedSetterMethodInInterface != null )
                {
                    final String typeSetterMethodName = typedSetterMethodInInterface.getSimpleName();
                    propField.setTypedSetterMethodName( typeSetterMethodName );
                    
                    final MethodModel setterForTyped = implClassModel.addMethod();
                    setterForTyped.setName( typeSetterMethodName );
                    setterForTyped.addParameter( new MethodParameterModel( "value", baseType ) );
                    
                    final Body stb = setterForTyped.getBody();
                    stb.append( "throw new UnsupportedOperationException();" );
                }
            }
        }
        
        // Contribute read method block.
        
        contributeReadMethodBlock( implClassModel, propField );
    }
    
    private void processElementProperty( final Messager messager,
                                         final ClassModel implClassModel,
                                         final InterfaceDeclaration interfaceDeclaration,
                                         final PropertyFieldDeclaration propField )
    {
        try
        {
            processElementPropertyInternal( messager, implClassModel, interfaceDeclaration, propField );
        }
        catch( AbortException e )
        {
            implClassModel.markInvalid();
        }
        catch( RuntimeException e )
        {
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter( sw );
            
            final InterfaceDeclaration modelElementInterface = propField.getDeclaringType();            
            
            pw.println();
            pw.println( "RuntimeException encountered during processElementProperty() method call." );
            pw.println();
            pw.println( "modelElementInterface : " + ( modelElementInterface == null ? "null" : modelElementInterface.getQualifiedName() ) ); //$NON-NLS-2$
            pw.println( "propField : " + propField.name );
            pw.println();
            e.printStackTrace( pw );
            pw.println();
            pw.flush();
            
            System.err.println( sw.getBuffer().toString() );
            
            throw e;
        }
    }

    private void processElementPropertyInternal( final Messager messager,
                                                 final ClassModel implClassModel,
                                                 final InterfaceDeclaration interfaceDeclaration,
                                                 final PropertyFieldDeclaration propField )
    {
        final boolean isImplied = isInstanceOf( propField.getType(), ImpliedElementProperty.class.getName() );

        final MethodDeclaration getterMethodInInterface = findMethodDeclaration( interfaceDeclaration, "get" + propField.propertyName );
        
        if( getterMethodInInterface == null )
        {
            final String msg = NLS.bind( Resources.unableToFindGetter, interfaceDeclaration.getSimpleName(), propField.name );
            messager.printError( propField.getSourcePosition(), msg );
            throw new AbortException();
        }
        
        final String getterMethodName = getterMethodInInterface.getSimpleName();
        propField.setGetterMethodName( getterMethodName );
        
        final String variableName = propField.propertyName.substring( 0, 1 ).toLowerCase() + propField.propertyName.substring( 1 );
        
        TypeReference memberType = null;
        
        final Type typeAnnotation = propField.getAnnotation( Type.class );
        
        try
        {
            typeAnnotation.base();
        }
        catch( MirroredTypeException e )
        {
            final InterfaceDeclaration typeMirror = ( (InterfaceType) e.getTypeMirror() ).getDeclaration();
            memberType = new TypeReference( typeMirror.getQualifiedName() );
        }
        
        final TypeReference handleType = ( new TypeReference( ModelElementHandle.class ) ).parameterize( memberType );
        
        final FieldModel field = implClassModel.addField();
        field.setName( variableName );
        field.setType( handleType );
        
        final MethodModel g = implClassModel.addMethod();
        g.setName( getterMethodName );
        g.setReturnType( isImplied ? memberType : handleType );
        
        final Body gb = g.getBody();
        
        gb.append( "synchronized( root() )\n" +
                   "{\n" +
                   "    if( this.#1 == null )\n" +
                   "    {\n" +
                   "        refresh( #2, true );\n" +
                   "    }\n" +
                   "    \n" +
                   "    return this.#1#3;\n" +
                   "}", 
                   variableName, propField.name, ( isImplied ? ".element()" : "" ) );
        
        final Body rb = prepareRefreshMethodBlock( implClassModel, propField );
        
        rb.append( "if( this.#1 == null )\n" +
                   "{\n" +
                   "    if( force == true )\n" +
                   "    {\n" +
                   "        this.#1 = new ModelElementHandle<#3>( this, #2 );\n" +
                   "        this.#1.init();\n" +
                   "        refreshPropertyEnablement( #2 );\n" +
                   "    }\n" +
                   "}\n" +
                   "else\n" +
                   "{\n" +
                   "    final EnablementRefreshResult enablementRefreshResult = refreshPropertyEnablement( #2 );\n" +
                   "    final boolean notified = this.#1.refresh();\n" +
                   "    \n" +
                   "    if( ! notified && enablementRefreshResult.changed() )\n" +
                   "    {\n" +
                   "        notifyPropertyChangeListeners( #2, enablementRefreshResult );\n" +
                   "    }\n" +
                   "}",
                   variableName, propField.name, memberType.getSimpleName() );
        
        rb.closeBlock();
        
        // Contribute read method block.
        
        contributeReadMethodBlock( implClassModel, propField );
    }
    
    private void processListProperty( final Messager messager,
                                      final ClassModel implClassModel,
                                      final InterfaceDeclaration interfaceDeclaration,
                                      final PropertyFieldDeclaration propField )
    {
        try
        {
            processListPropertyInternal( messager, implClassModel, interfaceDeclaration, propField );
        }
        catch( AbortException e )
        {
            implClassModel.markInvalid();
        }
        catch( RuntimeException e )
        {
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter( sw );
            
            final InterfaceDeclaration modelElementInterface = propField.getDeclaringType();            
            
            pw.println();
            pw.println( "RuntimeException encountered during processListProperty() method call." );
            pw.println();
            pw.println( "modelElementInterface : " + ( modelElementInterface == null ? "null" : modelElementInterface.getQualifiedName() ) ); //$NON-NLS-2$
            pw.println( "propField : " + propField.name );
            pw.println();
            e.printStackTrace( pw );
            pw.println();
            pw.flush();
            
            System.err.println( sw.getBuffer().toString() );
            
            throw e;
        }
    }

    private void processListPropertyInternal( final Messager messager,
                                              final ClassModel implClassModel,
                                              final InterfaceDeclaration interfaceDeclaration,
                                              final PropertyFieldDeclaration propField )
    {
        final MethodDeclaration getterMethodInInterface = findMethodDeclaration( interfaceDeclaration, "get" + propField.propertyName );
        
        if( getterMethodInInterface == null )
        {
            final String msg = NLS.bind( Resources.unableToFindGetter, interfaceDeclaration.getSimpleName(), propField.name );
            messager.printError( propField.getSourcePosition(), msg );
            throw new AbortException();
        }
        
        final String getterMethodName = getterMethodInInterface.getSimpleName();
        propField.setGetterMethodName( getterMethodName );
        
        final String variableName = propField.propertyName.substring( 0, 1 ).toLowerCase() + propField.propertyName.substring( 1 );
        
        TypeReference memberType = null;
        
        final Type typeAnnotation = propField.getAnnotation( Type.class );
        
        try
        {
            typeAnnotation.base();
        }
        catch( MirroredTypeException e )
        {
            final InterfaceDeclaration typeMirror = ( (InterfaceType) e.getTypeMirror() ).getDeclaration();
            memberType = new TypeReference( typeMirror.getQualifiedName() );
        }
        
        final TypeReference listType = ( new TypeReference( ModelElementList.class ) ).parameterize( memberType );
        
        final FieldModel field = implClassModel.addField();
        field.setName( variableName );
        field.setType( listType );
        
        final MethodModel g = implClassModel.addMethod();
        g.setName( getterMethodName );
        g.setReturnType( listType );
        
        final Body gb = g.getBody();
        
        gb.append( "synchronized( root() )\n" +
                   "{\n" +
                   "    if( this.#1 == null )\n" +
                   "    {\n" +
                   "        refresh( #2, true );\n" +
                   "    }\n" +
                   "    \n" +
                   "    return this.#1;\n" +
                   "}", 
                   variableName, propField.name );
        
        final Body rb = prepareRefreshMethodBlock( implClassModel, propField );
        
        rb.append( "if( this.#1 == null )\n" +
                   "{\n" +
                   "    if( force == true )\n" +
                   "    {\n" +
                   "        this.#1 = new ModelElementList<#3>( this, #2 );\n" +
                   "        final ListBindingImpl binding = resource().binding( #2 );\n" +
                   "        this.#1.init( binding );\n" +
                   "        refreshPropertyEnablement( #2 );\n" +
                   "    }\n" +
                   "}\n" +
                   "else\n" +
                   "{\n" +
                   "    final EnablementRefreshResult enablementRefreshResult = refreshPropertyEnablement( #2 );\n" +
                   "    final boolean notified = this.#1.refresh();\n" +
                   "    \n" +
                   "    if( ! notified && enablementRefreshResult.changed() )\n" +
                   "    {\n" +
                   "        notifyPropertyChangeListeners( #2, enablementRefreshResult );\n" +
                   "    }\n" +
                   "}",
                   variableName, propField.name, memberType.getSimpleName() );
        
        implClassModel.addImport( ListBindingImpl.class );
        
        rb.closeBlock();
        
        // Contribute read method block.
        
        contributeReadMethodBlock( implClassModel, propField );
    }
    
    private void processTransientProperty( final Messager messager,
                                           final ClassModel implClassModel,
                                           final InterfaceDeclaration interfaceDeclaration,
                                           final PropertyFieldDeclaration propField )
    {
        try
        {
            processTransientPropertyInternal( messager, implClassModel, interfaceDeclaration, propField );
        }
        catch( AbortException e )
        {
            implClassModel.markInvalid();
        }
        catch( RuntimeException e )
        {
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter( sw );
            
            final InterfaceDeclaration modelElementInterface = propField.getDeclaringType();
        
            pw.println();
            pw.println( "RuntimeException encountered during processTransientProperty() method call." );
            pw.println();
            pw.println( "interfaceDeclaration : " + ( modelElementInterface == null ? "null" : modelElementInterface.getQualifiedName() ) );
            pw.println( "propField : " + propField.name );
            pw.println();
            e.printStackTrace( pw );
            pw.println();
            pw.flush();
            
            System.err.println( sw.getBuffer().toString() );
            
            throw e;
        }
    }

    private void processTransientPropertyInternal( final Messager messager,
                                                   final ClassModel implClassModel,
                                                   final InterfaceDeclaration interfaceDeclaration,
                                                   final PropertyFieldDeclaration propField )
    {
        // Determine the property type.

        TypeReference baseType = null; 
        TypeReference wrapperType = null;        
        
        final Type typeAnnotation = propField.getAnnotation( Type.class );
        
        if( typeAnnotation == null )
        {
            baseType = new TypeReference( Object.class );
        }
        else
        {
            try
            {
                typeAnnotation.base();
            }
            catch( MirroredTypeException e )
            {
                baseType = toTypeReference( e.getTypeMirror() );
            }
        }
        
        wrapperType = ( new TypeReference( Transient.class ) ).parameterize( baseType );
        
        // Determine the variable, getter and setter names.
        
        final String variableName = propField.propertyName.substring( 0, 1 ).toLowerCase() + propField.propertyName.substring( 1 );
        
        final MethodDeclaration getterMethodInInterface = findMethodDeclaration( interfaceDeclaration, "get" + propField.propertyName );
        
        if( getterMethodInInterface == null )
        {
            final String msg = NLS.bind( Resources.unableToFindGetter, interfaceDeclaration.getSimpleName(), propField.name );
            messager.printError( propField.getSourcePosition(), msg );
            throw new AbortException();
        }
        
        final String getterMethodName = getterMethodInInterface.getSimpleName();
        propField.setGetterMethodName( getterMethodName );
        
        final MethodDeclaration setterMethodInInterface = findMethodDeclaration( interfaceDeclaration, "set" + propField.propertyName, baseType.getQualifiedName() );
        
        if( setterMethodInInterface == null )
        {
            final String msg = NLS.bind( Resources.unableToFindSetter, interfaceDeclaration.getSimpleName(), propField.name );
            messager.printError( propField.getSourcePosition(), msg );
            throw new AbortException();
        }
        
        final String setterMethodName = setterMethodInInterface.getSimpleName();
        propField.setSetterMethodName( setterMethodName );
        
        // Define the field that will hold the cached value of the property.
        
        final FieldModel field = implClassModel.addField();
        field.setName( variableName );
        field.setType( wrapperType );
        
        // Contribute content to the refresh method.
        
        final Body rb = prepareRefreshMethodBlock( implClassModel, propField );

        rb.append( "if( this.#1 == null )\n" +
                   "{\n" +
                   "    if( force == true )\n" +
                   "    {\n" +
                   "        #2( null );\n" +
                   "    }\n" +
                   "}\n" +
                   "else\n" +
                   "{\n" +
                   "    #2( this.#1.content() );\n" +
                   "}",
                   variableName, setterMethodName );
        
        rb.closeBlock();
        
        // Define the getter method.
        
        final MethodModel getter = implClassModel.addMethod();
        getter.setName( getterMethodName );
        getter.setReturnType( wrapperType );
        
        final Body gb = getter.getBody();
        
        gb.append( "synchronized( root() )\n" + 
                   "{\n" +
                   "    if( this.#1 == null )\n" + 
                   "    {\n" +
                   "        refresh( #2, true );\n" +
                   "    }\n" +
                   "    \n" +
                   "    return this.#1;\n" +
                   "}",
                   variableName, propField.name );
        
        // Define the setter method.
        
        final MethodModel setter = implClassModel.addMethod();
        setter.setName( setterMethodName );
        
        final MethodParameterModel param = new MethodParameterModel( "object", baseType );
        setter.addParameter( param );
        
        final Body sb = setter.getBody();
        
        sb.append( "synchronized( root() )\n" +
                   "{\n" +
                   "    final #2 oldTransient = this.#1;\n" +
                   "    \n" +
                   "    this.#1 = new #2( this, #3, object );\n" +
                   "    this.#1.init();\n" +
                   "    \n" +
                   "    final EnablementRefreshResult enablementRefreshResult = refreshPropertyEnablement( #3 );\n" +
                   "    \n" +
                   "    if( oldTransient == null )\n" +
                   "    {\n" +
                   "        if( object != null )\n" +
                   "        {\n" +
                   "            notifyPropertyChangeListeners( #3, enablementRefreshResult );\n" +
                   "        }\n" +
                   "    }\n" +
                   "    else\n" +
                   "    {\n" + 
                   "        if( this.#1.equals( oldTransient ) )\n" +
                   "        {\n" + 
                   "            this.#1 = oldTransient;\n" + 
                   "        }\n" + 
                   "        \n" +
                   "        if( this.#1 != oldTransient || enablementRefreshResult.changed() )\n" +
                   "        {\n" +
                   "            notifyPropertyChangeListeners( #3, enablementRefreshResult );\n" +
                   "        }\n" +
                   "    }\n" +
                   "}",
                   variableName, wrapperType.getSimpleName(), propField.name );
        
        // Contribute read and write method blocks.
        
        contributeReadMethodBlock( implClassModel, propField );
        contributeTransientWriteMethodBlock( implClassModel, propField );
    }
    
    private static MethodDeclaration findMethodDeclaration( final InterfaceDeclaration interfaceDeclaration,
                                                            final String methodName,
                                                            final String... paramTypes )
    {
        for( MethodDeclaration method : interfaceDeclaration.getMethods() )
        {
            if( method.getSimpleName().equalsIgnoreCase( methodName ) )
            {
                final Collection<ParameterDeclaration> params = method.getParameters();
                
                if( params.size() == paramTypes.length )
                {
                    final Iterator<ParameterDeclaration> itr = params.iterator();
                    boolean paramsMatch = true;
                    
                    for( String expectedParamTypeName : paramTypes )
                    {
                        final TypeMirror actualParamType = itr.next().getType();
    
                        if( actualParamType instanceof DeclaredType )
                        {
                            final TypeDeclaration actualParamTypeDeclaration = ( (DeclaredType) actualParamType ).getDeclaration();
                            
                            if( actualParamTypeDeclaration == null || ! actualParamTypeDeclaration.getQualifiedName().equals( expectedParamTypeName ) )
                            {
                                paramsMatch = false;
                                break;
                            }
                        }
                    }
                    
                    if( paramsMatch )
                    {
                        return method;
                    }
                }
            }
        }
        
        for( InterfaceType superInterfaceType : interfaceDeclaration.getSuperinterfaces() )
        {
            final MethodDeclaration method = findMethodDeclaration( superInterfaceType.getDeclaration(), methodName, paramTypes );
            
            if( method != null )
            {
                return method;
            }
        }
        
        return null;
    }
    
    private static String preparePropName( final String propFieldName )
    {
        final StringBuilder buf = new StringBuilder();
        boolean seenFirstSegment = false;
        
        for( String segment : propFieldName.split( "_" ) )
        {
            if( seenFirstSegment )
            {
                buf.append( segment.charAt( 0 ) );
                buf.append( segment.substring( 1 ).toLowerCase() );
            }
            else
            {
                // Skip the first segment that's always "PROP".
                
                seenFirstSegment = true;
            }
        }
        
        return buf.toString();
    }

    private static void visitAllMethods( final InterfaceDeclaration interfaceDeclaration,
                                         final Visitor<MethodDeclaration> visitor )
    {
        visitAllMethods( interfaceDeclaration, visitor, new HashSet<InterfaceDeclaration>() );
    }
    
    private static void visitAllMethods( final InterfaceDeclaration interfaceDeclaration,
                                         final Visitor<MethodDeclaration> visitor,
                                         final Set<InterfaceDeclaration> visited )
    {
        visited.add( interfaceDeclaration );
        
        for( MethodDeclaration method : interfaceDeclaration.getMethods() )
        {
            visitor.visit( method );
        }
        
        for( InterfaceType superInterface : interfaceDeclaration.getSuperinterfaces() )
        {
            final InterfaceDeclaration superInterfaceDeclaration = superInterface.getDeclaration();
            
            if( ! visited.contains( superInterfaceDeclaration ) )
            {
                visitAllMethods( superInterfaceDeclaration, visitor, visited );
            }
        }
    }

    private static void visitAllFields( final InterfaceDeclaration interfaceDeclaration,
                                        final Visitor<FieldDeclaration> visitor )
    {
        visitAllFields( interfaceDeclaration, visitor, new HashSet<InterfaceDeclaration>() );
    }
    
    private static void visitAllFields( final InterfaceDeclaration interfaceDeclaration,
                                        final Visitor<FieldDeclaration> visitor,
                                        final Set<InterfaceDeclaration> visited )
    {
        visited.add( interfaceDeclaration );
        
        for( InterfaceType superInterface : interfaceDeclaration.getSuperinterfaces() )
        {
            final InterfaceDeclaration superInterfaceDeclaration = superInterface.getDeclaration();
            
            if( ! visited.contains( superInterfaceDeclaration ) )
            {
                visitAllFields( superInterfaceDeclaration, visitor, visited );
            }
        }
        
        for( FieldDeclaration field : interfaceDeclaration.getFields() )
        {
            visitor.visit( field );
        }
    }
    
    private static MethodModel getReadMethod( final ClassModel implClassModel,
                                              final boolean createIfNecessary )
    {
        MethodModel readMethod = (MethodModel) implClassModel.getData( DATA_READ_METHOD );
        
        if( readMethod == null && createIfNecessary )
        {
            readMethod = implClassModel.addMethod( "read" );
            readMethod.setReturnType( Object.class );
            readMethod.addParameter( new MethodParameterModel( "property", ModelProperty.class, false ) );
            readMethod.setData( DATA_HAS_CONTENTS, Boolean.FALSE );
            implClassModel.setData( DATA_READ_METHOD, readMethod );

            final Body rb = readMethod.getBody();
            
            rb.append( "property = property.refine( this );" );
            rb.appendEmptyLine();
        }
        
        return readMethod;
    }
    
    private static Body contributeReadMethodBlock( final ClassModel implClassModel,
                                                   final PropertyFieldDeclaration propField )
    {
        final MethodModel readMethod = getReadMethod( implClassModel, true );
        final Body rb = readMethod.getBody();
        final boolean hasPriorContent;
        
        if( readMethod.getData( DATA_HAS_CONTENTS ) == Boolean.TRUE )
        {
            hasPriorContent = true;
        }
        else
        {
            hasPriorContent = false;
            readMethod.setData( DATA_HAS_CONTENTS, Boolean.TRUE );
        }
        
        rb.append( "#1if( property == #2 )\n" +
                   "{\n" +
                   "    return #3();\n" +
                   "}",
                   ( hasPriorContent ? "else " : "" ), propField.name, propField.getGetterMethodName() );

        return rb;
    }
    
    private static MethodModel getValueWriteMethod( final ClassModel implClassModel,
                                                    final boolean createIfNecessary )
    {
        MethodModel writeMethod = (MethodModel) implClassModel.getData( DATA_WRITE_VALUE_METHOD );
        
        if( writeMethod == null && createIfNecessary )
        {
            writeMethod = implClassModel.addMethod( "write" );
            writeMethod.addParameter( new MethodParameterModel( "property", ValueProperty.class, false ) );
            writeMethod.addParameter( new MethodParameterModel( "value", Object.class ) );
            writeMethod.setData( DATA_HAS_CONTENTS, Boolean.FALSE );
            implClassModel.setData( DATA_WRITE_VALUE_METHOD, writeMethod );

            final Body rb = writeMethod.getBody();
            
            rb.append( "property = (ValueProperty) property.refine( this );" );
            rb.appendEmptyLine();
        }
        
        return writeMethod;
    }
    
    private static Body contributeValueWriteMethodBlock( final ClassModel implClassModel,
                                                         final PropertyFieldDeclaration propField )
    {
        final MethodModel writeMethod = getValueWriteMethod( implClassModel, true );
        final Body rb = writeMethod.getBody();
        final boolean hasPriorContent;
        
        if( writeMethod.getData( DATA_HAS_CONTENTS ) == Boolean.TRUE )
        {
            hasPriorContent = true;
        }
        else
        {
            hasPriorContent = false;
            writeMethod.setData( DATA_HAS_CONTENTS, Boolean.TRUE );
        }
        
        final Type typeAnnotation = propField.getAnnotation( Type.class );
        TypeReference type = null;
        
        if( typeAnnotation != null )
        {
            try
            {
                typeAnnotation.base();
            }
            catch( MirroredTypeException e )
            {
                type = toTypeReference( e.getTypeMirror() );
            }
        }
        
        rb.append( "#1if( property == #2 )", ( hasPriorContent ? "else " : "" ), propField.name );
        rb.openBlock();
        
        if( type == null )
        {
            rb.append( "#1( (String) value );\n" +
                       "return;", 
                       propField.getSetterMethodName() );
        }
        else
        {
            rb.append( "if( ! ( value instanceof String ) )\n" +
                       "{\n" +
                       "    #2( (#3) value );\n" +
                       "}\n" +
                       "else\n" +
                       "{\n" +
                       "    #1( (String) value );\n" +
                       "}\n" +
                       "\n" +
                       "return;",
                       propField.getSetterMethodName(), propField.getTypedSetterMethodName(), type.getSimpleName() );
            
            implClassModel.addImport( type );
        }
        
        rb.closeBlock();

        return rb;
    }
    
    private static MethodModel getTransientWriteMethod( final ClassModel implClassModel,
                                                        final boolean createIfNecessary )
    {
        MethodModel writeMethod = (MethodModel) implClassModel.getData( DATA_WRITE_TRANSIENT_METHOD );
        
        if( writeMethod == null && createIfNecessary )
        {
            writeMethod = implClassModel.addMethod( "write" );
            writeMethod.addParameter( new MethodParameterModel( "property", TransientProperty.class, false ) );
            writeMethod.addParameter( new MethodParameterModel( "object", Object.class ) );
            writeMethod.setData( DATA_HAS_CONTENTS, Boolean.FALSE );
            implClassModel.setData( DATA_WRITE_TRANSIENT_METHOD, writeMethod );

            final Body rb = writeMethod.getBody();
            
            rb.append( "property = (TransientProperty) property.refine( this );" );
            rb.appendEmptyLine();
        }
        
        return writeMethod;
    }
    
    private static Body contributeTransientWriteMethodBlock( final ClassModel implClassModel,
                                                             final PropertyFieldDeclaration propField )
    {
        final MethodModel writeMethod = getTransientWriteMethod( implClassModel, true );
        final Body rb = writeMethod.getBody();
        final boolean hasPriorContent;
        
        if( writeMethod.getData( DATA_HAS_CONTENTS ) == Boolean.TRUE )
        {
            hasPriorContent = true;
        }
        else
        {
            hasPriorContent = false;
            writeMethod.setData( DATA_HAS_CONTENTS, Boolean.TRUE );
        }
        
        final Type typeAnnotation = propField.getAnnotation( Type.class );
        TypeReference type = null;
        
        if( typeAnnotation != null )
        {
            try
            {
                typeAnnotation.base();
            }
            catch( MirroredTypeException e )
            {
                type = toTypeReference( e.getTypeMirror() );
            }
        }
        
        rb.append( "#1if( property == #2 )", ( hasPriorContent ? "else " : "" ), propField.name );
        rb.openBlock();
        
        if( type == null )
        {
            rb.append( "#1( object );", propField.getSetterMethodName() );
        }
        else
        {
            rb.append( "#1( (#2) object );", propField.getSetterMethodName(), type.getSimpleName() );
        }
        
        rb.append( "return;" );
        
        rb.closeBlock();

        return rb;
    }
    
    private static MethodModel getRefreshMethod( final ClassModel implClassModel,
                                                 final boolean createIfNecessary )
    {
        MethodModel refreshMethod = (MethodModel) implClassModel.getData( DATA_REFRESH_METHOD );
        
        if( refreshMethod == null && createIfNecessary )
        {
            refreshMethod = implClassModel.addMethod( "refreshProperty" );
            refreshMethod.setAccessModifier( AccessModifier.PROTECTED );
            refreshMethod.addParameter( new MethodParameterModel( "property", ModelProperty.class, false ) );
            refreshMethod.addParameter( new MethodParameterModel( "force", TypeReference.BOOLEAN_TYPE ) );
            refreshMethod.setData( DATA_HAS_CONTENTS, Boolean.FALSE );
            implClassModel.setData( DATA_REFRESH_METHOD, refreshMethod );

            final Body rb = refreshMethod.getBody();
            
            rb.append( "synchronized( root() )" );
            rb.openBlock();
            
            rb.append( "property = property.refine( this );" );
            rb.appendEmptyLine();
        }
        
        return refreshMethod;
    }
    
    private static Body prepareRefreshMethodBlock( final ClassModel implClassModel,
                                                   final PropertyFieldDeclaration propField )
    {
        final MethodModel refreshMethod = getRefreshMethod( implClassModel, true );
        final Body rb = refreshMethod.getBody();
        final boolean hasPriorContent;
        
        if( refreshMethod.getData( DATA_HAS_CONTENTS ) == Boolean.TRUE )
        {
            hasPriorContent = true;
        }
        else
        {
            hasPriorContent = false;
            refreshMethod.setData( DATA_HAS_CONTENTS, Boolean.TRUE );
        }
        
        rb.append( "#1if( property == #2 )", ( hasPriorContent ? "else " : "" ), propField.name );
        rb.openBlock();

        return rb;
    }
    
    private static TypeReference toTypeReference( final TypeMirror typeMirror )
    {
        if( typeMirror instanceof VoidType )
        {
            return TypeReference.VOID_TYPE;
        }
        else if( typeMirror instanceof PrimitiveType )
        {
            return TypeReference.PRIMITIVE_TYPES.get( ( (PrimitiveType) typeMirror ).getKind() );
        }
        else if( typeMirror instanceof DeclaredType )
        {
            return new TypeReference( ( (DeclaredType) typeMirror ).getDeclaration().getQualifiedName() );
        }
        else if( typeMirror instanceof ArrayType )
        {
            return toTypeReference( ( (ArrayType) typeMirror ).getComponentType() ).array( 1 );
        }
        else
        {
            return new TypeReference( ( (TypeDeclaration) typeMirror ).getQualifiedName() );
        }
    }
    
    private static String getQualifiedName( final TypeDeclaration type )
    {
        final StringBuilder qname = new StringBuilder();
        final String pkg = type.getPackage().getQualifiedName();
        
        if( pkg.length() > 0 )
        {
            qname.append( pkg );
            qname.append( '.' );
            qname.append( type.getQualifiedName().substring( pkg.length() + 1 ).replace( '.', '$' ) );
        }
        else
        {
            qname.append( type.getQualifiedName().replace( '.', '$' ) );
        }
        
        return qname.toString();
    }
    
    private interface Visitor<T>
    {
        void visit( T item );
    }

    public static class PropertyFieldDeclaration
    {
        public String name;
        public String propertyName;
        public LinkedList<FieldDeclaration> declarations = new LinkedList<FieldDeclaration>();
        private String getterMethodName;
        private String setterMethodName;
        private String typedSetterMethodName;
        
        public boolean isElementProperty()
        {
            return isInstanceOf( getType(), ElementProperty.class.getName() );        
        }
        
        public TypeMirror getType()
        {
            if( this.declarations.isEmpty() )
            {
                return null;
            }
            else
            {
                return this.declarations.getFirst().getType();
            }
        }
        
        public InterfaceDeclaration getDeclaringType()
        {
            if( ! this.declarations.isEmpty() )
            {
                final Object declaringType = this.declarations.getFirst().getDeclaringType();
    
                if( declaringType instanceof InterfaceDeclaration )
                {
                    return (InterfaceDeclaration) declaringType;
                }
                else if( declaringType instanceof InterfaceType )
                {
                    return ( (InterfaceType) declaringType ).getDeclaration();
                }
                else
                {
                    throw new IllegalStateException();
                }
            }
            
            return null;
        }
        
        public <A extends Annotation> A getAnnotation( final Class<A> annotationType )
        {
            A annotation = null;
            
            for( FieldDeclaration fd : this.declarations )
            {
                annotation = fd.getAnnotation( annotationType );
                
                if( annotation != null )
                {
                    break;
                }
            }
            
            return annotation;
        }
        
        public String getGetterMethodName()
        {
            return this.getterMethodName;
        }
        
        public void setGetterMethodName( final String getterMethodName )
        {
            this.getterMethodName = getterMethodName;
        }
        
        public String getSetterMethodName()
        {
            return this.setterMethodName;
        }
        
        public void setSetterMethodName( final String setterMethodName )
        {
            this.setterMethodName = setterMethodName;
        }
        
        public String getTypedSetterMethodName()
        {
            return this.typedSetterMethodName;
        }
        
        public void setTypedSetterMethodName( final String typedSetterMethodName )
        {
            this.typedSetterMethodName = typedSetterMethodName;
        }
        
        public SourcePosition getSourcePosition()
        {
            return this.declarations.get( 0 ).getPosition();
        }
    }
    
    private static class AbortException extends RuntimeException 
    {
        private static final long serialVersionUID = 1L;
    }
    
    private static final class Resources extends NLS
    {
        public static String unableToFindGetter;
        public static String unableToFindSetter;
        public static String unableToFindStringSetter;
        public static String unableToFindTypedSetter;
        
        static
        {
            initializeMessages( GenerateImplProcessor.class.getName(), Resources.class );
        }
    }
    
}
