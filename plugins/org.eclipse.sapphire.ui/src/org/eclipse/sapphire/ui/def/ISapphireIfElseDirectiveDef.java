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

import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ReferenceValue;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.GenerateXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBindingMapping;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.ui.def.internal.ClassReferenceResolver;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "if" )
@GenerateXmlBinding

public interface ISapphireIfElseDirectiveDef

    extends ISapphirePartDef
    
{
    ModelElementType TYPE = new ModelElementType( ISapphireIfElseDirectiveDef.class );
 
    // *** ConditionClass ***
    
    @Reference( target = Class.class, resolver = ClassReferenceResolver.class )
    @Label( standard = "condition class" )
    @XmlBinding( path = "condition/class" )
    
    ValueProperty PROP_CONDITION_CLASS = new ValueProperty( TYPE, "ConditionClass" );
    
    ReferenceValue<Class<?>> getConditionClass();
    void setConditionClass( String conditionClass );
    
    // *** ConditionParameter ***
    
    @Label( standard = "condition parameter" )
    @XmlBinding( path = "condition/parameter" )
    
    ValueProperty PROP_CONDITION_PARAMETER = new ValueProperty( TYPE, "ConditionParameter" );
    
    Value<String> getConditionParameter();
    void setConditionParameter( String conditionParameter );
    
    // *** ThenContent ***
    
    @Type
    ( 
        base = ISapphirePartDef.class,
        possible = 
        { 
            ISapphirePropertyEditorDef.class, 
            ISapphireSeparatorDef.class,
            ISapphireSpacerDef.class,
            ISapphireLabelDef.class,
            ISapphireGroupDef.class,
            ISapphireWithDirectiveDef.class,
            ISapphireIfElseDirectiveDef.class,
            ISapphireCompositeDef.class,
            ISapphireCompositeRef.class,
            ISapphireActionLinkDef.class,
            ISapphireCustomPartDef.class,
            ISapphireStaticTextFieldDef.class,
            ISapphireElementPropertyCompositeDef.class,
            ISapphirePageBookExtDef.class,
            ISapphireTabGroupDef.class
        }
    )
                      
    @ListPropertyXmlBinding
    ( 
        path = "then",
        mappings =
        {
            @ListPropertyXmlBindingMapping( element = "property-editor", type = ISapphirePropertyEditorDef.class ),
            @ListPropertyXmlBindingMapping( element = "separator", type = ISapphireSeparatorDef.class ),
            @ListPropertyXmlBindingMapping( element = "spacer", type = ISapphireSpacerDef.class ),
            @ListPropertyXmlBindingMapping( element = "label", type = ISapphireLabelDef.class ),
            @ListPropertyXmlBindingMapping( element = "group", type = ISapphireGroupDef.class ),
            @ListPropertyXmlBindingMapping( element = "with", type = ISapphireWithDirectiveDef.class ),
            @ListPropertyXmlBindingMapping( element = "if", type = ISapphireIfElseDirectiveDef.class ),
            @ListPropertyXmlBindingMapping( element = "composite", type = ISapphireCompositeDef.class ),
            @ListPropertyXmlBindingMapping( element = "composite-ref", type = ISapphireCompositeRef.class ),
            @ListPropertyXmlBindingMapping( element = "action-link", type = ISapphireActionLinkDef.class ),
            @ListPropertyXmlBindingMapping( element = "custom", type = ISapphireCustomPartDef.class ),
            @ListPropertyXmlBindingMapping( element = "read-only-text", type = ISapphireStaticTextFieldDef.class ),
            @ListPropertyXmlBindingMapping( element = "element-property-composite", type = ISapphireElementPropertyCompositeDef.class ),
            @ListPropertyXmlBindingMapping( element = "switching-panel", type = ISapphirePageBookExtDef.class ),
            @ListPropertyXmlBindingMapping( element = "tab-group", type = ISapphireTabGroupDef.class )
        }
    )
                             
    ListProperty PROP_THEN_CONTENT = new ListProperty( TYPE, "ThenContent" );
    
    ModelElementList<ISapphirePartDef> getThenContent();
    
    // *** ElseContent ***
    
    @Type
    ( 
        base = ISapphirePartDef.class,
        possible = 
        { 
            ISapphirePropertyEditorDef.class, 
            ISapphireSeparatorDef.class,
            ISapphireSpacerDef.class,
            ISapphireLabelDef.class,
            ISapphireGroupDef.class,
            ISapphireWithDirectiveDef.class,
            ISapphireIfElseDirectiveDef.class,
            ISapphireCompositeDef.class,
            ISapphireCompositeRef.class,
            ISapphireActionLinkDef.class,
            ISapphireCustomPartDef.class,
            ISapphireStaticTextFieldDef.class,
            ISapphireElementPropertyCompositeDef.class,
            ISapphirePageBookExtDef.class,
            ISapphireTabGroupDef.class
        }
    )
                      
    @ListPropertyXmlBinding
    ( 
        path = "else",
        mappings =
        {
            @ListPropertyXmlBindingMapping( element = "property-editor", type = ISapphirePropertyEditorDef.class ),
            @ListPropertyXmlBindingMapping( element = "separator", type = ISapphireSeparatorDef.class ),
            @ListPropertyXmlBindingMapping( element = "spacer", type = ISapphireSpacerDef.class ),
            @ListPropertyXmlBindingMapping( element = "label", type = ISapphireLabelDef.class ),
            @ListPropertyXmlBindingMapping( element = "group", type = ISapphireGroupDef.class ),
            @ListPropertyXmlBindingMapping( element = "with", type = ISapphireWithDirectiveDef.class ),
            @ListPropertyXmlBindingMapping( element = "if", type = ISapphireIfElseDirectiveDef.class ),
            @ListPropertyXmlBindingMapping( element = "composite", type = ISapphireCompositeDef.class ),
            @ListPropertyXmlBindingMapping( element = "composite-ref", type = ISapphireCompositeRef.class ),
            @ListPropertyXmlBindingMapping( element = "action-link", type = ISapphireActionLinkDef.class ),
            @ListPropertyXmlBindingMapping( element = "custom", type = ISapphireCustomPartDef.class ),
            @ListPropertyXmlBindingMapping( element = "read-only-text", type = ISapphireStaticTextFieldDef.class ),
            @ListPropertyXmlBindingMapping( element = "element-property-composite", type = ISapphireElementPropertyCompositeDef.class ),
            @ListPropertyXmlBindingMapping( element = "switching-panel", type = ISapphirePageBookExtDef.class ),
            @ListPropertyXmlBindingMapping( element = "tab-group", type = ISapphireTabGroupDef.class )
        }
    )
                             
    ListProperty PROP_ELSE_CONTENT = new ListProperty( TYPE, "ElseContent" );
    
    ModelElementList<ISapphirePartDef> getElseContent();
    
}
