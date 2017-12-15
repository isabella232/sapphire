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

package org.eclipse.sapphire.samples.contacts;

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementHandle;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.Documentation;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NoDuplicates;
import org.eclipse.sapphire.modeling.annotations.NonNullValue;
import org.eclipse.sapphire.modeling.annotations.PossibleValues;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.CustomXmlListBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlElementBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.samples.contacts.internal.ConnectionsListController;
import org.eclipse.sapphire.samples.contacts.internal.ContactCategoryValuesProvider;
import org.eclipse.sapphire.samples.contacts.internal.ContactMethods;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Image( small = "org.eclipse.sapphire.samples/images/person.png" )
@GenerateImpl

public interface IContact

    extends IModelElement

{
    ModelElementType TYPE = new ModelElementType( IContact.class );

    // *** Name ***
    
    @Label( standard = "name" )
    @NonNullValue
    @NoDuplicates
    @XmlBinding( path = "@name" )

    ValueProperty PROP_NAME = new ValueProperty( TYPE, "Name" );

    Value<String> getName();
    void setName( String name );
    
    // *** Category ***
    
    @Label( standard = "category" )
    @DefaultValue( text = "Personal" )
    @PossibleValues( service = ContactCategoryValuesProvider.class )
    @XmlBinding( path = "%category" )
    @Documentation( content = "This would be the help content for the category property." )

    ValueProperty PROP_CATEGORY = new ValueProperty( TYPE, "Category" );

    Value<String> getCategory();
    void setCategory( String category );
    
    // *** EMail ***
    
    @Label( standard = "E-Mail" )
    @XmlBinding( path = "e-mail" )

    ValueProperty PROP_E_MAIL = new ValueProperty( TYPE, "EMail" );

    Value<String> getEMail();
    void setEMail( String email );
    
    // *** PhoneNumbers ***
    
    @Type( base = IPhoneNumber.class )
    @Label( standard = "phone numbers" )
    @XmlListBinding( path = "phone-numbers", mappings = @XmlListBinding.Mapping( element = "phone-number", type = IPhoneNumber.class ) )
                             
    ListProperty PROP_PHONE_NUMBERS = new ListProperty( TYPE, "PhoneNumbers" );
    
    ModelElementList<IPhoneNumber> getPhoneNumbers();
    
    // *** WebSites ***
    
    @Type( base = IWebSite.class )
    @Label( standard = "web sites" )
    @XmlListBinding( path = "web-sites", mappings = @XmlListBinding.Mapping( element = "web-site", type = IWebSite.class ) )
                             
    ListProperty PROP_WEB_SITES = new ListProperty( TYPE, "WebSites" );
    
    ModelElementList<IWebSite> getWebSites();
    
    // *** METHOD: removePhoneNumbersByAreaCode ***
    
    @DelegateImplementation( ContactMethods.class )
    
    void removePhoneNumbersByAreaCode( String areaCode );
    
    // *** Address ***
    
    @Type( base = IAddress.class )
    @XmlBinding( path = "address" )
    
    ImpliedElementProperty PROP_ADDRESS = new ImpliedElementProperty( TYPE, "Address" );

    IAddress getAddress();

    // *** Assistant ***

    @Type( base = IAssistant.class )
    @Label( standard = "assistant" )
    @XmlBinding( path = "assistant" )
    
    ElementProperty PROP_ASSISTANT = new ElementProperty( TYPE, "Assistant" );

    ModelElementHandle<IAssistant> getAssistant();
    
    // *** Connections ***
    
    @Label( standard = "connections" )
    @Type( base = IConnection.class )
    @CustomXmlListBinding( impl = ConnectionsListController.class )
                             
    ListProperty PROP_CONNECTIONS = new ListProperty( TYPE, "Connections" );
    
    ModelElementList<IConnection> getConnections();
    
    // *** PrimaryOccupation ***
    
    @Type
    ( 
        base = IOccupation.class, 
        possible = 
        { 
            IJobOccupation.class, 
            IStudentOccupation.class, 
            IHomemakerOccupation.class 
        }
    )
    
    @Label( standard = "primary occupation" )
    
    @XmlElementBinding
    ( 
        path = "primary-occupation",
        mappings = 
        {
            @XmlElementBinding.Mapping( element = "job", type = IJobOccupation.class ),
            @XmlElementBinding.Mapping( element = "student", type = IStudentOccupation.class ),
            @XmlElementBinding.Mapping( element = "homemaker", type = IHomemakerOccupation.class )
        }
    )
    
    ElementProperty PROP_PRIMARY_OCCUPATION = new ElementProperty( TYPE, "PrimaryOccupation" );
    
    ModelElementHandle<IOccupation> getPrimaryOccupation();
    
}
