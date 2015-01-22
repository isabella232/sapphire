/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms;

import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.ui.def.EditorPageDef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "form editor page" )
@Image( path = "FormEditorPageDef.png" )
@XmlBinding( path = "form-editor-page" )

public interface FormEditorPageDef extends EditorPageDef
{
    ElementType TYPE = new ElementType(FormEditorPageDef.class);

    // *** Content ***
    
    @Type
    ( 
        base = FormComponentDef.class,
        possible = 
        { 
            PropertyEditorDef.class, 
            LineSeparatorDef.class,
            SpacerDef.class,
            TextDef.class,
            GroupDef.class,
            WithDef.class,
            CompositeDef.class,
            ActuatorDef.class,
            CustomFormComponentDef.class,
            StaticTextFieldDef.class,
            DetailSectionDef.class,
            TabGroupDef.class,
            HtmlPanelDef.class,
            FormComponentRef.class,
            FormDef.class,
            SplitFormDef.class,
            SectionDef.class
        }
    )
                      
    @XmlListBinding( path = "content" )
                             
    ListProperty PROP_CONTENT = new ListProperty( TYPE, "Content" );
    
    ElementList<FormComponentDef> getContent();

}
