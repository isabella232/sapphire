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

package org.eclipse.sapphire.sdk.xml.schema.normalizer;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Unique;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.ProgressMonitor;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.FileExtensions;
import org.eclipse.sapphire.modeling.annotations.FileSystemResourceType;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Listeners;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.annotations.ValidFileSystemResourceType;
import org.eclipse.sapphire.sdk.xml.schema.normalizer.internal.CreateNormalizedXmlSchemaOpMethods;
import org.eclipse.sapphire.sdk.xml.schema.normalizer.internal.CreateNormalizedXmlSchemaOpServices.FolderInitialValueService;
import org.eclipse.sapphire.sdk.xml.schema.normalizer.internal.CreateNormalizedXmlSchemaOpServices.SourceFileInitialValueService;
import org.eclipse.sapphire.sdk.xml.schema.normalizer.internal.CreateNormalizedXmlSchemaOpServices.SourceFileListener;
import org.eclipse.sapphire.workspace.CreateWorkspaceFileOp;
import org.eclipse.sapphire.workspace.WorkspaceRelativePath;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface CreateNormalizedXmlSchemaOp extends CreateWorkspaceFileOp
{
    ElementType TYPE = new ElementType( CreateNormalizedXmlSchemaOp.class );
    
    // *** Folder ***
    
    @Service( impl = FolderInitialValueService.class )

    ValueProperty PROP_FOLDER = new ValueProperty( TYPE, CreateWorkspaceFileOp.PROP_FOLDER );
    
    // *** File ***

    @FileExtensions( expr = "xsd" )

    ValueProperty PROP_FILE = new ValueProperty( TYPE, CreateWorkspaceFileOp.PROP_FILE );
    
    // *** SourceFile ***
    
    @Type( base = Path.class )
    @Label( standard = "source file" )
    @ValidFileSystemResourceType( FileSystemResourceType.FILE )
    @FileExtensions( expr = "xsd" )
    @WorkspaceRelativePath
    @MustExist
    @Required
    @Service( impl = SourceFileInitialValueService.class )
    @Listeners( SourceFileListener.class )
    
    ValueProperty PROP_SOURCE_FILE = new ValueProperty( TYPE, "SourceFile" );
    
    Value<Path> getSourceFile();
    void setSourceFile( String value );
    void setSourceFile( Path value );
    
    // *** RootElements ***
    
    interface RootElement extends Element
    {
        ElementType TYPE = new ElementType( RootElement.class );
        
        // *** Name ***
        
        @Label( standard = "name" )
        @Required
        @Unique
        
        ValueProperty PROP_NAME = new ValueProperty( TYPE, "Name" );
        
        Value<String> getName();
        void setName( String value );
    }
    
    @Type( base = RootElement.class )
    @Label( standard = "root elements" )
    
    ListProperty PROP_ROOT_ELEMENTS = new ListProperty( TYPE, "RootElements" );
    
    ElementList<RootElement> getRootElements();
    
    // *** Exclusions ***
    
    interface Exclusion extends Element
    {
        ElementType TYPE = new ElementType( Exclusion.class );
        
        // *** Type ***
        
        enum ExclusionType
        {
            ATTRIBUTE,
            ELEMENT
        }
        
        @Type( base = ExclusionType.class )
        @Label( standard = "type" )
        @Required
        
        ValueProperty PROP_TYPE = new ValueProperty( TYPE, "Type" );
        
        Value<ExclusionType> getType();
        void setType( String value );
        void setType( ExclusionType value );
        
        // *** Name ***
        
        @Label( standard = "name" )
        @Required
        
        ValueProperty PROP_NAME = new ValueProperty( TYPE, "Name" );
        
        Value<String> getName();
        void setName( String value );
    }
    
    @Type( base = Exclusion.class )
    @Label( standard = "exclusions" )
    
    ListProperty PROP_EXCLUSIONS = new ListProperty( TYPE, "Exclusions" );
    
    ElementList<Exclusion> getExclusions();
    
    // *** TypeSubstitutions ***

    interface TypeSubstitution extends Element
    {
        ElementType TYPE = new ElementType( TypeSubstitution.class );
        
        // *** Before ***
        
        @Label( standard = "before" )
        @Required
        
        ValueProperty PROP_BEFORE = new ValueProperty( TYPE, "Before" );
        
        Value<String> getBefore();
        void setBefore( String value );
        
        // *** After ***
        
        @Label( standard = "after" )
        @Required
        
        ValueProperty PROP_AFTER = new ValueProperty( TYPE, "After" );
        
        Value<String> getAfter();
        void setAfter( String value );
    }
    
    @Type( base = TypeSubstitution.class )
    @Label( standard = "type substitutions" )
    
    ListProperty PROP_TYPE_SUBSTITUTIONS = new ListProperty( TYPE, "TypeSubstitutions" );
    
    ElementList<TypeSubstitution> getTypeSubstitutions();
    
    // *** SortSequenceContent ***
    
    @Type( base = Boolean.class )
    @Label( standard = "sort sequence content" )
    @DefaultValue( text = "false" )
    
    ValueProperty PROP_SORT_SEQUENCE_CONTENT = new ValueProperty( TYPE, "SortSequenceContent" );
    
    Value<Boolean> getSortSequenceContent();
    void setSortSequenceContent( String value );
    void setSortSequenceContent( Boolean value );
    
    // *** RemoveWildcards ***
    
    @Type( base = Boolean.class )
    @Label( standard = "remove wildcards" )
    @DefaultValue( text = "false" )

    ValueProperty PROP_REMOVE_WILDCARDS = new ValueProperty( TYPE, "RemoveWildcards" );
    
    Value<Boolean> getRemoveWildcards();
    void setRemoveWildcards( String value );
    void setRemoveWildcards( Boolean value );
    
    // *** Method: execute ***
    
    @DelegateImplementation( CreateNormalizedXmlSchemaOpMethods.class )
    
    Status execute( ProgressMonitor monitor );

}
