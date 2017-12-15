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
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBindingMapping;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public interface ISapphirePartContainerDef

    extends ISapphirePartDef
    
{
    ModelElementType TYPE = new ModelElementType( ISapphirePartContainerDef.class );
    
    // *** Content ***
    
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
        path = "content",
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
                             
    ListProperty PROP_CONTENT = new ListProperty( TYPE, "Content" ); //$NON-NLS-1$
    
    ModelElementList<ISapphirePartDef> getContent();
    
}
