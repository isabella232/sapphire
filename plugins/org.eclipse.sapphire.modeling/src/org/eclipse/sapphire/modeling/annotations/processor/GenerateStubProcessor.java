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

import org.eclipse.sapphire.modeling.IModel;
import org.eclipse.sapphire.modeling.IModelParticle;
import org.eclipse.sapphire.modeling.Model;
import org.eclipse.sapphire.modeling.ModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelStore;
import org.eclipse.sapphire.modeling.annotations.GenerateStub;
import org.eclipse.sapphire.modeling.annotations.processor.util.AccessModifier;
import org.eclipse.sapphire.modeling.annotations.processor.util.Body;
import org.eclipse.sapphire.modeling.annotations.processor.util.ClassModel;
import org.eclipse.sapphire.modeling.annotations.processor.util.MethodModel;
import org.eclipse.sapphire.modeling.annotations.processor.util.MethodParameterModel;
import org.eclipse.sapphire.modeling.annotations.processor.util.TypeReference;

import com.sun.mirror.declaration.InterfaceDeclaration;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class GenerateStubProcessor 

    extends GenerateModelElementProcessor
    
{
    @Override
    protected void preProcess( final InterfaceDeclaration elInterface,
                               final ClassModel elImplClass )
    {
        final String simpleName = elInterface.getSimpleName().substring( 1 );
        final String defaultPackageName = elInterface.getPackage().getQualifiedName() + ".internal";
        
        final GenerateStub generateStubAnnotation = elInterface.getAnnotation( GenerateStub.class );
        
        String packageName = generateStubAnnotation.packageName();
        
        if( packageName.length() == 0 )
        {
            packageName = defaultPackageName;
        }

        elImplClass.setName( new TypeReference( packageName, simpleName + "Stub" ) );
        elImplClass.setAbstract( true );
        elImplClass.addInterface( new TypeReference( elInterface.getQualifiedName() ) );
        
        final MethodModel c = elImplClass.addConstructor();

        if( isInstanceOf( elInterface, IModel.class.getName() ) )
        {
            elImplClass.setBaseClass( new TypeReference( Model.class.getName() ) );
            
            c.addParameter( new MethodParameterModel( "modelStore", ModelStore.class ) );
            c.getBody().append( "super( TYPE, modelStore );\n" );
        }
        else
        {
            elImplClass.setBaseClass( new TypeReference( ModelElement.class.getName() ) );
            
            c.addParameter( new MethodParameterModel( "parent", IModelParticle.class ) );
            c.addParameter( new MethodParameterModel( "parentProperty", ModelProperty.class ) );
            c.getBody().append( "super( TYPE, parent, parentProperty );" );
        }
    }

    @Override
    protected void generateRemoveMethodBody( final InterfaceDeclaration elInterface,
                                             final ClassModel elImplClass,
                                             final Body removeMethodBody )
    {
        removeMethodBody.append( "validateEdit();" );
        removeMethodBody.append( "doRemove();" ); 

        final MethodModel drm = elImplClass.addMethod( "doRemove" );
        drm.setAccessModifier( AccessModifier.PROTECTED );
        drm.setAbstract( true );
    }

    @Override
    protected void generateValuePropertyReadLogic( final InterfaceDeclaration elInterface,
                                                   final ClassModel elImplClass,
                                                   final PropertyFieldDeclaration property,
                                                   final Body mb )
    {
        mb.append( "final String val = read#1();", property.propertyName );
        
        final MethodModel rm = elImplClass.addMethod( "read" + property.propertyName );
        rm.setAbstract( true );
        rm.setAccessModifier( AccessModifier.PROTECTED );
        rm.setReturnType( String.class );
    }
    
    @Override
    protected void generateValuePropertyWriteLogic( final InterfaceDeclaration elInterface,
                                                    final ClassModel elImplClass,
                                                    final PropertyFieldDeclaration property,
                                                    final Body mb )
    {
        mb.append( "write#1( value );", property.propertyName );
        
        final MethodModel wm = elImplClass.addMethod( "write" + property.propertyName );
        wm.setAbstract( true );
        wm.setAccessModifier( AccessModifier.PROTECTED );
        wm.addParameter( new MethodParameterModel( "value", String.class ) );
    }
    
    @Override
    protected void generateElementPropertyRefreshLogic( final InterfaceDeclaration elInterface,
                                                        final ClassModel elImplClass,
                                                        final PropertyFieldDeclaration property,
                                                        final Body mb )
    {
        mb.append( "element = refresh#1();", property.propertyName );
        
        final TypeReference type = (TypeReference) property.getData( DATA_ELEMENT_TYPE );
        
        final MethodModel rm = elImplClass.addMethod( "refresh" + property.propertyName );
        rm.setAbstract( true );
        rm.setAccessModifier( AccessModifier.PROTECTED );
        rm.setReturnType( type );
    }

    @Override
    protected void generateElementPropertyCreateLogic( InterfaceDeclaration elInterface,
                                                       ClassModel elImplClass,
                                                       PropertyFieldDeclaration property,
                                                       Body mb )
    {
        mb.append( "create#1();", property.propertyName );
        
        final MethodModel cm = elImplClass.addMethod( "create" + property.propertyName );
        cm.setAbstract( true );
        cm.setAccessModifier( AccessModifier.PROTECTED );
        cm.setReturnType( TypeReference.VOID_TYPE );
    }
    
    @Override
    protected void generateListPropertyInitLogic( final InterfaceDeclaration elInterface,
                                                  final ClassModel elImplClass,
                                                  final PropertyFieldDeclaration property,
                                                  final Body mb )
    {
        final String variableName = (String) property.getData( DATA_VARIABLE_NAME );
        final TypeReference listType = (TypeReference) property.getData( DATA_LIST_TYPE );
        
        mb.append( "this.#1 = init#2();", variableName, property.propertyName );
        
        final MethodModel im = elImplClass.addMethod( "init" + property.propertyName );
        im.setAbstract( true );
        im.setAccessModifier( AccessModifier.PROTECTED );
        im.setReturnType( listType );
    }

}
