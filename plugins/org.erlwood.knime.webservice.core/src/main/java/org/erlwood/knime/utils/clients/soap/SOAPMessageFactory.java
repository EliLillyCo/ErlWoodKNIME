/*
 * ------------------------------------------------------------------------
 *
 * Copyright (C) 2017 Eli Lilly and Company Limited
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * ------------------------------------------------------------------------
*/
package org.erlwood.knime.utils.clients.soap;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/** Factory for generating SOAP request and response messages when making SOAP calls using
 * KNIME REST clients.
 * @author Tom Wilkin */
public final class SOAPMessageFactory {
	
	/** Class to hold a MultiPart include element. */
	public static final class Include {
		
		/** The id for the MultiPart data element. */
		private String id;
		
		public Include(final String id) {
			this.id = id;
		}
		
		/** Add the Include element to the supplied SOAP XML.
		 * @param element The element to add to.
		 * @throws SOAPException If an error occured generating the XML. */
		public void addToElement(final SOAPElement element) throws SOAPException {
			SOAPElement child = element.addChildElement(
					new QName("http://www.w3.org/2004/08/xop/include", "Include", "xop")
			);
			child.setAttribute("href", "cid:" + id);
		}
		
	}

	/** Cannot instantiate factory. */
	private SOAPMessageFactory() { }
	
	/** Create a request SOAPMessage.
	 * @param protocol The SOAP protocol to use for creating the message (see SOAPConstants).
	 * @param namespace The namespace of the SOAP web service.
	 * @param alias The namespace alias for the SOAP web service namespace.
	 * @param method The web service method the message is for.
	 * @param params The parameters to pass to the web service method.
	 * @return The SOAPMessage containing the given parameters.
	 * @throws SOAPException If the SOAPMessage cannot be created.
	 * @throws DatatypeConfigurationException If a date cannot be converted. */
	public static SOAPMessage createRequestMessage(final String protocol, final String namespace, 
				final String alias, final String method, final Map<String, Object> params) 
			throws SOAPException, DatatypeConfigurationException
	{
		// Setup the SOAP messaging.
		MessageFactory factory = MessageFactory.newInstance(protocol);
		SOAPMessage message = factory.createMessage();
		
		// create the SOAP body
		SOAPBody body = message.getSOAPBody();
		message.getSOAPHeader().detachNode();
		QName bodyName;
		if(alias != null) {
			bodyName = new QName(namespace, method, alias);
		} else {
			bodyName = new QName(namespace, method);
		}
		SOAPBodyElement bodyElement = body.addBodyElement(bodyName);
		
		// Add the params
		addParameters(bodyElement, params);
		
		return message;
	}
	
	/** Create a String version of the SOAPMessage to pass in the POST packet payload.
	 * @param protocol The SOAP protocol to use for creating the message (see SOAPConstants).
	 * @param namespace The namespace of the SOAP web service.
	 * @param alias The namespace alias for the SOAP web service namespace.
	 * @param method The web service method the message is for.
	 * @param params The parameters to pass to the web service method.
	 * @return The string version of the SOAPMessage.
	 * @throws SOAPException If the SOAPMessage cannot be created.
	 * @throws TransformerException If the SOAPMessage cannot be converted to a string.
	 * @throws DatatypeConfigurationException If a date cannot be converted. */
	public static String createRequestString(final String protocol, final String namespace, final String alias, 
					final String method, final Map<String, Object> params) 
			throws SOAPException, TransformerException, DatatypeConfigurationException
	{
		SOAPMessage message = createRequestMessage(protocol, namespace, alias, method, params);
		
		StringWriter sw = new StringWriter();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.transform(
				new DOMSource(message.getSOAPPart()),
				new StreamResult(sw)
		);
		
		return sw.toString();
	}
	
	/** Create a String version of the SOAPMessage to pass in the POST packet payload.
	 * @param protocol The SOAP protocol to use for creating the message (see SOAPConstants).
	 * @param namespace The namespace of the SOAP web service.
	 * @param method The web service method the message is for.
	 * @param params The parameters to pass to the web service method.
	 * @return The string version of the SOAPMessage.
	 * @throws SOAPException If the SOAPMessage cannot be created.
	 * @throws TransformerException If the SOAPMessage cannot be converted to a string.
	 * @throws DatatypeConfigurationException If a date cannot be converted. */
	public static String createRequestString(final String protocol, final String namespace, 
				final String method, final Map<String, Object> params)
			throws SOAPException, TransformerException, DatatypeConfigurationException
	{
		return createRequestString(protocol, namespace, null, method, params);
	}
	
	/** Parse the web service response stream into a SOAPMessage.
	 * @param protocol The SOAP protocol to use for creating the message (see SOAPConstants).
	 * @param stream The web service response stream.
	 * @return The SOAPMessage from the InputStream.
	 * @throws IOException If an error occurs reading from the stream.
	 * @throws SOAPException If an error occurs creating the SOAPMessage. */
	public static SOAPMessage createResponseMessage(final String protocol, final InputStream stream)
			throws IOException, SOAPException
	{
		return MessageFactory.newInstance(protocol).createMessage(null, stream);
	}
	
	/** Parse the web service response stream into an XML Document.
	 * @param protocol The SOAP protocol to use for creating the message (see SOAPConstants).
	 * @param stream The web service response stream.
	 * @return The XML Document from the InputStream.
	 * @throws IOException If an error occurs reading from the stream.
	 * @throws SOAPException If an error occurs creating the SOAPMessage. */
	public static Document createResponseXML(final String protocol, final InputStream stream) 
			throws IOException, SOAPException
	{
		SOAPMessage message = createResponseMessage(protocol, stream);
		return message.getSOAPBody().extractContentAsDocument();
	}
	
	/** Add the supplied parameters to the SOAPElement.
	 * @param element The SOAPElement to add the parameters to.
	 * @param params The parameter key value pairs to add.
	 * @throws SOAPException If an error occurs adding a parameter to the element.
	 * @throws DatatypeConfigurationException If an error occurs converting a date. */
	private static void addParameters(final SOAPElement element, final Map<String, Object> params) 
			throws SOAPException, DatatypeConfigurationException
	{
		if (params != null) {
			for (Entry<String, Object> es : params.entrySet()) {
				addValue(element, es.getKey(), es.getValue());
			}
		}
	}
	
	/** Add the supplied value to the SOAPElement.
	 * @param element The SOAPElement to add the parameters to.
	 * @param key The name of the new element to add.
	 * @param value The value of the element to add.
	  * @throws SOAPException If an error occurs adding a parameter to the element.
	 * @throws DatatypeConfigurationException If an error occurs converting a date. */
	@SuppressWarnings("unchecked")
	private static void addValue(final SOAPElement element, final String key, final Object value) 
			throws SOAPException, DatatypeConfigurationException
	{
		if(value instanceof Map) {
			// add a nested element
			SOAPElement child = element.addChildElement(key);
			addParameters(child, (Map<String, Object>)value);
		} else if(value instanceof List) {
			// add a nested list
			for(Object o : (List<?>)value) {
				addValue(element, key, o);
			}
		} else if(value instanceof Date) {
			// handle dates
			Calendar cal = Calendar.getInstance();
			cal.setTime((Date)value);
			XMLGregorianCalendar csal = DatatypeFactory.newInstance().newXMLGregorianCalendar((GregorianCalendar) cal);
			SOAPElement child = element.addChildElement(key);
			child.addTextNode(csal.toXMLFormat());
		} else if(value instanceof Include) {
			// handle nested multi-part elements
			SOAPElement child = element.addChildElement(key);
			((Include)value).addToElement(child);
		} else {
			// catch all for string serialisable data types
			String s = (value == null ? null : value.toString());
			SOAPElement child = element.addChildElement(key);
			child.removeAttributeNS(child.getNamespaceURI(), "xmlns");
			child.addTextNode(s);
		}
	}

}
