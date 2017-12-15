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

package org.eclipse.sapphire.modeling.annotations.processor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.IModelParticle;
import org.eclipse.sapphire.modeling.IRemovable;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ReferenceValue;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.annotations.processor.util.AccessModifier;
import org.eclipse.sapphire.modeling.annotations.processor.util.Body;
import org.eclipse.sapphire.modeling.annotations.processor.util.ClassModel;
import org.eclipse.sapphire.modeling.annotations.processor.util.FieldModel;
import org.eclipse.sapphire.modeling.annotations.processor.util.IndentingPrintWriter;
import org.eclipse.sapphire.modeling.annotations.processor.util.MethodModel;
import org.eclipse.sapphire.modeling.annotations.processor.util.MethodParameterModel;
import org.eclipse.sapphire.modeling.annotations.processor.util.TypeReference;
import org.eclipse.sapphire.modeling.annotations.processor.util.WildcardTypeReference;
import org.eclipse.sapphire.modeling.serialization.ValueSerializationService;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
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

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class GenerateModelElementProcessor 

    extends SapphireAnnotationsProcessor
    
{
    private static final String DATA_REFRESH_METHOD = "refresh.method";
    private static final String DATA_HAS_CONTENTS = "has.contents";
    protected static final String DATA_VARIABLE_NAME = "variable.name";
    protected static final String DATA_IMPL_TYPE = "impl.type";
    protected static final String DATA_ELEMENT_TYPE = "element.type";
    protected static final String DATA_ELEMENT_TYPE_REMOVABLE = "element.type.removable";
    protected static final String DATA_LIST_TYPE = "list.type";
    protected static final String DATA_LIST_MEMBER_TYPE = "list.member.type";
    protected static final String DATA_PROPERTIES = "properties";
    
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
	        
	        process( implClassModel, interfaceDeclaration );
	        
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
		catch( Exception e )
		{
		    e.printStackTrace();
		}
	}
    
    protected void preProcess( final InterfaceDeclaration elInterface,
                               final ClassModel elImplClass )
    {
    }

    protected void postProcess( final InterfaceDeclaration elInterface,
                                final ClassModel elImplClass )
    {
    }
    
    protected void generateRemoveMethodBody( final InterfaceDeclaration elInterface,
                                             final ClassModel elImplClass,
                                             final Body removeMethodBody )
    {
    }
    
    protected boolean prepareValueProperty( final InterfaceDeclaration elInterface,
                                            final ClassModel elImplClass,
                                            final PropertyFieldDeclaration property )
    {
        return true;
    }

    protected abstract void generateValuePropertyReadLogic( InterfaceDeclaration elInterface,
                                                            ClassModel elImplClass,
                                                            PropertyFieldDeclaration property,
                                                            Body mb );
    
    protected abstract void generateValuePropertyWriteLogic( InterfaceDeclaration elInterface,
                                                             ClassModel elImplClass,
                                                             PropertyFieldDeclaration property,
                                                             Body mb );
    
    protected boolean prepareElementProperty( final InterfaceDeclaration elInterface,
                                              final ClassModel elImplClass,
                                              final PropertyFieldDeclaration property )
    {
        return true;
    }

    protected abstract void generateElementPropertyRefreshLogic( InterfaceDeclaration elInterface,
                                                                 ClassModel elImplClass,
                                                                 PropertyFieldDeclaration property,
                                                                 Body mb );

    protected abstract void generateElementPropertyCreateLogic( InterfaceDeclaration elInterface,
                                                                ClassModel elImplClass,
                                                                PropertyFieldDeclaration property,
                                                                Body mb );

    protected abstract void generateListPropertyInitLogic( InterfaceDeclaration elInterface,
                                                           ClassModel elImplClass,
                                                           PropertyFieldDeclaration property,
                                                           Body mb );

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

    private void process( final ClassModel implClassModel,
                          final InterfaceDeclaration interfaceDeclaration )
    {
        preProcess( interfaceDeclaration, implClassModel );
        
        final Map<String,PropertyFieldDeclaration> propFields = new TreeMap<String,PropertyFieldDeclaration>();
        implClassModel.setData( DATA_PROPERTIES, propFields );
        
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

        visitAllFields( interfaceDeclaration, fieldsVisitor );
        
        for( PropertyFieldDeclaration field : propFields.values() )
        {
            if( isInstanceOf( field.getType(), ValueProperty.class.getName() ) )
            {
                processValueProperty( implClassModel, interfaceDeclaration, field );
            }
            else if( isInstanceOf( field.getType(), ElementProperty.class.getName() ) )
            {
                processElementProperty( implClassModel, interfaceDeclaration, field );
            }
            else if( isInstanceOf( field.getType(), ListProperty.class.getName() ) )
            {
                processListProperty( implClassModel, interfaceDeclaration, field );
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
                    final MethodModel m = implClassModel.addMethod( method.getSimpleName() );
                    m.setReturnType( toTypeReference( method.getReturnType() ) );
                    
                    for( ParameterDeclaration param : method.getParameters() )
                    {
                        final MethodParameterModel p = new MethodParameterModel();
                        p.setName( param.getSimpleName() );
                        p.setType( toTypeReference( param.getType() ) );
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
                    
                    mb.append( "synchronized( this.model )" );
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
                    
                    implClassModel.addImport( delegate );
                }
            }
        };
        
        visitAllMethods( interfaceDeclaration, methodsVisitor );
        
        final MethodModel mRefresh = getRefreshMethod( implClassModel, false );
        
        if( mRefresh != null )
        {
            implClassModel.removeMethod( mRefresh );
            implClassModel.addMethod( mRefresh );
            mRefresh.getBody().closeBlock();
        }
        
        if( isInstanceOf( interfaceDeclaration, IRemovable.class.getName() ) )
        {
            final MethodModel rm = implClassModel.addMethod( "remove" );
            final Body rmb = rm.getBody();
            
            rmb.append( "synchronized( this.model )" );
            rmb.openBlock();
            generateRemoveMethodBody( interfaceDeclaration, implClassModel, rmb );
            rmb.closeBlock();
            rmb.appendEmptyLine();
            
            rmb.append( "final IModelParticle parent = getParent();\n" +
                        "\n" +
                        "if( parent != null )\n" +
                        "{\n" +
                        "    if( parent instanceof ModelElementList<?> )\n" +
                        "    {\n" +
                        "        ( (ModelElementList<?>) parent ).handleElementRemovedEvent();\n" + 
                        "    }\n" +
                        "    else\n" +
                        "    {\n" +
                        "        ( (IModelElement) parent ).refresh( getParentProperty() );\n" +
                        "    }\n" +
                        "}" );

            implClassModel.addImport( IModelElement.class );
            implClassModel.addImport( IModelParticle.class );
            implClassModel.addImport( ModelElementList.class );
        }
        
        postProcess( interfaceDeclaration, implClassModel );
    }

    private void processValueProperty( final ClassModel implClassModel,
	                                   final InterfaceDeclaration interfaceDeclaration,
	                                   final PropertyFieldDeclaration propField )
    {
        try
        {
            processValuePropertyInternal( implClassModel, interfaceDeclaration, propField );
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

    private void processValuePropertyInternal( final ClassModel implClassModel,
                                               final InterfaceDeclaration interfaceDeclaration,
                                               final PropertyFieldDeclaration propField )
	{
        final boolean includeThisProperty = prepareValueProperty( interfaceDeclaration, implClassModel, propField );
        
        if( ! includeThisProperty )
        {
            return;
        }
        
        // Determine the property type.

        TypeReference baseType = null; 
        TypeReference wrapperType = null;        
        
        final Reference referenceAnnotation = propField.getAnnotation( Reference.class );
        
        if( referenceAnnotation != null )
        {
        	baseType = new TypeReference( String.class );
        	
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
	            
	            wrapperType = new TypeReference( ReferenceValue.class ).parameterize( targetType );
	        }
        }
        else
        {
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
	        
	        wrapperType = ( new TypeReference( Value.class ) ).parameterize( baseType );
        }
        
        // Determine the getter method name.
        
        final String variableName = propField.propertyName.substring( 0, 1 ).toLowerCase() + propField.propertyName.substring( 1 );
        propField.setData( DATA_VARIABLE_NAME, variableName );
        
        final String setterMethodName = "set" + propField.propertyName;
        String getterMethodName = null;
        
        final InterfaceDeclaration modelElementInterface = propField.getDeclaringType();
        final String getterAlt1 = "get" + propField.propertyName;
        final String getterAlt2 = "is" + propField.propertyName;

        MethodDeclaration getterMethod = findMethodDeclaration( modelElementInterface, getterAlt1 );
        
        if( getterMethod == null )
        {
            getterMethod = findMethodDeclaration( modelElementInterface, getterAlt2 );
        }
        
        if( getterMethod == null )
        {
            throw new IllegalStateException( "Unable to find getter method for " + modelElementInterface.getSimpleName() + '@' + propField.name );
        }
        
        getterMethodName = getterMethod.getSimpleName();
        
        // Define the field that will hold the cached value of the property.
        
        final FieldModel field = implClassModel.addField();
        field.setName( variableName );
        field.setType( wrapperType );
        field.setValue( "null" );
        
        // Contribute content to the refresh method.
        
        final Body rb = prepareRefreshMethodBlock( implClassModel, propField );

        rb.append( "if( this.#1 != null || force == true )", variableName );
        rb.openBlock();
        rb.append( "final #1 oldValue = this.#2;", wrapperType.getSimpleName(), variableName );
        
        generateValuePropertyReadLogic( interfaceDeclaration, implClassModel, propField, rb );
        rb.appendEmptyLine();
        
        rb.append( "this.#1 = new #2( this, #3, #3.encodeKeywords( val ) );\n" +
                   "this.#1.init();\n" +
                   "\n" +
                   "final boolean propertyEnabledStatusChanged = refreshPropertyEnabledStatus( #3 );\n" +
                   "\n" +
                   "if( oldValue != null )\n" +
                   "{\n" + 
                   "    if( this.#1.equals( oldValue ) )\n" +
                   "    {\n" + 
                   "        this.#1 = oldValue;\n" + 
                   "    }\n" + 
                   "    \n" +
                   "    if( this.#1 != oldValue || propertyEnabledStatusChanged )\n" +
                   "    {\n" +
                   "        notifyPropertyChangeListeners( #3 );\n" +
                   "    }\n" +
                   "}",
                   variableName, wrapperType.getSimpleName(), propField.name );
        
        rb.closeBlock();
        rb.closeBlock();
        
        // Define the getter method.
        
        final MethodModel getter = implClassModel.addMethod();
        getter.setName( getterMethodName );
        getter.setReturnType( wrapperType );
        
        final Body gb = getter.getBody();
        
        gb.append( "synchronized( this.model )\n" + 
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
        
        MethodModel setter = null;
        
        if( findMethodDeclaration( modelElementInterface, setterMethodName ) != null )
        {
            setter = implClassModel.addMethod();
            setter.setName( setterMethodName );
            
            final MethodParameterModel param = new MethodParameterModel( "value", String.class );
            param.setFinal( false );
            
            setter.addParameter( param );
            
            final Body sb = setter.getBody();
            
            sb.append( "synchronized( this.model )" );
            sb.openBlock();
            
            sb.append( "if( value != null && value.equals( \"\" ) )\n" +
                       "{\n" +
                       "    value = null;\n" + 
                       "}\n" +
                       "\n" +
                       "value = #1.decodeKeywords( value );\n" + 
                       "\n" +
                       "refresh( #1, true );",
                       propField.name );
            
            sb.appendEmptyLine();
            sb.append( "if( ! equal( this.#1.getText( false ), value ) )", variableName );
            sb.openBlock();
            sb.append( "validateEdit();" );

            generateValuePropertyWriteLogic( interfaceDeclaration, implClassModel, propField, sb );
            
            sb.append( "refresh( #1, false );", propField.name );
            sb.closeBlock();
            sb.closeBlock();
            
            if( ! baseType.getQualifiedName().equals( String.class.getName() ) )
            {
                final MethodModel setterForTyped = implClassModel.addMethod();
                setterForTyped.setName( setterMethodName );
                setterForTyped.addParameter( new MethodParameterModel( "value", baseType ) );
                
                final Body stb = setterForTyped.getBody();

                stb.append( "#1( value != null ? service( ValueSerializationService.class ).encode( #2, value ) : null );", 
                		    setterMethodName, propField.name );
                
                implClassModel.addImport( ValueSerializationService.class );
            }
        }
	}
	
    private void processElementProperty( final ClassModel implClassModel,
                                         final InterfaceDeclaration interfaceDeclaration,
                                         final PropertyFieldDeclaration propField )
    {
        try
        {
            processElementPropertyInternal( implClassModel, interfaceDeclaration, propField );
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

    private void processElementPropertyInternal( final ClassModel implClassModel,
                                                 final InterfaceDeclaration interfaceDeclaration,
                                                 final PropertyFieldDeclaration propField )
    {
        final boolean includeThisProperty = prepareElementProperty( interfaceDeclaration, implClassModel, propField );
        
        if( ! includeThisProperty )
        {
            return;
        }
        
        final String getterMethodName = "get" + propField.propertyName;
        
        final String variableName = propField.propertyName.substring( 0, 1 ).toLowerCase() + propField.propertyName.substring( 1 );
        propField.setData( DATA_VARIABLE_NAME, variableName );
        
        TypeReference type = null;
        boolean removableType = false;
        TypeReference implType = null;
        
        final InterfaceDeclaration modelElementInterface = propField.getDeclaringType();
        
        for( MethodDeclaration method : modelElementInterface.getMethods() )
        {
            final String methodName = method.getSimpleName();
            
            if( methodName.equals( getterMethodName ) )
            {
                final TypeMirror getterReturnType = method.getReturnType();
                
                if( getterReturnType instanceof DeclaredType )
                {
                    final TypeDeclaration getterReturnTypeDeclaration 
                        = ( (DeclaredType) getterReturnType ).getDeclaration();

                    type = new TypeReference( getterReturnTypeDeclaration.getQualifiedName() );
                    propField.setData( DATA_ELEMENT_TYPE, type );
                    
                    removableType = ( isInstanceOf( getterReturnTypeDeclaration, IRemovable.class.getName() ) );
                    propField.setData( DATA_ELEMENT_TYPE_REMOVABLE, removableType );

                    // TODO: Implementation information needs to be read from annotations on the interface.
                    implType = new TypeReference( getterReturnTypeDeclaration.getPackage().getQualifiedName() + ".internal." + type.getSimpleName().substring( 1 ) );
                    propField.setData( DATA_IMPL_TYPE, implType );
                }
            }
        }
        
        final FieldModel f1 = implClassModel.addField();
        f1.setName( variableName );
        f1.setType( type );
        
        if( removableType )
        {
            final FieldModel f2 = implClassModel.addField();
            f2.setName( variableName + "Cached" );
            f2.setType( TypeReference.BOOLEAN_TYPE );
            f2.setValue( "false" );
        }
        
        final MethodModel getter1 = implClassModel.addMethod();
        getter1.setName( getterMethodName );
        getter1.setReturnType( type );
        
        final Body rb = prepareRefreshMethodBlock( implClassModel, propField );
        
        if( removableType )
        {
            getter1.getBody().append( "return " + getterMethodName + "( false );\n" );
            
            final MethodModel getter2 = implClassModel.addMethod( getterMethodName );
            getter2.setReturnType( type );
            getter2.addParameter( new MethodParameterModel( "createIfNecessary", TypeReference.BOOLEAN_TYPE ) );
            
            final Body g2b = getter2.getBody();
            
            g2b.append( "synchronized( this.model )" );
            g2b.openBlock();
            
            g2b.append( "if( this.#1Cached == false )\n" +
                        "{\n" +
                        "    refresh( #2, true );\n" +
                        "}",
                        variableName, propField.name );
            
            g2b.appendEmptyLine();
            g2b.append( "if( this.#1 == null && createIfNecessary )", variableName );
            g2b.openBlock();
            g2b.append( "validateEdit();" );

            generateElementPropertyCreateLogic( interfaceDeclaration, implClassModel, propField, g2b );

            g2b.append( "refresh( #1, true );", propField.name );
            g2b.closeBlock();
            g2b.appendEmptyLine();
            g2b.append( "return this.#1;", variableName );
            g2b.closeBlock();
            
            rb.append( "if( this.#1Cached == true || force == true )", variableName );
            rb.openBlock();
            rb.append( "this.#1Cached = true;", variableName );
            rb.appendEmptyLine();
            rb.append( "#1 element = null;", type.getSimpleName() );
            rb.appendEmptyLine();
            
            generateElementPropertyRefreshLogic( interfaceDeclaration, implClassModel, propField, rb );
            
            rb.appendEmptyLine();

            rb.append( "final boolean propertyEnabledStatusChanged = refreshPropertyEnabledStatus( #2 );\n" +
                       "\n" + 
                       "if( this.#1 != element )\n" +
                       "{\n" +
                       "    if( this.#1 != null )\n" +
                       "    {\n" +
                       "        this.#1.dispose();\n" +
                       "    }\n" +
                       "    \n" +
                       "    this.#1 = element;\n" +
                       "    \n" +
                       "    notifyPropertyChangeListeners( #2 );\n" +
                       "}\n" +
                       "else if( propertyEnabledStatusChanged )\n" +
                       "{\n" +
                       "    notifyPropertyChangeListeners( #2 );\n" +
                       "}",
                       variableName, propField.name );

            rb.closeBlock();
        }
        else
        {
            final Body g1b = getter1.getBody();
            
            g1b.append( "synchronized( this.model )\n" +
                        "{\n" +
                        "    if( this.#1 == null )\n" +
                        "    {\n" +
                        "        refresh( #2, true );\n" +
                        "    }\n" +
                        "    \n" +
                        "    return this.#1;\n" +
                        "}",
                        variableName, propField.name );
            
            rb.append( "if( this.#1 == null && force == true )\n" +
                       "{\n" +
                       "    this.#1 = new #2( this, #3 );\n" +
                       "}\n" +
                       "\n" +
                       "final boolean propertyEnabledStatusChanged = refreshPropertyEnabledStatus( #3 );\n" +
                       "\n" + 
                       "if( propertyEnabledStatusChanged )\n" +
                       "{\n" + 
                       "    notifyPropertyChangeListeners( #3 );\n" +
                       "}",
                       variableName, implType.getSimpleName(), propField.name );
        }
        
        rb.closeBlock();
        
        implClassModel.addImport( implType );
    }
    
    private void processListProperty( final ClassModel implClassModel,
                                      final InterfaceDeclaration interfaceDeclaration,
                                      final PropertyFieldDeclaration propField )
    {
        try
        {
            processListPropertyInternal( implClassModel, interfaceDeclaration, propField );
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

    private void processListPropertyInternal( final ClassModel implClassModel,
                                              final InterfaceDeclaration interfaceDeclaration,
                                              final PropertyFieldDeclaration propField )
    {
        final String getterMethodName = "get" + propField.propertyName;
        
        final String variableName = propField.propertyName.substring( 0, 1 ).toLowerCase() + propField.propertyName.substring( 1 );
        propField.setData( DATA_VARIABLE_NAME, variableName );
        
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
        
        propField.setData( DATA_LIST_MEMBER_TYPE, memberType );
        propField.setData( DATA_LIST_TYPE, listType );
        
        final FieldModel field = implClassModel.addField();
        field.setName( variableName );
        field.setType( listType );
        
        final MethodModel g = implClassModel.addMethod();
        g.setName( getterMethodName );
        g.setReturnType( listType );
        
        final Body gb = g.getBody();
        
        gb.append( "synchronized( this.model )\n" +
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
        
        rb.append( "final boolean propertyEnabledStatusChanged = refreshPropertyEnabledStatus( #1 );", propField.name );
        rb.appendEmptyLine();
        
        rb.append( "if( this.#1 == null && force == true )", variableName );
        rb.openBlock();
        
        generateListPropertyInitLogic( interfaceDeclaration, implClassModel, propField, rb );
        
        rb.closeBlock();
        rb.appendEmptyLine();
        
        rb.append( "if( this.#1 != null )\n" +
                   "{\n" +
                   "    final boolean notified = this.#1.refresh();\n" +
                   "    \n" +
                   "    if( ! notified && propertyEnabledStatusChanged )\n" +
                   "    {\n" +
                   "        notifyPropertyChangeListeners( #2 );\n" +
                   "    }\n" +
                   "}",
                   variableName, propField.name );

        rb.closeBlock();
    }
    
    private static final MethodDeclaration findMethodDeclaration( final InterfaceDeclaration interfaceDeclaration,
                                                                  final String methodName )
    {
        final List<MethodDeclaration> results = findMethodDeclarations( interfaceDeclaration, methodName );
        
        if( results.isEmpty() )
        {
            return null;
        }
        else
        {
            return results.get( 0 );
        }
    }
    
    private static final List<MethodDeclaration> findMethodDeclarations( final InterfaceDeclaration interfaceDeclaration,
                                                                         final String methodName )
    {
        final List<MethodDeclaration> results = new ArrayList<MethodDeclaration>();
        findMethodDeclarations( interfaceDeclaration, methodName, results );
        return results;
    }
    
    private static final void findMethodDeclarations( final InterfaceDeclaration interfaceDeclaration,
                                                      final String methodName,
                                                      final List<MethodDeclaration> results )
    {
        for( MethodDeclaration method : interfaceDeclaration.getMethods() )
        {
            if( method.getSimpleName().equals( methodName ) )
            {
                results.add( method );
            }
        }
        
        for( InterfaceType superInterfaceType : interfaceDeclaration.getSuperinterfaces() )
        {
            findMethodDeclarations( superInterfaceType.getDeclaration(), methodName, results );
        }
    }
    
    private static final String preparePropName( final String propFieldName )
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
        for( InterfaceType superInterface : interfaceDeclaration.getSuperinterfaces() )
        {
            final InterfaceDeclaration superInterfaceDeclaration = superInterface.getDeclaration();
            visitAllMethods( superInterfaceDeclaration, visitor );
        }
        
        for( MethodDeclaration method : interfaceDeclaration.getMethods() )
        {
            visitor.visit( method );
        }
    }

    private static void visitAllFields( final InterfaceDeclaration interfaceDeclaration,
                                        final Visitor<FieldDeclaration> visitor )
    {
        for( InterfaceType superInterface : interfaceDeclaration.getSuperinterfaces() )
        {
            final InterfaceDeclaration superInterfaceDeclaration = superInterface.getDeclaration();
            visitAllFields( superInterfaceDeclaration, visitor );
        }
        
        for( FieldDeclaration field : interfaceDeclaration.getFields() )
        {
            visitor.visit( field );
        }
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
            
            rb.append( "synchronized( this.model )" );
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
    
    private interface Visitor<T>
    {
        void visit( T item );
    }

    public static class PropertyFieldDeclaration
    {
        public String name;
        public String propertyName;
        public LinkedList<FieldDeclaration> declarations = new LinkedList<FieldDeclaration>();
        private Map<String,Object> data = new HashMap<String,Object>();
        
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
        
        public Object getData( final String key )
        {
            return this.data.get( key );
        }
        
        public void setData( final String key,
                             final Object value )
        {
            this.data.put( key, value );
        }
    }

}
