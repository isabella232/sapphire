/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [338605] The include directive not handled in XML Schema parsing (regression)
 *               [337232] Certain schema causes elements to be out of order in corresponding xml files
 ******************************************************************************/

package org.eclipse.sapphire.modeling.xml.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.namespace.QName;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class XmlDocumentSchema
{
    private String namespace;
    private String schemaLocation;
    private final Map<String,String> importedNamespaces;
    private final Map<String,XmlContentModel> contentModels;
    private final Map<String,XmlElementDefinition> topLevelElements;
    
    private XmlDocumentSchema( final String namespace,
                               final String schemaLocation,
                               final Map<String,String> importedNamespaces,
                               final Map<String,XmlContentModel.Factory> contentModels,
                               final List<XmlElementDefinition.Factory> topLevelElements )
    {
        this.namespace = namespace;
        this.schemaLocation = schemaLocation;
        this.importedNamespaces = new TreeMap<String,String>( importedNamespaces );
        
        this.contentModels = new TreeMap<String,XmlContentModel>();
        
        for( Map.Entry<String,XmlContentModel.Factory> entry : contentModels.entrySet() )
        {
            final XmlContentModel.Factory factory = entry.getValue();
            
            if( factory != null )
            {
                this.contentModels.put( entry.getKey(), optimize( factory.create( this ) ) );
            }
        }
        
        this.topLevelElements = new TreeMap<String,XmlElementDefinition>();
        
        for( XmlElementDefinition.Factory factory : topLevelElements )
        {
            final XmlElementDefinition def = (XmlElementDefinition) optimize( factory.create( this ) );
            this.topLevelElements.put( def.getName().getLocalPart(), def );
        }
        
        for ( Map.Entry<String, XmlElementDefinition> map : this.topLevelElements.entrySet() ) { 
            final XmlElementDefinition definition = map.getValue();
            if (definition.isAbstract()) {
                List<XmlElementDefinition> substitutionList = new ArrayList<XmlElementDefinition>();
                for ( Map.Entry<String, XmlElementDefinition> map2 : this.topLevelElements.entrySet() ) {
                    final XmlElementDefinition definition2 = map2.getValue();
                    if (definition.getName().equals(definition2.getSubstitutionGroup())) {
                        XmlElementDefinition.Factory def = new XmlElementDefinitionByReference.Factory();
                        def.setName(definition2.getName());
                        def.setMinOccur(definition2.getMinOccur());
                        def.setMaxOccur(definition2.getMaxOccur());
                        substitutionList.add((XmlElementDefinition)def.create(this));
                    }
                }
                definition.setSubstitutionList(substitutionList);
            }
        }
    }
    
    public String getNamespace()
    {
        return this.namespace;
    }
    
    public String getSchemaLocation()
    {
        return this.schemaLocation;
    }
    
    public String getSchemaLocation( final String namespace )
    {
        String res;
        
        if( namespace.equals( this.namespace ) )
        {
            res = this.schemaLocation;
        }
        else
        {
            res = this.importedNamespaces.get( namespace );
            
            if( res == null )
            {
                res = namespace;
            }
        }
        
        return res;
    }
    
    public Map<String,String> getSchemaLocations()
    {
        final Map<String,String> schemaLocations = new TreeMap<String,String>();
        
        if( this.namespace != null && this.schemaLocation != null )
        {
            schemaLocations.put( this.namespace, this.schemaLocation );
        }
        
        schemaLocations.putAll( this.importedNamespaces );
        
        return Collections.unmodifiableMap( schemaLocations );
    }
    
    public XmlElementDefinition getElement( final String name )
    {
        return this.topLevelElements.get( name );
    }
    
    public XmlContentModel getContentModel( final String name )
    {
        return this.contentModels.get( name );
    }
    
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        
        for( XmlElementDefinition xmlElementSchema : this.topLevelElements.values() )
        {
            xmlElementSchema.toString( buf, "" );
            buf.append( "\n\n" );
        }
        
        for( Map.Entry<String,XmlContentModel> entry : this.contentModels.entrySet() )
        {
            buf.append( entry.getKey() );
            buf.append( " = " );
            entry.getValue().toString( buf, "" );
            buf.append( "\n\n" );
        }

        return buf.toString();
    }
    
    private XmlContentModel optimize( final XmlContentModel contentModel )
    {
        if( contentModel instanceof XmlSequenceGroup )
        {
            final XmlSequenceGroup sequenceContentModel = (XmlSequenceGroup) contentModel;
            final List<XmlContentModel> nestedContent = new ArrayList<XmlContentModel>();
            boolean optimized = false;
            
            for( XmlContentModel child : sequenceContentModel.getNestedContent() )
            {
                boolean handled = false;
                
                if( child instanceof XmlSequenceGroup )
                {
                    final XmlSequenceGroup cs = (XmlSequenceGroup) child;
                    
                    if( cs.getMinOccur() == 1 && cs.getMaxOccur() == 1 )
                    {
                        for( XmlContentModel nested : cs.getNestedContent() )
                        {
                            nestedContent.add( optimize( nested ) );
                        }

                        handled = true;
                        optimized = true;
                    }
                }
                
                if( ! handled )
                {
                    final XmlContentModel optimizedChild = optimize( child );
                    
                    if( optimizedChild != child )
                    {
                        optimized = true;
                    }
                    
                    nestedContent.add( optimizedChild );
                }
            }
            
            if( optimized )
            {
                return new XmlSequenceGroup( sequenceContentModel.getSchema(), sequenceContentModel.getMinOccur(), sequenceContentModel.getMaxOccur(), nestedContent );
            }
        }
        else if( contentModel instanceof XmlChoiceGroup )
        {
            final XmlChoiceGroup choiceContentModel = (XmlChoiceGroup) contentModel;
            final List<XmlContentModel> nestedContent = new ArrayList<XmlContentModel>();
            boolean optimized = false;
            
            for( XmlContentModel child : choiceContentModel.getNestedContent() )
            {
                final XmlContentModel optimizedChild = optimize( child );
                
                if( optimizedChild != child )
                {
                    optimized = true;
                }
                
                nestedContent.add( optimizedChild );
            }
            
            if( optimized )
            {
                return new XmlChoiceGroup( choiceContentModel.getSchema(), choiceContentModel.getMinOccur(), choiceContentModel.getMaxOccur(), nestedContent );
            }
        }
        
        return contentModel;
    }
    
    public static final class Factory
    {
        private String namespace;
        private String schemaLocation;
        private final Map<String,String> importedNamespaces = new TreeMap<String,String>();
        private final Map<String,XmlContentModel.Factory> contentModels = new TreeMap<String,XmlContentModel.Factory>();
        private final List<XmlElementDefinition.Factory> topLevelElements = new ArrayList<XmlElementDefinition.Factory>();

        public String getNamespace()
        {
            return this.namespace;
        }
        
        public void setNamespace( final String namespace )
        {
            this.namespace = namespace;
        }
        
        public String getSchemaLocation()
        {
            return this.schemaLocation;
        }
        
        public void setSchemaLocation( final String schemaLocation )
        {
            this.schemaLocation = schemaLocation;
        }
        
        public Map<String,String> getImportedNamespaces()
        {
            return Collections.unmodifiableMap( this.importedNamespaces );
        }
        
        public void addImportedNamespace( final String namespace,
                                          final String schemaLocation )
        {
            this.importedNamespaces.put( namespace, schemaLocation );
        }
        
        public Map<String,XmlContentModel.Factory> getContentModels()
        {
            return Collections.unmodifiableMap( this.contentModels );
        }
        
        public XmlContentModel.Factory getContentModel( final String name )
        {
            return this.contentModels.get( name );
        }
        
        public void addContentModel( final String name,
                                     final XmlContentModel.Factory contentModel )
        {
            this.contentModels.put( name, contentModel );
        }
        
        public void removeContentModel( final String name )
        {
            this.contentModels.remove( name );
        }
        
        public QName createContentModelName()
        {
            int counter = 1;
            String contentModelName = null;
            
            do
            {
                contentModelName = "##@" + String.valueOf( counter );
                counter++;
            }
            while( this.contentModels.containsKey( contentModelName ) );
            
            this.contentModels.put( contentModelName, null );
            
            return new QName( this.namespace, contentModelName );
        }
        
        public List<XmlElementDefinition.Factory> getTopLevelElements()
        {
            return Collections.unmodifiableList( this.topLevelElements );
        }
        
        public void addTopLevelElement( final XmlElementDefinition.Factory topLevelElement )
        {
            this.topLevelElements.add( topLevelElement );
        }
        
        public XmlDocumentSchema create()
        {
            return new XmlDocumentSchema( this.namespace, this.schemaLocation, this.importedNamespaces, this.contentModels, this.topLevelElements );
        }
    }

}
