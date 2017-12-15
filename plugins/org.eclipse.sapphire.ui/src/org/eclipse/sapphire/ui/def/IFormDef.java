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
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "form" )
@GenerateImpl

public interface IFormDef

    extends IFormPartDef
    
{
    ModelElementType TYPE = new ModelElementType( IFormDef.class );
    
    // *** Content ***
    
    @Type
    ( 
        base = IFormPartDef.class,
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
            ISapphireActionLinkDef.class,
            ISapphireCustomPartDef.class,
            ISapphireStaticTextFieldDef.class,
            ISapphirePageBookExtDef.class,
            ISapphireTabGroupDef.class,
            ISapphireHtmlPanelDef.class,
            IFormPartInclude.class,
            IFormDef.class
        }
    )
                      
    @XmlListBinding
    ( 
        path = "content",
        mappings =
        {
            @XmlListBinding.Mapping( element = "property-editor", type = ISapphirePropertyEditorDef.class ),
            @XmlListBinding.Mapping( element = "separator", type = ISapphireSeparatorDef.class ),
            @XmlListBinding.Mapping( element = "spacer", type = ISapphireSpacerDef.class ),
            @XmlListBinding.Mapping( element = "label", type = ISapphireLabelDef.class ),
            @XmlListBinding.Mapping( element = "group", type = ISapphireGroupDef.class ),
            @XmlListBinding.Mapping( element = "with", type = ISapphireWithDirectiveDef.class ),
            @XmlListBinding.Mapping( element = "if", type = ISapphireIfElseDirectiveDef.class ),
            @XmlListBinding.Mapping( element = "composite", type = ISapphireCompositeDef.class ),
            @XmlListBinding.Mapping( element = "action-link", type = ISapphireActionLinkDef.class ),
            @XmlListBinding.Mapping( element = "custom", type = ISapphireCustomPartDef.class ),
            @XmlListBinding.Mapping( element = "read-only-text", type = ISapphireStaticTextFieldDef.class ),
            @XmlListBinding.Mapping( element = "switching-panel", type = ISapphirePageBookExtDef.class ),
            @XmlListBinding.Mapping( element = "tab-group", type = ISapphireTabGroupDef.class ),
            @XmlListBinding.Mapping( element = "html", type = ISapphireHtmlPanelDef.class ),
            @XmlListBinding.Mapping( element = "include", type = IFormPartInclude.class ),
            @XmlListBinding.Mapping( element = "form", type = IFormDef.class )
        }
    )
                             
    ListProperty PROP_CONTENT = new ListProperty( TYPE, "Content" );
    
    ModelElementList<IFormPartDef> getContent();
    
}
