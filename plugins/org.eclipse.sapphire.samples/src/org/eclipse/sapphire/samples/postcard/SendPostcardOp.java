/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.samples.postcard;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ExecutableElement;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.ProgressMonitor;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.Derived;
import org.eclipse.sapphire.modeling.annotations.LongString;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface SendPostcardOp extends ExecutableElement
{
    ElementType TYPE = new ElementType( SendPostcardOp.class );
    
    // *** Sender ***
    
    @Type( base = Sender.class )
    
    ImpliedElementProperty PROP_SENDER = new ImpliedElementProperty( TYPE, "Sender" );
    
    Sender getSender();
    
    // *** Recipient ***
    
    @Type( base = FullAddress.class )
    
    ImpliedElementProperty PROP_RECIPIENT = new ImpliedElementProperty( TYPE, "Recipient" );
    
    FullAddress getRecipient();
    
    // *** Message ***
    
    @LongString
    
    ValueProperty PROP_MESSAGE = new ValueProperty( TYPE, "Message" );
    
    Value<String> getMessage();
    void setMessage( String value );
    
    // *** Postcard ***
    
    @Derived
    (
        text = "<html>" +
                   "<body>" +
                       "<style type='text/css'>" +
                           "p" +
                           "{" +
                               "font: 14px 'Trebuchet MS', Verdana, Arial, Helvetica, sans-serif;" +
                           "}" +
                           "div" +
                           "{" +
                               "font: 14px 'Trebuchet MS', Verdana, Arial, Helvetica, sans-serif;" +
                           "}" +
                       "</style>" +
                       "<table width='100%'>" +
                           "<tr>" +
                               "<td width='60%' style='padding:10px;vertical-align:top'>" +
                                   "<p>" +
                                       "${ Sender.Name.EncodeToXml }<br/>" +
                                       "${ Sender.Street.EncodeToXml }<br/>" +
                                       "${ Sender.City.EncodeToXml }${ Sender.City == null ? '' : ', ' }${ Sender.State.EncodeToXml } ${ Sender.ZipCode.EncodeToXml }" +
                                   "</p>" +
                                   "<br/><br/>" +
                                   "<p>" +
                                       "${ Message.EncodeToXml.Replace( '\n', '<br/>' ) }" +
                                   "</p>" +
                               "</td>" +
                               "<td style='border-left:2px solid black;padding:10px'>" +
                                   "<div style='border:1px solid black;float:right;padding:20px'>Stamp</div>" +
                                   "<div style='height:200px;'>&nbsp;</div>" +
                                   "<p>" +
                                       "${ Recipient.Name.EncodeToXml }<br/>" +
                                       "${ Recipient.Street.EncodeToXml }<br/>" +
                                       "${ Recipient.City.EncodeToXml }${ Recipient.City == null ? '' : ', ' }${ Recipient.State.EncodeToXml } ${ Recipient.ZipCode.EncodeToXml }" +
                                   "</p>" +
                               "</td>" +
                           "</tr>" +
                       "</table>" +
                   "</body>" +
               "</html>"
    )
    
    ValueProperty PROP_POSTCARD = new ValueProperty( TYPE, "Postcard" );
    
    Value<String> getPostcard();

    // *** Method: execute ***
    
    @DelegateImplementation( SendPostcardOpMethods.class )
    
    Status execute( ProgressMonitor monitor );

}
