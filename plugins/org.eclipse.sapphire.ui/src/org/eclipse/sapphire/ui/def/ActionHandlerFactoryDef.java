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

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.ReferenceValue;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.JavaTypeConstraint;
import org.eclipse.sapphire.java.JavaTypeKind;
import org.eclipse.sapphire.java.JavaTypeName;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.Documentation;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.LongString;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.annotations.Whitespace;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlValueBinding;
import org.eclipse.sapphire.ui.def.internal.SapphireActionHandlerFactoryDefMethods;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "action handler factory" )
@Image( path = "ActionHandlerFactoryDef.png" )

public interface ActionHandlerFactoryDef extends ISapphireConditionHostDef, ActionContextsHostDef
{
    ElementType TYPE = new ElementType( ActionHandlerFactoryDef.class );
    
    // *** Action ***
    
    @Label( standard = "action" )
    @Required
    //@PossibleValuesFromModel( path = "/Actions/Id", invalidValueSeverity = IStatus.OK )
    @XmlBinding( path = "action" )
    
    @Documentation( content = "The ID of the action that this factory is to provide handlers for." )
    
    ValueProperty PROP_ACTION = new ValueProperty( TYPE, "Action" );
    
    Value<String> getAction();
    void setAction( String value );
    
    // *** Description ***
    
    @LongString
    @Label( standard = "description" )
    @Whitespace( collapse = true )
    @XmlValueBinding( path = "description" )
    
    @Documentation
    ( 
        content = "Provides information about the action handler factory. The " +
                  "description should be in the form of properly capitalized and punctuated sentences."
    )
    
    ValueProperty PROP_DESCRIPTION = new ValueProperty( TYPE, "Description" );
    
    Value<String> getDescription();
    void setDescription( String value );
    
    // *** ImplClass ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @Label( standard = "implementation class" )
    @Required
    @JavaTypeConstraint( kind = JavaTypeKind.CLASS, type = "org.eclipse.sapphire.ui.SapphireActionHandlerFactory" )
    @MustExist
    @XmlBinding( path = "impl" )
    
    @Documentation( content = "The action handler factory implementation class. Must extend SapphireActionHandlerFactory." )

    ValueProperty PROP_IMPL_CLASS = new ValueProperty( TYPE, "ImplClass" );
    
    ReferenceValue<JavaTypeName,JavaType> getImplClass();
    void setImplClass( String value );
    void setImplClass( JavaTypeName value );
    
    // *** Params ***
    
    @Type( base = ISapphireParam.class )
    @Label( standard = "params" )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "param", type = ISapphireParam.class ) )
    
    @Documentation( content = "Parameters that can be interpreted by the action handler factory." )
    
    ListProperty PROP_PARAMS = new ListProperty( TYPE, "Params" );
    
    ElementList<ISapphireParam> getParams();
    
    // *** Method: getParam ***
    
    @DelegateImplementation( SapphireActionHandlerFactoryDefMethods.class )
    
    String getParam( String name );
    
    // *** ConditionClass ***
    
    @Documentation
    ( 
        content = "A condition allows use of arbitrary logic to control whether the action handler factory is going to be " +
                  "available or not in a given situation. Conditions must extends SapphireCondition class."
    )

    ValueProperty PROP_CONDITION_CLASS = new ValueProperty( TYPE, ActionSystemPartDef.PROP_CONDITION_CLASS );

    // *** Contexts ***
    
    @Documentation
    ( 
        content = "Every UI part that supports actions will define one or more context. An action handler factory can be " +
                  "constrained to apply only to the specified contexts. If no context is specified, the " +
                  "action handler factory will be treated as applicable to all contexts." 
    )

    ListProperty PROP_CONTEXTS = new ListProperty( TYPE, ActionSystemPartDef.PROP_CONTEXTS );
    
}
