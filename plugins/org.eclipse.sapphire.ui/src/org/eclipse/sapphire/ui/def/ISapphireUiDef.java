/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [bugzilla 329114] rewrite context help binding feature
 ******************************************************************************/

package org.eclipse.sapphire.ui.def;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlRootBinding;
import org.eclipse.sapphire.ui.def.internal.SapphireUiDefMethods;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@GenerateImpl
@XmlRootBinding( elementName = "definition" )

public interface ISapphireUiDef

    extends IModelElement
    
{
    ModelElementType TYPE = new ModelElementType( ISapphireUiDef.class );
    
    // *** ImportDirectives ***
    
    @Type( base = IImportDirective.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "import", type = IImportDirective.class ) )
                             
    ListProperty PROP_IMPORT_DIRECTIVES = new ListProperty( TYPE, "ImportDirectives" );
    
    ModelElementList<IImportDirective> getImportDirectives();
    
    // *** Method : getImportedDefinnitions ***
    
    @DelegateImplementation( SapphireUiDefMethods.class )
    
    List<ISapphireUiDef> getImportedDefinitions();

    // *** CompositeDefs ***
    
    @Type( base = ISapphireCompositeDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "composite", type = ISapphireCompositeDef.class ) )
                             
    ListProperty PROP_COMPOSITE_DEFS = new ListProperty( TYPE, "CompositeDefs" );
    
    ModelElementList<ISapphireCompositeDef> getCompositeDefs();
    
    // *** Method : getCompositeDef ***
    
    @DelegateImplementation( SapphireUiDefMethods.class )
    
    ISapphireCompositeDef getCompositeDef( String id,
                                           boolean searchImportedDefinitions );
    
    // *** DocumentationDefs ***
    
    @Type( base = ISapphireDocumentationDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "documentation", type = ISapphireDocumentationDef.class ) )
                             
    ListProperty PROP_DOCUMENTATION_DEFS = new ListProperty( TYPE, "DocumentationDefs" );
    
    ModelElementList<ISapphireDocumentationDef> getDocumentationDefs();
    
    // *** Method : getDocumentationDef ***
    
    @DelegateImplementation( SapphireUiDefMethods.class )
    
    ISapphireDocumentationDef getDocumentationDef( String id,
                                               boolean searchImportedDefinitions );

    // *** MasterDetailsTreeNodeDefs ***
    
    @Type( base = IMasterDetailsTreeNodeDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "node", type = IMasterDetailsTreeNodeDef.class ) )
    
    ListProperty PROP_MASTER_DETAILS_TREE_NODE_DEFS = new ListProperty( TYPE, "MasterDetailsTreeNodeDefs" );
    
    ModelElementList<IMasterDetailsTreeNodeDef> getMasterDetailsTreeNodeDefs();
    
    // *** Method : getMasterDetailsTreeNodeDef ***
    
    @DelegateImplementation( SapphireUiDefMethods.class )
    
    IMasterDetailsTreeNodeDef getMasterDetailsTreeNodeDef( String id,
                                                           boolean searchImportedDefinitions );
    
    // *** MasterDetailsTreeNodeFactoryDefs ***
    
    @Type( base = IMasterDetailsTreeNodeFactoryDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "node-list", type = IMasterDetailsTreeNodeFactoryDef.class ) )
    
    ListProperty PROP_MASTER_DETAILS_TREE_NODE_FACTORY_DEFS = new ListProperty( TYPE, "MasterDetailsTreeNodeFactoryDefs" );
    
    ModelElementList<IMasterDetailsTreeNodeFactoryDef> getMasterDetailsTreeNodeFactoryDefs();
    
    // *** Method : getMasterDetailsTreeNodeFactoryDef ***
    
    @DelegateImplementation( SapphireUiDefMethods.class )
    
    IMasterDetailsTreeNodeFactoryDef getMasterDetailsTreeNodeFactoryDef( String id,
                                                                         boolean searchImportedDefinitions );
    
    // *** EditorPageDefs ***
    
    @Type( base = IEditorPageDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "editor-page", type = IEditorPageDef.class ) )
    
    ListProperty PROP_EDITOR_PAGE_DEFS = new ListProperty( TYPE, "EditorPageDefs" );
    
    ModelElementList<IEditorPageDef> getEditorPageDefs();
    
    // *** DialogDefs ***
    
    @Type( base = ISapphireDialogDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "dialog", type = ISapphireDialogDef.class ) )
    
    ListProperty PROP_DIALOG_DEFS = new ListProperty( TYPE, "DialogDefs" );
    
    ModelElementList<ISapphireDialogDef> getDialogDefs();
    
    // *** Method : getDialogDef ***
    
    @DelegateImplementation( SapphireUiDefMethods.class )
    
    ISapphireDialogDef getDialogDef( String id,
                                     boolean searchImportedDefinitions );
    
    // *** WizardDefs ***
    
    @Type( base = ISapphireWizardDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "wizard", type = ISapphireWizardDef.class ) )
    
    ListProperty PROP_WIZARD_DEFS = new ListProperty( TYPE, "WizardDefs" );
    
    ModelElementList<ISapphireWizardDef> getWizardDefs();
    
    // *** Method : getWizardDef ***
    
    @DelegateImplementation( SapphireUiDefMethods.class )
    
    ISapphireWizardDef getWizardDef( String id,
                                     boolean searchImportedDefinitions );
    
    // *** Method : resolveClass ***
    
    @DelegateImplementation( SapphireUiDefMethods.class )
    
    Class<?> resolveClass( String className );
    
    // *** Method : resolveProperty ***
    
    @DelegateImplementation( SapphireUiDefMethods.class )
    
    ModelProperty resolveProperty( String qualifiedPropertyName );
    
    // *** Method : resolveImage ***
    
    @DelegateImplementation( SapphireUiDefMethods.class )
    
    ImageDescriptor resolveImage( String imagePath );
    
}
