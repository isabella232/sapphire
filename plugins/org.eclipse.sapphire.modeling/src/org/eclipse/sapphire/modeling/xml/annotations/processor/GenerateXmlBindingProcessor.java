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

package org.eclipse.sapphire.modeling.xml.annotations.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.sapphire.modeling.IModel;
import org.eclipse.sapphire.modeling.IModelParticle;
import org.eclipse.sapphire.modeling.IRemovable;
import org.eclipse.sapphire.modeling.ModelElementListController;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.annotations.ListPropertyCustomBinding;
import org.eclipse.sapphire.modeling.annotations.ValuePropertyCustomBinding;
import org.eclipse.sapphire.modeling.annotations.processor.GenerateModelElementProcessor;
import org.eclipse.sapphire.modeling.annotations.processor.util.AccessModifier;
import org.eclipse.sapphire.modeling.annotations.processor.util.Body;
import org.eclipse.sapphire.modeling.annotations.processor.util.ClassModel;
import org.eclipse.sapphire.modeling.annotations.processor.util.FieldModel;
import org.eclipse.sapphire.modeling.annotations.processor.util.MethodModel;
import org.eclipse.sapphire.modeling.annotations.processor.util.MethodParameterModel;
import org.eclipse.sapphire.modeling.annotations.processor.util.StaticInitializerModel;
import org.eclipse.sapphire.modeling.annotations.processor.util.TypeReference;
import org.eclipse.sapphire.modeling.xml.IModelElementForXml;
import org.eclipse.sapphire.modeling.xml.ModelElementForXml;
import org.eclipse.sapphire.modeling.xml.ModelElementListControllerForXml;
import org.eclipse.sapphire.modeling.xml.ModelForXml;
import org.eclipse.sapphire.modeling.xml.ModelStoreForXml;
import org.eclipse.sapphire.modeling.xml.RootElementController;
import org.eclipse.sapphire.modeling.xml.StandardRootElementController;
import org.eclipse.sapphire.modeling.xml.StandardXmlNamespaceResolver;
import org.eclipse.sapphire.modeling.xml.XmlElement;
import org.eclipse.sapphire.modeling.xml.XmlNode;
import org.eclipse.sapphire.modeling.xml.XmlPath;
import org.eclipse.sapphire.modeling.xml.annotations.BooleanPropertyXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.CustomRootXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.GenerateXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.GenerateXmlBindingModelImpl;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBindingMapping;
import org.eclipse.sapphire.modeling.xml.annotations.RootXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.ValuePropertyCustomXmlBindingImpl;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.InterfaceDeclaration;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.MirroredTypeException;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class GenerateXmlBindingProcessor 

    extends GenerateModelElementProcessor
    
{
    private static final String DATA_CUSTOM_BINDING_IMPL_CLASS = "custom.binding.impl.class";
    private static final String DATA_CUSTOM_BINDING = "custom.binding";
    private static final String DATA_CUSTOM_BINDING_IMPL_FIELD = "custom.binding.impl.field";
    private static final String DATA_XML_PATH_CONSTANT = "xml.path.constant";
    private static final String DATA_TREAT_EXISTENCE_AS_VALUE = "treat.existence.as.value";
    private static final String DATA_VALUE_WHEN_PRESENT = "value.when.present";
    private static final String DATA_REMOVE_NODE_ON_SET_IF_NULL = "remove.node.on.set.if.null";
    private static final String DATA_REMOVE_EXTRA_WHITESPACE = "remove.extra.whitespace";
    
    @Override
    protected void preProcess( final InterfaceDeclaration elInterface,
                               final ClassModel elImplClass )
    {
        final String simpleName = elInterface.getSimpleName().substring( 1 );
        final String defaultPackageName = elInterface.getPackage().getQualifiedName() + ".internal";
        
        final GenerateXmlBindingModelImpl generateModelImplAnnotation 
            = elInterface.getAnnotation( GenerateXmlBindingModelImpl.class );
        
        if( generateModelImplAnnotation != null )
        {
            String packageName = generateModelImplAnnotation.packageName();
            
            if( packageName.length() == 0 )
            {
                packageName = defaultPackageName;
            }
            
            elImplClass.setName( new TypeReference( packageName, simpleName ) );
            elImplClass.setBaseClass( new TypeReference( ModelForXml.class.getName() ) );
            elImplClass.addInterface( new TypeReference( elInterface.getQualifiedName() ) );
            
            // Constructor
            
            final MethodModel c = new MethodModel();
            c.setConstructor( true );
            c.setName( elImplClass.getName().getSimpleName() );
            c.addParameter( new MethodParameterModel( "modelStore", ModelStoreForXml.class ) );
            c.getBody().append( "super( TYPE, modelStore );\n" );
            c.getBody().append( "( (ModelStoreForXml) getModel().getModelStore() ).registerRootModelElement( this );" );
            elImplClass.addMethod( c );
            
            // createRootElementController
            
            final MethodModel m = new MethodModel();
            m.setName( "createRootElementController" );
            m.setReturnType( new TypeReference( RootElementController.class ) );
            m.setAccessModifier( AccessModifier.PROTECTED );
            
            final RootXmlBinding rootXmlBindingAnnotation = elInterface.getAnnotation( RootXmlBinding.class );
            
            if( rootXmlBindingAnnotation != null )
            {
                final String ns = normalizeToNull( rootXmlBindingAnnotation.namespace() );
                final String sl = normalizeToNull( rootXmlBindingAnnotation.schemaLocation() );
                final String dp = normalizeToNull( rootXmlBindingAnnotation.defaultPrefix() );
                final String en = rootXmlBindingAnnotation.elementName();
                
                m.getBody().append( "return new StandardRootElementController( #1, #2, #3, \"#4\" );",
                                    ( ns == null ? "null" : "\"" + ns + "\"" ), ( sl == null ? "null" : "\"" + sl + "\"" ),
                                    ( dp == null ? "null" : "\"" + dp + "\"" ), en );
                
                elImplClass.addImport( StandardRootElementController.class );
            }
            
            final CustomRootXmlBinding customRootXmlBinding = elInterface.getAnnotation( CustomRootXmlBinding.class );
            
            if( customRootXmlBinding != null )
            {
                try
                {
                    customRootXmlBinding.value();
                }
                catch( MirroredTypeException e )
                {
                    final ClassDeclaration typeMirror = ( (ClassType) e.getTypeMirror() ).getDeclaration();
                    final TypeReference customRootElementController = new TypeReference( typeMirror.getQualifiedName() );
                    
                    m.getBody().append( "return new #1();", customRootElementController.getSimpleName() );
                    
                    elImplClass.addImport( customRootElementController );
                }
            }
            
            elImplClass.addMethod( m );
        }
        else
        {
            final GenerateXmlBinding generateModelElementImplAnnotation 
                = elInterface.getAnnotation( GenerateXmlBinding.class );
        
            String packageName = generateModelElementImplAnnotation.packageName();
            
            if( packageName.length() == 0 )
            {
                packageName = defaultPackageName;
            }
            
            elImplClass.setName( new TypeReference( packageName, simpleName ) );
            elImplClass.addInterface( new TypeReference( elInterface.getQualifiedName() ) );
            elImplClass.setBaseClass( new TypeReference( ModelElementForXml.class.getName() ) );
            
            // Constructor
            
            final MethodModel c = new MethodModel();
            c.setConstructor( true );
            c.setName( elImplClass.getName().getSimpleName() );
            c.addParameter( new MethodParameterModel( "parent", IModelParticle.class ) );
            c.addParameter( new MethodParameterModel( "parentProperty", ModelProperty.class ) );
            
            final Body cb = c.getBody();
            
            if( isInstanceOf( elInterface, IRemovable.class.getName() ) )
            {
                c.addParameter( new MethodParameterModel( "element", XmlElement.class ) );
                cb.append( "super( TYPE, parent, parentProperty, element );" );
                cb.append( "( (ModelStoreForXml) getModel().getModelStore() ).registerModelElement( element.getDomNode(), this );" );
                elImplClass.addImport( ModelStoreForXml.class );
            }
            else
            {
                cb.append( "super( TYPE, parent, parentProperty, null );" );
            }
            
            elImplClass.addMethod( c );
        }
    }
    
    @Override
    @SuppressWarnings( "unchecked" )
    protected void postProcess( final InterfaceDeclaration elInterface,
                                final ClassModel elImplClass )
    {
        String[] elementPath = {};

        final GenerateXmlBinding generateModelElementImplAnnotation 
            = elInterface.getAnnotation( GenerateXmlBinding.class );
        
        if( generateModelElementImplAnnotation != null )
        {
            elementPath = generateModelElementImplAnnotation.elementPath();
        }
        
        if( ! isInstanceOf( elInterface, IRemovable.class.getName() ) &&
            ! isInstanceOf( elInterface, IModel.class.getName() ) )
        {
            final MethodModel m = elImplClass.addMethod( "getXmlElement" );
            m.setReturnType( XmlElement.class );
            m.addParameter( new MethodParameterModel( "createIfNecessary", TypeReference.BOOLEAN_TYPE ) );
            
            final Body mb = m.getBody();
            
            mb.append( "synchronized( this.model )" );
            mb.openBlock();
            mb.append( "XmlElement root = ( (IModelElementForXml) getParent() ).getXmlElement( createIfNecessary );" );
            
            if( elementPath.length > 0 )
            {
                for( String element : elementPath )
                {
                    mb.appendEmptyLine();
                    
                    mb.append( "if( root != null )\n" +
                               "{\n" +
                               "    root = root.getChildElement( \"#1\", createIfNecessary );\n" +
                               "}",
                               element );
                }
            }
            
            mb.appendEmptyLine();
            mb.append( "return root;" );
            
            mb.closeBlock();
            
            elImplClass.addImport( IModelElementForXml.class );
        }
        
        final MethodModel m = elImplClass.addMethod( "getXmlNode" );
        m.setReturnType( XmlNode.class );
        m.addParameter( new MethodParameterModel( "property", ModelProperty.class, false ) );
        
        final Body mb = m.getBody();
        
        mb.append( "synchronized( this.model )" );
        mb.openBlock();
        
        mb.append( "property = property.refine( this );" );
        mb.appendEmptyLine();
        
        final Map<String,PropertyFieldDeclaration> properties 
            = (Map<String,PropertyFieldDeclaration>) elImplClass.getData( DATA_PROPERTIES );
        
        boolean hasContent = false;
        
        for( PropertyFieldDeclaration property : properties.values() )
        {
            final ClassDeclaration customBindingImplClass = (ClassDeclaration) property.getData( DATA_CUSTOM_BINDING_IMPL_CLASS );
            
            if( customBindingImplClass != null && isInstanceOf( customBindingImplClass, ValuePropertyCustomXmlBindingImpl.class.getName() ) )
            {
                final TypeReference customBindingImplTypeRef = (TypeReference) property.getData( DATA_CUSTOM_BINDING );
                final FieldModel customBindingImplField = (FieldModel) property.getData( DATA_CUSTOM_BINDING_IMPL_FIELD );

                mb.append( "#1if( property == #2 )\n" +
                           "{\n" +
                           "    if( this.#3 == null )\n" +
                           "    {\n" +
                           "        this.#3 = new #4();\n" +
                           "        this.#3.init( this, #2, #2.getAnnotation( ValuePropertyCustomBinding.class ).params() );\n" +
                           "    }\n" +
                           "    \n" +
                           "    return this.#3.getXmlNode();\n" +
                           "}", 
                           ( hasContent ? "else " : "" ), property.name,
                           customBindingImplField.getName(), customBindingImplTypeRef.getSimpleName() );
                
                hasContent = true;
            }
        }
        
        if( elImplClass.containsField( "MAP_PROP_TO_XML_PATH" ) )
        {
            if( hasContent )
            {
                mb.append( "else" );
                mb.openBlock();
            }
            
            mb.append( "XmlElement el = getXmlElement( false );\n" +
                       "\n" +
                       "if( el != null )\n" +
                       "{\n" +
                       "    final XmlPath path = MAP_PROP_TO_XML_PATH.get( property );\n" +
                       "    \n" +
                       "    if( path != null )\n" +
                       "    {\n" +
                       "        return el.getChildNode( path, false );\n" +
                       "    }\n" +
                       "}" );
            
            elImplClass.addImport( XmlElement.class );

            if( hasContent )
            {
                mb.closeBlock();
            }

            hasContent = true;
        }

        mb.appendEmptyLine();
        mb.append( "return null;" );
        mb.closeBlock();
        
        // We didn't end up producing any actual logic for this method. Let's get rid of it.
        
        if( ! hasContent )
        {
            elImplClass.removeMethod( m );
        }
    }
    
    @Override
    protected void generateRemoveMethodBody( final InterfaceDeclaration elInterface,
                                             final ClassModel elImplClass,
                                             final Body removeMethodBody )
    {
        removeMethodBody.append( "final XmlElement element = getXmlElement( false );\n" +
                                 "\n" +
                                 "if( element != null )\n" +
                                 "{\n" + 
                                 "    validateEdit();\n" +
                                 "    element.remove();\n" + 
                                 "}" );
    }
    
    @Override
    protected boolean prepareValueProperty( final InterfaceDeclaration elInterface,
                                            final ClassModel elImplClass,
                                            final PropertyFieldDeclaration property )
    {
        boolean xmlBindingFound = false;
        String xmlPathConstant = null;
        boolean treatExistenceAsValue = false;
        boolean valueWhenPresent = false;
        boolean removeNodeOnSetIfNull = true;
        boolean removeExtraWhitespace = false;
        ClassDeclaration customBindingImplClass = null;
        TypeReference customBinding = null;
        FieldModel customBindingImplField = null;
        
        final String variableName = property.propertyName.substring( 0, 1 ).toLowerCase() + property.propertyName.substring( 1 );
        
        final XmlBinding valuePropertyXmlBindingAnnotation = property.getAnnotation( XmlBinding.class );
        
        if( valuePropertyXmlBindingAnnotation != null )
        {
            xmlBindingFound = true;
            xmlPathConstant = processBasicXmlBinding( elImplClass, elInterface, property, valuePropertyXmlBindingAnnotation.path() );
            removeExtraWhitespace = valuePropertyXmlBindingAnnotation.removeExtraWhitespace();
            
            for( String x : valuePropertyXmlBindingAnnotation.options().split( ";" ) )
            {
                final String[] pair = x.split( "=" );
                
                if( pair.length == 2 )
                {
                    final String key = pair[ 0 ].trim();
                    final String value = pair[ 1 ].trim();
                    
                    if( key.equalsIgnoreCase( "removeNodeOnSetIfNull" ) )
                    {
                        removeNodeOnSetIfNull = value.equalsIgnoreCase( "true" );
                    }
                }
            }
        }
        
        if( ! xmlBindingFound )
        {
            final BooleanPropertyXmlBinding booleanPropertyXmlBindingAnnotation
                = property.getAnnotation( BooleanPropertyXmlBinding.class );
            
            if( booleanPropertyXmlBindingAnnotation != null )
            {
                xmlBindingFound = true;
                xmlPathConstant = processBasicXmlBinding( elImplClass, elInterface, property, booleanPropertyXmlBindingAnnotation.path() );

                if( booleanPropertyXmlBindingAnnotation.treatExistenceAsValue() == true )
                {
                    treatExistenceAsValue = true;
                    valueWhenPresent = booleanPropertyXmlBindingAnnotation.valueWhenPresent();
                }
            }
        }
        
        if( ! xmlBindingFound )
        {
            final ValuePropertyCustomBinding valuePropertyCustomXmlBindingAnnotation
                = property.getAnnotation( ValuePropertyCustomBinding.class );
            
            if( valuePropertyCustomXmlBindingAnnotation != null )
            {
                try
                {
                    valuePropertyCustomXmlBindingAnnotation.impl();
                }
                catch( MirroredTypeException e )
                {
                    xmlBindingFound = true;
                    
                    customBindingImplClass = ( (ClassType) e.getTypeMirror() ).getDeclaration();

                    customBinding = new TypeReference( customBindingImplClass.getQualifiedName() );
                    elImplClass.addImport( customBinding );
                    elImplClass.addImport( ValuePropertyCustomBinding.class );
                    
                    customBindingImplField = elImplClass.addField();
                    customBindingImplField.setName( variableName + "Binding" );
                    customBindingImplField.setType( customBinding );
                }
            }
        }
        
        property.setData( DATA_CUSTOM_BINDING_IMPL_CLASS, customBindingImplClass );
        property.setData( DATA_CUSTOM_BINDING, customBinding );
        property.setData( DATA_CUSTOM_BINDING_IMPL_FIELD, customBindingImplField );
        property.setData( DATA_XML_PATH_CONSTANT, xmlPathConstant );
        property.setData( DATA_TREAT_EXISTENCE_AS_VALUE, treatExistenceAsValue );
        property.setData( DATA_VALUE_WHEN_PRESENT, valueWhenPresent );
        property.setData( DATA_REMOVE_NODE_ON_SET_IF_NULL, removeNodeOnSetIfNull );
        property.setData( DATA_REMOVE_EXTRA_WHITESPACE, removeExtraWhitespace );
        
        return xmlBindingFound;
    }

    @Override
    protected void generateValuePropertyReadLogic( final InterfaceDeclaration elInterface,
                                                   final ClassModel elImplClass,
                                                   final PropertyFieldDeclaration property,
                                                   final Body mb )
    {
        final TypeReference customBinding = (TypeReference) property.getData( DATA_CUSTOM_BINDING );
        final FieldModel customBindingImplField = (FieldModel) property.getData( DATA_CUSTOM_BINDING_IMPL_FIELD );
        final String xmlPathConstant = (String) property.getData( DATA_XML_PATH_CONSTANT );
        final boolean treatExistenceAsValue = (Boolean) property.getData( DATA_TREAT_EXISTENCE_AS_VALUE );
        final boolean valueWhenPresent = (Boolean) property.getData( DATA_VALUE_WHEN_PRESENT );
        
        if( customBinding != null )
        {
            mb.appendEmptyLine();
            mb.append( "if( this.#1 == null )", customBindingImplField.getName() );
            mb.openBlock();
            mb.append( "this.#1 = new #2();", customBindingImplField.getName(), customBinding.getSimpleName() );
            mb.append( "this.#1.init( this, #2, #2.getAnnotation( ValuePropertyCustomBinding.class ).params() );", customBindingImplField.getName(), property.name );
            mb.closeBlock();
            mb.appendEmptyLine();
            mb.append( "final String val = this.#1.read();", customBindingImplField.getName() );
        }
        else
        {
            final boolean removeExtraWhitespace = (Boolean) property.getData( DATA_REMOVE_EXTRA_WHITESPACE );
            final String removeExtraWhitespaceStr = ( removeExtraWhitespace ? "true" : "false" );
            
            mb.append( "final XmlElement element = getXmlElement( false );" );
            
            if( treatExistenceAsValue )
            {
                mb.append( "final XmlNode child = ( element == null ? null : element.getChildNode( #1, false ) );", xmlPathConstant );
                mb.append( "final String val = ( child != null ? Boolean.#1.toString() : null );", ( valueWhenPresent == true ? "TRUE" : "FALSE" ) );
                elImplClass.addImport( XmlNode.class );
            }
            else if( xmlPathConstant == null )
            {
                mb.append( "final String val = ( element == null ? null : element.getText( #1 ) );", removeExtraWhitespaceStr );
            }
            else
            {
                mb.append( "final String val = ( element == null ? null : element.getChildNodeText( #1, #2 ) );", xmlPathConstant, removeExtraWhitespaceStr );
            }
        }
    }

	@Override
    protected void generateValuePropertyWriteLogic( final InterfaceDeclaration elInterface,
                                                    final ClassModel elImplClass,
                                                    final PropertyFieldDeclaration property,
                                                    final Body mb )
    {
        final TypeReference customBinding = (TypeReference) property.getData( DATA_CUSTOM_BINDING );
        final FieldModel customBindingImplField = (FieldModel) property.getData( DATA_CUSTOM_BINDING_IMPL_FIELD );
        final String xmlPathConstant = (String) property.getData( DATA_XML_PATH_CONSTANT );
        final boolean treatExistenceAsValue = (Boolean) property.getData( DATA_TREAT_EXISTENCE_AS_VALUE );
        final boolean valueWhenPresent = (Boolean) property.getData( DATA_VALUE_WHEN_PRESENT );
        final String variableName = (String) property.getData( DATA_VARIABLE_NAME );
        final boolean removeNodeOnSetIfNull = (Boolean) property.getData( DATA_REMOVE_NODE_ON_SET_IF_NULL );

        if( customBinding != null )
        {
            mb.appendEmptyLine();
            
            mb.append( "if( this.#1 == null )\n" +
                       "{\n" + 
                       "    this.#1 = new #2();\n" +
                       "    this.#1.init( this, #3, #3.getAnnotation( ValuePropertyCustomBinding.class ).params() );\n" +
                       "}\n" +
                       "\n" +
                       "this.#1.write( value );",
                       customBindingImplField.getName(), customBinding.getSimpleName(), property.name );
            
            mb.appendEmptyLine();
        }
        else if( treatExistenceAsValue )
        {
            mb.appendEmptyLine();
            
            mb.append( "final boolean elementShouldBePresent = Boolean.#1.toString().equals( value );\n" +
                       "\n" +
                       "if( elementShouldBePresent )\n" +
                       "{\n" +
                       "    getXmlElement( true ).getChildNode( #2, true );\n" +
                       "}\n" +
                       "else\n" +
                       "{\n" +
                       "    final XmlElement element = getXmlElement( false );\n" +
                       "    \n" +
                       "    if( element != null )\n" +
                       "    {\n" +
                       "        element.removeChildNode( #2 );\n" +
                       "    }\n" + 
                       "}",
                       ( valueWhenPresent == true ? "TRUE" : "FALSE" ), xmlPathConstant );

            mb.appendEmptyLine();
            
            elImplClass.addImport( XmlNode.class );
            elImplClass.addImport( XmlElement.class );
        }
        else if( xmlPathConstant == null )
        {
            mb.append( "getXmlElement( true ).setText( value );" );
        }
        else
        {
            mb.append( "getXmlElement( true ).setChildNodeText( #1, value, #2 );", xmlPathConstant, String.valueOf( removeNodeOnSetIfNull ) );
        }
    }
	
    @Override
    protected boolean prepareElementProperty( final InterfaceDeclaration elInterface,
                                              final ClassModel elImplClass,
                                              final PropertyFieldDeclaration property )
    {
        final XmlBinding xmlElementBinding = property.getAnnotation( XmlBinding.class );
        String xmlPathConstant = null;
        
        if( xmlElementBinding != null )
        {
            xmlPathConstant = processBasicXmlBinding( elImplClass, elInterface, property, xmlElementBinding.path() );
        }

        property.setData( DATA_XML_PATH_CONSTANT, xmlPathConstant );
        
        return true;
    }

    @Override
    protected void generateElementPropertyRefreshLogic( final InterfaceDeclaration elInterface,
                                                        final ClassModel elImplClass,
                                                        final PropertyFieldDeclaration property,
                                                        final Body mb )
    {
        final String xmlPathConstant = (String) property.getData( DATA_XML_PATH_CONSTANT );
        final String variableName = (String) property.getData( DATA_VARIABLE_NAME );
        final TypeReference implType = (TypeReference) property.getData( DATA_IMPL_TYPE );
        final boolean removableType = (Boolean) property.getData( DATA_ELEMENT_TYPE_REMOVABLE );
    
        if( removableType )
        {
            final FieldModel f = elImplClass.addField();
            f.setName( variableName + "Element" );
            f.setType( XmlElement.class );
        }
        
        mb.append( "final XmlElement el = getXmlElement( false );\n" +
                   "final XmlNode child = ( el == null ? null : el.getChildNode( #2, false ) );\n" +
                   "\n" +
                   "if( child == null )\n" +
                   "{\n" +
                   "    this.#1Element = null;\n" +
                   "}\n" +
                   "else\n" +
                   "{\n" +
                   "    if( ! child.equals( this.#1Element ) )\n" +
                   "    {\n" +
                   "        this.#1Element = (XmlElement) child;\n" +
                   "        element = new #3( this, #4, this.#1Element );\n" +
                   "    }\n" +
                   "    else\n" +
                   "    {\n" +
                   "        element = this.#1;\n" +
                   "    }\n" +
                   "}",
                   variableName, xmlPathConstant, implType.getSimpleName(), property.name );

        elImplClass.addImport( XmlNode.class );
    }

    @Override
    protected void generateElementPropertyCreateLogic( final InterfaceDeclaration elInterface,
                                                       final ClassModel elImplClass,
                                                       final PropertyFieldDeclaration property,
                                                       final Body mb )
    {
        final String xmlPathConstant = (String) property.getData( DATA_XML_PATH_CONSTANT );

        mb.append( "getXmlElement( true ).getChildNode( #1, true );", xmlPathConstant );
    }
    
    @Override
    protected void generateListPropertyInitLogic( final InterfaceDeclaration elInterface,
                                                  final ClassModel elImplClass,
                                                  final PropertyFieldDeclaration property,
                                                  final Body mb )
    {
        final String variableName = (String) property.getData( DATA_VARIABLE_NAME );
        final TypeReference memberType = (TypeReference) property.getData( DATA_LIST_MEMBER_TYPE );

        boolean xmlBindingFound = false;
        
        final ListPropertyXmlBinding xmlBindingAnnotation = property.getAnnotation( ListPropertyXmlBinding.class );
        
        if( xmlBindingAnnotation != null )
        {
            xmlBindingFound = true;
            
            mb.append( "this.#1 = new ModelElementList<#2>( this, #3 );", variableName, memberType.getSimpleName(), property.name );
            
            mb.appendEmptyLine();
            
            final String xmlPathConstant 
                = processBasicXmlBinding( elImplClass, elInterface, property, xmlBindingAnnotation.path() );
            
            final List<ElementMapping> xmlElementMappings = new ArrayList<ElementMapping>();
            
            for( ListPropertyXmlBindingMapping mapping : xmlBindingAnnotation.mappings() )
            {
                final ElementMapping m = new ElementMapping();
                
                m.element = mapping.element();
                
                try
                {
                    mapping.type();
                }
                catch( MirroredTypeException e )
                {
                    final InterfaceDeclaration typeMirror 
                        = ( (InterfaceType) e.getTypeMirror() ).getDeclaration();
                    
                    final GenerateXmlBinding generateImplAnnotation = typeMirror.getAnnotation( GenerateXmlBinding.class );
                    String implPackage = (generateImplAnnotation == null ? null : generateImplAnnotation.packageName());
                    
                    if( implPackage == null || implPackage.length() == 0 )
                    {
                        implPackage = typeMirror.getPackage().getQualifiedName() + ".internal";
                    }
                    
                    m.wrapperInterface = new TypeReference( typeMirror.getQualifiedName() );
                    elImplClass.addImport( m.wrapperInterface );
                    
                    m.wrapperImplementation = new TypeReference( implPackage, m.wrapperInterface.getSimpleName().substring( 1 ) );
                    elImplClass.addImport( m.wrapperImplementation );
                }

                xmlElementMappings.add( m );
            }
            
            if( xmlElementMappings.size() > 1 )
            {
                mb.append( "final Map<ModelElementType,String> typeToElementName = new java.util.HashMap<ModelElementType,String>();" );
                
                for( ElementMapping mapping : xmlElementMappings )
                {
                    mb.append( "typeToElementName.put( #1.TYPE, \"#2\" );", 
                               mapping.getWrapperInterface().getSimpleName(), mapping.getElement() );
                }
            }
            else
            {
                final ElementMapping mapping = xmlElementMappings.get( 0 );
                
                mb.append( "final Map<ModelElementType,String> typeToElementName = Collections.singletonMap( #1.TYPE, \"#2\" );",
                           mapping.getWrapperInterface().getSimpleName(), mapping.getElement() );
                
                elImplClass.addImport( Collections.class );
            }
            
            mb.appendEmptyLine();
            
            mb.append( "final ModelElementListControllerForXml<#1> controller = new ModelElementListControllerForXml<#1>( typeToElementName.values( ) )", memberType.getSimpleName() );
            mb.openBlock();
            
            mb.append( "@Override" );
            mb.append( "protected #1 wrap( final XmlElement element )", memberType.getSimpleName() );
            mb.openBlock();
            
            if( xmlElementMappings.size() > 1 )
            {
                mb.append( "final String elementName = element.getLocalName();" );
                mb.appendEmptyLine();
                
                boolean isFirstMapping = true;
                
                for( ElementMapping mapping : xmlElementMappings )
                {
                    mb.append( "#1if( elementName.equals( \"#2\" ) )\n" +
                               "{\n" +
                               "    return new #3( getList(), #4, element );\n" +
                               "}",
                               ( isFirstMapping ? "" : "else " ), mapping.getElement(), 
                               mapping.getWrapperImplementation().getSimpleName(), property.name );
                    
                    isFirstMapping = false;
                }
                
                mb.appendEmptyLine();
                mb.append( "throw new IllegalArgumentException();" );
            }
            else
            {
                mb.append( "return new #1( getList(), #2, element );", 
                           xmlElementMappings.get( 0 ).getWrapperImplementation().getSimpleName(),
                           property.name );
            }
            
            mb.closeBlock();
            mb.appendEmptyLine();
            
            mb.append( "@Override\n" +
                       "public #1 createNewElement( final ModelElementType type )\n" + 
                       "{\n" +
                       "    validateEdit();\n" +
                       "    final String elementName = typeToElementName.get( type );\n" +
                       "    return wrap( getParentXmlElement( true ).addChildElement( elementName ) );\n" +
                       "}",
                       memberType.getSimpleName() );
            
            mb.appendEmptyLine();
            
            mb.append( "@Override" );
            mb.append( "protected XmlElement getParentXmlElement( final boolean createIfNecessary )" );
            mb.openBlock();
            
            if( xmlPathConstant == null )
            {
                mb.append( "return getXmlElement( createIfNecessary );" );
            }
            else
            {
                mb.append( "XmlElement parent = getXmlElement( createIfNecessary );\n" +
                           "\n" +
                           "if( parent != null )\n" +
                           "{\n" +
                           "    parent = (XmlElement) parent.getChildNode( #1, createIfNecessary );\n" +
                           "}\n" +
                           "\n" +
                           "return parent;",
                           xmlPathConstant );
            }
            
            mb.closeBlock();

            if( xmlPathConstant != null )
            {
                mb.append( "@Override\n" +
                           "public void handleElementRemovedEvent()\n" + 
                           "{\n" +
                           "    XmlElement base = getXmlElement( false );\n" +
                           "    \n" +
                           "    if( base != null )\n" +
                           "    {\n" +
                           "        final XmlElement parent = (XmlElement) base.getChildNode( #1, false );\n" +
                           "        \n" +
                           "        if( parent != null && parent.isEmpty() )\n" +
                           "        {\n" +
                           "            base.removeChildNode( #1 );\n" +
                           "        }\n" + 
                           "    }\n" + 
                           "}",
                           xmlPathConstant );
            }
            
            mb.closeBlock( true );
            mb.appendEmptyLine();
            
            mb.append( "controller.init( this, #1, this.#2, new String[ 0 ] );", property.name, variableName );
            mb.append( "this.#1.init( controller );", variableName );
            
            elImplClass.addImport( Map.class );
            elImplClass.addImport( ModelElementType.class );
            elImplClass.addImport( ModelElementListControllerForXml.class );
            elImplClass.addImport( XmlElement.class );
        }
        
        if( ! xmlBindingFound )
        {
            final ListPropertyCustomBinding listPropertyCustomXmlBindingAnnotation
                = property.getAnnotation( ListPropertyCustomBinding.class );
            
            if( listPropertyCustomXmlBindingAnnotation != null )
            {
                TypeReference customBinding = null;
                
                try
                {
                    listPropertyCustomXmlBindingAnnotation.impl();
                }
                catch( MirroredTypeException e )
                {
                    final ClassDeclaration typeMirror 
                        = ( (ClassType) e.getTypeMirror() ).getDeclaration();
                    
                    customBinding = new TypeReference( typeMirror.getQualifiedName() );
                }
                
                mb.append( "this.#1 = new ModelElementList<#2>( this, #3 );",
                           variableName, memberType.getSimpleName(), property.name );
             
                mb.append( "final ModelElementListController<#1> controller = new #2();",
                		   memberType.getSimpleName(), customBinding.getSimpleName() );
                
                mb.append( "controller.init( this, #1, this.#2, #1.getAnnotation( ListPropertyCustomBinding.class ).params() );", property.name, variableName );
                mb.append( "this.#1.init( controller );", variableName );
                
                elImplClass.addImport( customBinding );
                elImplClass.addImport( ListPropertyCustomBinding.class );
                elImplClass.addImport( ModelElementListController.class );
            }
        }
    }

    private String processBasicXmlBinding( final ClassModel implClassModel,
                                           final InterfaceDeclaration interfaceDeclaration,
                                           final PropertyFieldDeclaration propField,
                                           final String xpath )
    {
        if( xpath.length() == 0 )
        {
            return null;
        }
        
        if( ! implClassModel.containsField( "NAMESPACE_RESOLVER" ) )
        {
            final FieldModel nsr = implClassModel.addConstant();
            nsr.setName( "NAMESPACE_RESOLVER" );
            nsr.setType( StandardXmlNamespaceResolver.class );
            nsr.setValue( "new StandardXmlNamespaceResolver( TYPE )" );
            
            final FieldModel map = implClassModel.addConstant();
            map.setName( "MAP_PROP_TO_XML_PATH" );
            map.setType( ( new TypeReference( Map.class ) ).parameterize( ModelProperty.class, XmlPath.class ) );
            map.setValue( "new HashMap<ModelProperty,XmlPath>()" );
            
            implClassModel.addImport( HashMap.class );
        }
        
        final FieldModel pf = implClassModel.addConstant();
        pf.setName( "PATH_" + propField.name.substring( 5 ) );
        pf.setType( XmlPath.class );
        pf.setValue( "new XmlPath( \"" + xpath + "\", NAMESPACE_RESOLVER )" );
        
        final StaticInitializerModel mapInit = implClassModel.getStaticInitializer( "propToXmlPathMapInit", true );
        mapInit.appendToBody( "MAP_PROP_TO_XML_PATH.put( " + propField.name + ", " + pf.getName() + " );" );
        
        return pf.getName();
    }

    private static final String normalizeToNull( final String str )
    {
        return ( (str.length() == 0) ? null : str );
    }
    
    private static final class ElementMapping
    {
        public String element;
        public TypeReference wrapperInterface;
        public TypeReference wrapperImplementation;
        
        public String getElement()
        {
            return this.element;
        }
        
        public TypeReference getWrapperInterface()
        {
            return this.wrapperInterface;
        }
        
        public TypeReference getWrapperImplementation()
        {
            return this.wrapperImplementation;
        }
    }
    
}