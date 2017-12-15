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

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "form" )
@GenerateImpl

public interface FormDef extends FormComponentDef
{
    ModelElementType TYPE = new ModelElementType( FormDef.class );
    
    // *** Content ***
    
    @Type
    ( 
        base = FormComponentDef.class,
        possible = 
        { 
            PropertyEditorDef.class, 
            LineSeparatorDef.class,
            WhitespaceSeparatorDef.class,
            ISapphireLabelDef.class,
            ISapphireGroupDef.class,
            ISapphireWithDirectiveDef.class,
            ConditionalDef.class,
            CompositeDef.class,
            ActuatorDef.class,
            ISapphireCustomPartDef.class,
            ISapphireStaticTextFieldDef.class,
            PageBookExtDef.class,
            TabGroupDef.class,
            HtmlPanelDef.class,
            IFormPartInclude.class,
            FormDef.class,
            SplitFormDef.class,
            SectionDef.class
        }
    )
                      
    @XmlListBinding
    ( 
        path = "content",
        mappings =
        {
            @XmlListBinding.Mapping( element = "property-editor", type = PropertyEditorDef.class ),
            @XmlListBinding.Mapping( element = "separator", type = LineSeparatorDef.class ),
            @XmlListBinding.Mapping( element = "spacer", type = WhitespaceSeparatorDef.class ),
            @XmlListBinding.Mapping( element = "label", type = ISapphireLabelDef.class ),
            @XmlListBinding.Mapping( element = "group", type = ISapphireGroupDef.class ),
            @XmlListBinding.Mapping( element = "with", type = ISapphireWithDirectiveDef.class ),
            @XmlListBinding.Mapping( element = "if", type = ConditionalDef.class ),
            @XmlListBinding.Mapping( element = "composite", type = CompositeDef.class ),
            @XmlListBinding.Mapping( element = "actuator", type = ActuatorDef.class ),
            @XmlListBinding.Mapping( element = "custom", type = ISapphireCustomPartDef.class ),
            @XmlListBinding.Mapping( element = "read-only-text", type = ISapphireStaticTextFieldDef.class ),
            @XmlListBinding.Mapping( element = "switching-panel", type = PageBookExtDef.class ),
            @XmlListBinding.Mapping( element = "tab-group", type = TabGroupDef.class ),
            @XmlListBinding.Mapping( element = "html", type = HtmlPanelDef.class ),
            @XmlListBinding.Mapping( element = "include", type = IFormPartInclude.class ),
            @XmlListBinding.Mapping( element = "form", type = FormDef.class ),
            @XmlListBinding.Mapping( element = "split-form", type = SplitFormDef.class ),
            @XmlListBinding.Mapping( element = "section", type = SectionDef.class )
        }
    )
                             
    ListProperty PROP_CONTENT = new ListProperty( TYPE, "Content" );
    
    ModelElementList<FormComponentDef> getContent();
    
}
