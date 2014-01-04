/******************************************************************************
 * Copyright (c) 2014 Oracle and Accenture
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 *    Ling Hao - [329114] rewrite context help binding feature
 *    Shenxue Zhou - [330482] support diagram editing in Sapphire UI
 *    Kamesh Sampath - [355751] General improvement of XML root binding API
 ******************************************************************************/

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.ui.def.internal.SapphireUiDefMethods;
import org.eclipse.sapphire.ui.diagram.def.DiagramEditorPageDef;
import org.eclipse.sapphire.ui.forms.ActuatorDef;
import org.eclipse.sapphire.ui.forms.CompositeDef;
import org.eclipse.sapphire.ui.forms.CustomFormComponentDef;
import org.eclipse.sapphire.ui.forms.DialogDef;
import org.eclipse.sapphire.ui.forms.FormDef;
import org.eclipse.sapphire.ui.forms.FormEditorPageDef;
import org.eclipse.sapphire.ui.forms.GroupDef;
import org.eclipse.sapphire.ui.forms.HtmlPanelDef;
import org.eclipse.sapphire.ui.forms.LineSeparatorDef;
import org.eclipse.sapphire.ui.forms.MasterDetailsContentNodeDef;
import org.eclipse.sapphire.ui.forms.MasterDetailsContentNodeFactoryDef;
import org.eclipse.sapphire.ui.forms.MasterDetailsEditorPageDef;
import org.eclipse.sapphire.ui.forms.DetailSectionDef;
import org.eclipse.sapphire.ui.forms.PropertyEditorDef;
import org.eclipse.sapphire.ui.forms.SectionDef;
import org.eclipse.sapphire.ui.forms.SplitFormDef;
import org.eclipse.sapphire.ui.forms.StaticTextFieldDef;
import org.eclipse.sapphire.ui.forms.TabGroupDef;
import org.eclipse.sapphire.ui.forms.TextDef;
import org.eclipse.sapphire.ui.forms.SpacerDef;
import org.eclipse.sapphire.ui.forms.WithDef;
import org.eclipse.sapphire.ui.forms.WizardDef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a> 
 */

@XmlBinding( path = "definition" )

public interface ISapphireUiDef extends Element
{
    ElementType TYPE = new ElementType( ISapphireUiDef.class );
    
    // *** ImportedPackages ***
    
    @Type( base = IPackageReference.class )
    @Label( standard = "imported packages" )
    @XmlListBinding( path = "import", mappings = @XmlListBinding.Mapping( element = "package", type = IPackageReference.class ) )
    
    ListProperty PROP_IMPORTED_PACKAGES = new ListProperty( TYPE, "ImportedPackages" );
    
    ElementList<IPackageReference> getImportedPackages();
    
    // *** ImportedDefinitions ***
    
    @Type( base = IDefinitionReference.class )
    @Label( standard = "imported definitions" )
    @XmlListBinding( path = "import", mappings = @XmlListBinding.Mapping( element = "definition", type = IDefinitionReference.class ) )
    
    ListProperty PROP_IMPORTED_DEFINITIONS = new ListProperty( TYPE, "ImportedDefinitions" );
    
    ElementList<IDefinitionReference> getImportedDefinitions();
    
    // *** DocumentationDefs ***
    
    @Type( base = ISapphireDocumentationDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "documentation", type = ISapphireDocumentationDef.class ) )
                             
    ListProperty PROP_DOCUMENTATION_DEFS = new ListProperty( TYPE, "DocumentationDefs" );
    
    ElementList<ISapphireDocumentationDef> getDocumentationDefs();
    
    // *** Method : getDocumentationDef ***
    
    @DelegateImplementation( SapphireUiDefMethods.class )
    
    ISapphireDocumentationDef getDocumentationDef( String id,
                                                   boolean searchImportedDefinitions );

    // *** PartDefs ***
    
    @Type
    ( 
        base = PartDef.class,
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
            FormDef.class,
            SplitFormDef.class,
            MasterDetailsContentNodeDef.class,
            MasterDetailsContentNodeFactoryDef.class,
            MasterDetailsEditorPageDef.class,
            DiagramEditorPageDef.class,
            DialogDef.class,
            WizardDef.class,
            FormEditorPageDef.class,
            SectionDef.class
        }
    )
                      
    @XmlListBinding( path = "" )
                             
    ListProperty PROP_PART_DEFS = new ListProperty( TYPE, "PartDefs" );
    
    ElementList<PartDef> getPartDefs();
    
    // *** Method : getPartDef ***
    
    @DelegateImplementation( SapphireUiDefMethods.class )
    
    PartDef getPartDef( String id,
                                 boolean searchImportedDefinitions,
                                 Class<?> expectedType );
    
    // *** Method : resolveClass ***
    
    @DelegateImplementation( SapphireUiDefMethods.class )
    
    Class<?> resolveClass( String className );
    
    // *** Method : resolveProperty ***
    
    @DelegateImplementation( SapphireUiDefMethods.class )
    
    PropertyDef resolveProperty( String qualifiedPropertyName );
    
}
