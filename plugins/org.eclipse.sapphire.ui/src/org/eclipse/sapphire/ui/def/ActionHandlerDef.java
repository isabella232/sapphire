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
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.ui.def.internal.SapphireActionHandlerDefMethods;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "action handler" )
@Image( path = "ActionHandlerDef.png" )

public interface ActionHandlerDef extends ActionSystemPartDef
{
    ElementType TYPE = new ElementType( ActionHandlerDef.class );
    
    // *** Action ***
    
    @Label( standard = "action" )
    @Required
    //@PossibleValuesFromModel( path = "/Actions/Id", invalidValueSeverity = IStatus.OK )
    @XmlBinding( path = "action" )
    
    @Documentation( content = "The ID of the action that this handler is for." )
    
    ValueProperty PROP_ACTION = new ValueProperty( TYPE, "Action" );
    
    Value<String> getAction();
    void setAction( String value );
    
    // *** Id ***

    @Label( standard = "ID", full = "action handler ID" )
    
    @Documentation
    ( 
        content = "An action handler ID serves to uniquely identify an action for a variety of purposes, " +
                  "such as controlling ordering of action handlers via location " +
                  "hints. An ID only needs to be unique in the contexts where the action handler is expected to " +
                  "be used. To help avoid conflicts with other extensions, prefix the ID with company, " +
                  "organization or product name." +
                  "[pbr/]" +
                  "Note that action handler ID can also be set in the implementation."
    )
    
    ValueProperty PROP_ID = new ValueProperty( TYPE, ActionSystemPartDef.PROP_ID );
    
    // *** Label ***
    
    @Label( standard = "label", full = "action handler label" )
    
    @Documentation
    ( 
        content = "An action handler label is used when presenting the action handler to the user." +
                  "[pbr/]" +
                  "The exact way that the label is used is dependent on the presentation, but typical " +
                  "presentations use labels for menu item names and as tooltips." +
                  "[pbr/]" +
                  "A label should only capitalize words that must remain capitalized regardless of " +
                  "context (proper nouns and acronyms). Words that are not already capitalized will " +
                  "be capitalized as necessary depending on how the label is used by the presentation." +
                  "[pbr/]" +
                  "Note that action handler label can also be set in the implementation."
    )
    
    ValueProperty PROP_LABEL = new ValueProperty( TYPE, ActionSystemPartDef.PROP_LABEL );
    
    // *** Images ***
    
    @Documentation
    (
        content = "One or more images can be associated with an action handler to be used by the presentation " +
                  "as appropriate. If an image is necessary, the presentation will look for an image " +
                  "of the appropriate size." +
                  "[pbr/]" +
                  "Note that images can also be set in the implementation."
    )
    
    ListProperty PROP_IMAGES = new ListProperty( TYPE, ActionSystemPartDef.PROP_IMAGES );
    
    // *** Description ***
    
    @Documentation
    (
        content = "Provides more information about the action handler than what is available in the label. The " +
                  "description should be in the form of properly capitalized and punctuated sentences."
    )
    
    ValueProperty PROP_DESCRIPTION = new ValueProperty( TYPE, ActionSystemPartDef.PROP_DESCRIPTION );

    // *** ImplClass ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @Label( standard = "implementation class" )
    @Required
    @JavaTypeConstraint( kind = JavaTypeKind.CLASS, type = "org.eclipse.sapphire.ui.SapphireActionHandler" )
    @MustExist
    @XmlBinding( path = "impl" )
    
    @Documentation( content = "The action handler implementation class. Must extend SapphireActionHandler." )
    
    ValueProperty PROP_IMPL_CLASS = new ValueProperty( TYPE, "ImplClass" );
    
    ReferenceValue<JavaTypeName,JavaType> getImplClass();
    void setImplClass( String value );
    void setImplClass( JavaTypeName value );
    
    // *** Params ***
    
    @Type( base = ISapphireParam.class )
    @Label( standard = "params" )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "param", type = ISapphireParam.class ) )
    
    @Documentation( content = "Parameters that can be interpreted by the action handler." )
    
    ListProperty PROP_PARAMS = new ListProperty( TYPE, "Params" );
    
    ElementList<ISapphireParam> getParams();
    
    // *** Method: getParam ***
    
    @DelegateImplementation( SapphireActionHandlerDefMethods.class )
    
    String getParam( String name );
    
    // *** ConditionClass ***
    
    @Documentation
    ( 
        content = "A condition allows use of arbitrary logic to control whether the action handler is going to be " +
                  "available or not in a given situation. Conditions must extends SapphireCondition class."
    )

    ValueProperty PROP_CONDITION_CLASS = new ValueProperty( TYPE, ActionSystemPartDef.PROP_CONDITION_CLASS );

    // *** Contexts ***
    
    @Documentation
    ( 
        content = "Every UI part that supports actions will define one or more context. An action handler can be " +
                  "constrained to apply only to the specified contexts. If no context is specified, the " +
                  "action handler will be treated as applicable to all contexts."
    )

    ListProperty PROP_CONTEXTS = new ListProperty( TYPE, ActionSystemPartDef.PROP_CONTEXTS );

    // *** LocationHints ***
    
    @Documentation
    ( 
        content = "Location hints are used to arrange action handlers in relation to each other. " +
                  "The location hints are expressed in terms of preference for whether this action handler should " + 
                  "appear before or after another action handler. Multiple hints can be specified to precisely " +
                  "position an action handler." +
                  "[pbr/]" +
                  "Note that ordering is particularly important for action handlers as certain presentations " +
                  "are only capable of presenting one handler for an action. Those presentations will use the first " +
                  "handler from the ordering imposed by the location hints."
    )

    ListProperty PROP_LOCATION_HINTS = new ListProperty( TYPE, ActionSystemPartDef.PROP_LOCATION_HINTS );

}
