/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [329114] rewrite context help binding feature
 *    Shenxue Zhou - [330482] support diagram editing in Sapphire UI
 ******************************************************************************/

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlRootBinding;
import org.eclipse.sapphire.ui.def.internal.SapphireUiDefMethods;
import org.eclipse.sapphire.ui.diagram.def.IDiagramEditorPageDef;
import org.eclipse.sapphire.ui.form.editors.masterdetails.def.IMasterDetailsContentNodeDef;
import org.eclipse.sapphire.ui.form.editors.masterdetails.def.IMasterDetailsContentNodeFactoryDef;
import org.eclipse.sapphire.ui.form.editors.masterdetails.def.IMasterDetailsEditorPageDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl
@XmlRootBinding( elementName = "definition" )

public interface ISapphireUiDef

    extends IModelElement
    
{
    ModelElementType TYPE = new ModelElementType( ISapphireUiDef.class );
    
    // *** ImportedPackages ***
    
    @Type( base = IPackageReference.class )
    @Label( standard = "imported packages" )
    @XmlListBinding( path = "import", mappings = @XmlListBinding.Mapping( element = "package", type = IPackageReference.class ) )
    
    ListProperty PROP_IMPORTED_PACKAGES = new ListProperty( TYPE, "ImportedPackages" );
    
    ModelElementList<IPackageReference> getImportedPackages();
    
    // *** ImportedDefinitions ***
    
    @Type( base = IDefinitionReference.class )
    @Label( standard = "imported definitions" )
    @XmlListBinding( path = "import", mappings = @XmlListBinding.Mapping( element = "definition", type = IDefinitionReference.class ) )
    
    ListProperty PROP_IMPORTED_DEFINITIONS = new ListProperty( TYPE, "ImportedDefinitions" );
    
    ModelElementList<IDefinitionReference> getImportedDefinitions();
    
    // *** DocumentationDefs ***
    
    @Type( base = ISapphireDocumentationDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "documentation", type = ISapphireDocumentationDef.class ) )
                             
    ListProperty PROP_DOCUMENTATION_DEFS = new ListProperty( TYPE, "DocumentationDefs" );
    
    ModelElementList<ISapphireDocumentationDef> getDocumentationDefs();
    
    // *** Method : getDocumentationDef ***
    
    @DelegateImplementation( SapphireUiDefMethods.class )
    
    ISapphireDocumentationDef getDocumentationDef( String id,
                                                   boolean searchImportedDefinitions );

    // *** PartDefs ***
    
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
            ISapphireActionLinkDef.class,
            ISapphireCustomPartDef.class,
            ISapphireStaticTextFieldDef.class,
            ISapphirePageBookExtDef.class,
            ISapphireTabGroupDef.class,
            ISapphireHtmlPanelDef.class,
            IFormDef.class,
            IMasterDetailsContentNodeDef.class,
            IMasterDetailsContentNodeFactoryDef.class,
            IMasterDetailsEditorPageDef.class,
            IDiagramEditorPageDef.class,
            ISapphireDialogDef.class,
            ISapphireWizardDef.class
        }
    )
                      
    @XmlListBinding
    ( 
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
            @XmlListBinding.Mapping( element = "form", type = IFormDef.class ),
            @XmlListBinding.Mapping( element = "node", type = IMasterDetailsContentNodeDef.class ),
            @XmlListBinding.Mapping( element = "node-factory", type = IMasterDetailsContentNodeFactoryDef.class ),
            @XmlListBinding.Mapping( element = "editor-page", type = IMasterDetailsEditorPageDef.class ),
            @XmlListBinding.Mapping( element = "diagram-page", type = IDiagramEditorPageDef.class ),
            @XmlListBinding.Mapping( element = "dialog", type = ISapphireDialogDef.class ),
            @XmlListBinding.Mapping( element = "wizard", type = ISapphireWizardDef.class )
        }
    )
                             
    ListProperty PROP_PART_DEFS = new ListProperty( TYPE, "PartDefs" );
    
    ModelElementList<ISapphirePartDef> getPartDefs();
    
    // *** Method : getPartDef ***
    
    @DelegateImplementation( SapphireUiDefMethods.class )
    
    ISapphirePartDef getPartDef( String id,
                                 boolean searchImportedDefinitions,
                                 Class<?> expectedType );
    
    // *** Method : resolveClass ***
    
    @DelegateImplementation( SapphireUiDefMethods.class )
    
    Class<?> resolveClass( String className );
    
    // *** Method : resolveProperty ***
    
    @DelegateImplementation( SapphireUiDefMethods.class )
    
    ModelProperty resolveProperty( String qualifiedPropertyName );
    
}
