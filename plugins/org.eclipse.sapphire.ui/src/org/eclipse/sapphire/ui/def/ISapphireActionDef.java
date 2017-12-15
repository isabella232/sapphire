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

package org.eclipse.sapphire.ui.def;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Documentation;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NonNullValue;
import org.eclipse.sapphire.modeling.annotations.PossibleValues;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.serialization.ValueSerialization;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.ui.def.internal.KeySequenceValueSerializationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "action" )
@GenerateImpl

public interface ISapphireActionDef

    extends ISapphireActionSystemPartDef
    
{
    ModelElementType TYPE = new ModelElementType( ISapphireActionDef.class );
    
    // *** Id ***

    @NonNullValue
    @Label( standard = "ID", full = "action ID" )
    
    @Documentation( content = "An action ID serves to uniquely identify an action for a variety of purposes, " +
                              "such as defining an action handler or controlling ordering of actions via location " +
                              "hints. An ID only needs to be unique in the contexts where the action is expected to " +
                              "be used. To help avoid conflicts with other extensions, prefix the ID with company, " +
                              "organization or product name." )
    
    ValueProperty PROP_ID = new ValueProperty( TYPE, ISapphireActionSystemPartDef.PROP_ID );
    
    // *** Label ***
    
    @NonNullValue
    @Label( standard = "label", full = "action label" )
    
    @Documentation( content = "An action label is used when presenting the action to the user." +
                              "[pbr/]" +
                              "The exact way that the label is used is dependent on the presentation, but typical " +
                              "presentations use labels for menu item names and as tooltips." +
                              "[pbr/]" +
                              "A label should only capitalize words that must remain capitalized regardless of " +
                              "context (proper nouns and acronyms). Words that are not already capitalized will " +
                              "be capitalized as necessary depending on how the label is used by the presentation." )
    
    ValueProperty PROP_LABEL = new ValueProperty( TYPE, ISapphireActionSystemPartDef.PROP_LABEL );
    
    // *** Description ***
    
    @Documentation( content = "Provides more information about the action than what is available in the label. The " +
                              "description should be in the form of properly capitalized and punctuated sentences." )
    
    ValueProperty PROP_DESCRIPTION = new ValueProperty( TYPE, ISapphireActionSystemPartDef.PROP_DESCRIPTION );

    // *** KeyBinding ***
    
    @Type( base = SapphireKeySequence.class )
    @ValueSerialization( service = KeySequenceValueSerializationService.class )
    @Label( standard = "key binding" )
    @XmlBinding( path = "key-binding" )
    
    @Documentation( content = "A key binding defines how an action is accessed via the keyboard as a sequence " +
                              "of key presses. Separate multiple keys in a sequence with a '+' (plus) character." +
                              "[pbr/]" +
                              "Example: CONTROL+SHIFT+A" +
                              "[pbr/]" +
                              "Legal Keys" +
                              "[ul]" +
                              "[li]Any character from ASCII.[/li]" +
                              "[li]ALT[/li]" +
                              "[li]CONTROL[/li]" +
                              "[li]SHIFT[/li]" +
                              "[li]DEL[/li]" + 
                              "[li]ARROW_UP[/li]" +
                              "[li]ARROW_DOWN[/li]" +
                              "[/ul]" )
    
    ValueProperty PROP_KEY_BINDING = new ValueProperty( TYPE, "KeyBinding" );
    
    Value<SapphireKeySequence> getKeyBinding();
    void setKeyBinding( String value );
    void setKeyBinding( SapphireKeySequence value );
    
    // *** Type ***
    
    @Type( base = SapphireActionType.class )
    @Label( standard = "type", full = "action type" )
    @DefaultValue( text = "push" )
    @XmlBinding( path = "type" )
    
    @Documentation( content = "An action can be a push action or a toggle action. Unlike a push action, a toggle " +
                              "action has distinct on/off states that are made visible by the presentation." +
                              "[pbr/]" +
                              "The exact method for how a toggle is presented is dependent on the presentation, but " +
                              "typical presentations use check marks next to menu items or recessed state for buttons." )
    
    ValueProperty PROP_TYPE = new ValueProperty( TYPE, "Type" );
    
    Value<SapphireActionType> getType();
    void setType( String value );
    void setType( SapphireActionType value );
    
    // *** Images ***
    
    @Documentation( content = "One or more images can be associated with an action to be used by the presentation " +
                              "as appropriate. If an image is necessary, the presentation will look for an image " +
                              "of the appropriate size." )
    
    ListProperty PROP_IMAGES = new ListProperty( TYPE, ISapphireActionSystemPartDef.PROP_IMAGES );

    // *** ConditionClass ***
    
    @Documentation( content = "A condition allows use of arbitrary logic to control whether the action is going to be " +
                              "available or not in a given situation. Conditions must extends SapphireCondition class." )

    ValueProperty PROP_CONDITION_CLASS = new ValueProperty( TYPE, ISapphireActionSystemPartDef.PROP_CONDITION_CLASS );

    // *** Contexts ***
    
    @Documentation( content = "Every UI part that supports actions will define one or more context. An action can be " +
                              "constrained to apply only to the specified contexts. If no context is specified, the " +
                              "action will be treated as applicable to all contexts." )

    ListProperty PROP_CONTEXTS = new ListProperty( TYPE, ISapphireActionSystemPartDef.PROP_CONTEXTS );

    // *** Group ***
    
    @Label( standard = "group" )
    @PossibleValues( property = "../Actions/Group", invalidValueSeverity = IStatus.OK ) // TODO: Bug. This isn't working.
    @XmlBinding( path = "group" )
    
    @Documentation( content = "Specifies the ID of the group that this action belongs to, if any. Groups " +
                              "are used to visually segregate actions. Groups are created on first use. They do " +
                              "not need to be explicitly defined." +
                              "[pbr/]" +
                              "The exact way to segregate groups is dependent on the presentation, but typical " +
                              "presentations use separator lines." )
    
    ValueProperty PROP_GROUP = new ValueProperty( TYPE, "Group" );
    
    Value<String> getGroup();
    void setGroup( String value );
    
    // *** LocationHints ***
    
    @Documentation( content = "Location hints are used to arrange actions in relation to each other. " +
                              "The location hints are expressed in terms of preference for whether this action should " + 
                              "appear before or after another action. Multiple hints can be specified to precisely " +
                              "position an action." )

    ListProperty PROP_LOCATION_HINTS = new ListProperty( TYPE, ISapphireActionSystemPartDef.PROP_LOCATION_HINTS );

}
