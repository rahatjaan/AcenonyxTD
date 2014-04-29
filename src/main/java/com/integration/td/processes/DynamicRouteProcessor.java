package com.integration.td.processes;


import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.UUID;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.integration.td.constants.Constants;
import com.integration.td.constants.EmailSource;
import com.integration.td.transformer.BillDetailsTransformer;
import com.integration.td.transformer.ReceiveTDOrderTransformer;
import com.integration.td.utils.SendEmail;
import com.integration.td.utils.XMLElementExtractor;

public class DynamicRouteProcessor implements Processor{

	private EmailSource emailSource;
	
	public EmailSource getEmailSource() {
		return emailSource;
	}


	public void setEmailSource(EmailSource emailSource) {
		this.emailSource = emailSource;
	}


	public void process(Exchange arg0) throws Exception {
		String url = arg0.getIn().getHeader("OutboundUrl").toString();
		String flow = arg0.getIn().getHeader("flow").toString();
		System.out.println("URL IS: "+url+" and flow:"+flow);
		String req = arg0.getIn().getBody().toString();
		boolean isNotValidResLookUp = false;
		//***** Find whether the given host is qualified or not
		//boolean isCon = isConnectable(url);
		//*****************************************************
		//*******************************************
		if(isConnectable(url)){
			System.out.println(req);
			int ind1 = req.indexOf("<");
			req = req.substring(ind1);
			if(req.contains("&gt;")){
				req = req.replaceAll("&gt;",">");
				req = req.replaceAll("&lt;","<");
			}
			System.out.println("1"+req);
			req = req.substring(req.indexOf("<o>"));
			System.out.println("2"+req);
			req = req.replaceAll("<o>","");
			req = req.replaceAll("</o>","");
			req = req.replaceAll("<item>","");
			req = req.replaceAll("</item>","");
			req = req.replaceAll("<e>","<item>");
			req = req.replaceAll("</e>","</item>");
			System.out.println("3"+req);
			if(req.contains("</payload>"))
				req = req.substring(0,req.indexOf("</payload>"));
			
			req = "<request>"+req+"</request>";
			System.out.println("REQUEST : "+req);
			try {
	            // Create SOAP Connection
	            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
	            SOAPConnection soapConnection = soapConnectionFactory.createConnection();
	
	            // Send SOAP Message to SOAP Server
	            //String url = "http://ws.cdyne.com/emailverify/Emailvernotestemail.asmx";
	            SOAPMessage soapResponse = null;
	            
	            if(Constants.RECEIVETDORDER.equalsIgnoreCase(flow)){
	            	String orderNumber = null;
	            	String terminalId = null;
	            	String tableNumber = null;
	            	try{
	            		orderNumber = XMLElementExtractor.extractXmlElementValue(req, "order_number");
	            		terminalId = XMLElementExtractor.extractXmlElementValue(req, "terminalId");
	            		tableNumber = XMLElementExtractor.extractXmlElementValue(req, "tableNumber");
	            	}catch(Exception e){
	            		String body = "{\"ServiceError\":{\"faultstring\":\"An exception has occured.\",\"faultreason\":\"You are missing one of these three, please check your request properly before submitting (1). Order Number (2). Terminal Id (3). Table Number\"}}";
			            soapConnection.close();
			            arg0.getOut().setBody(body);
			            return;
	            	}
		            	if(null == orderNumber || null == terminalId || null == tableNumber){
		            		String body = "{\"ServiceError\":{\"faultstring\":\"An exception has occured.\",\"faultreason\":\"You are missing one of these three, please check your request properly before submitting (1). Order Number (2). Terminal Id (3). Table Number\"}}";
				            soapConnection.close();
				            arg0.getOut().setBody(body);
		            	}else{
			            	soapResponse = soapConnection.call(createSOAPRequestForCreateAndLockTransaction(req), url);
			            	String message = printSOAPResponse(soapResponse); 
							int index1 = req.indexOf("<products>");
							int index2 = req.indexOf("</products>");
							String val = req.substring(index1,index2);
							message = message.substring(message.indexOf("beleg"),message.indexOf("kassierer"));
							message = "<"+ message +"/beleg>";
							while(index1 < index2 && val.contains("<id>")){
								index1 = val.indexOf("<id>");
								System.out.println(val.substring(index1+4,val.indexOf("</id>")));
								String productNumber = val.substring(index1+4,val.indexOf("</id>"));
								index1 = val.indexOf("<quantity>");
								System.out.println(val.substring(index1+10,val.indexOf("</quantity>")));
								String quantity = val.substring(index1+10,val.indexOf("</quantity>"));
								val = val.substring(val.indexOf("</quantity>")+11);
								System.out.println("MESSAGEEEEEEEEEE IS: "+message);
								System.out.println("Beleg Nummber: "+XMLElementExtractor.extractXmlElementValue(message, "nummer"));
								System.out.println("Comment is: "+XMLElementExtractor.extractXmlElementValue(req, "commentText"));
								soapResponse = soapConnection.call(createSOAPRequestForAddItems(XMLElementExtractor.extractXmlElementValue(message, "nummer"), productNumber, quantity, XMLElementExtractor.extractXmlElementValue(req, "commentText")), url);
								
							}
							soapResponse = soapConnection.call(createSOAPRequestForReleaseTransaction(XMLElementExtractor.extractXmlElementValue(message, "nummer")), url);
			            	
			            	
			            	//System.out.println(req);
			            	//String val = UUID.randomUUID().toString();
			            	//System.out.println("Val: "+val);
			            	//String body = "<ReceiveTDOrder><isErrorOccured>false</isErrorOccured><message>Your request has been received</message><OrderId>"+val+"</OrderId></ReceiveTDOrder>";
			            	arg0.getOut().setBody("<ticketNumber>"+XMLElementExtractor.extractXmlElementValue(message, "nummer")+"</ticketNumber>");
		            	}
	            }else if(Constants.FETCHDYNAMICMENU.equalsIgnoreCase(flow)){
	            	System.out.println(callProducts(arg0));
	            }else if(Constants.COMMODITYGROUPS.equalsIgnoreCase(flow)){
	            	System.out.println(callCommodityGroups(arg0));
	            }
	            soapConnection.close();
	            //arg0.getOut().setBody(body);
	            if(isNotValidResLookUp){
	            	String body = "{\"ServiceError\":{\"faultstring\":\"An exception has occured.\",\"faultreason\":\"Please provide any of these three: (1). Reservation Confirmation Number (2). Last Name AND Last 4 digits of Credit Card (3). Hotel Loyalty Number\"}}";
		            soapConnection.close();
		            arg0.getOut().setBody(body);
	            }
	        } catch (Exception e) {
	            System.err.println("Error occurred while sending SOAP Request to Server");
	            e.printStackTrace();
	            String mesg = "DynamicRouteProcessor: process: "+e.toString();
	            if(1 == new SendEmail().sendEmail(emailSource.getHOST(), emailSource.getFROM_EMAIL(), emailSource.getADMIN_EMAIL(), emailSource.getPASS(), emailSource.getPORT(), null, "Exception occured at DynamicRouteProcessor.", mesg,emailSource.getFROM_NAME())){
					arg0.getOut().setBody("<Message><Failure>An exception has occured. An email is sent to Admin.</Failure></Message>");
				}else{
					arg0.getOut().setBody("<Message><Failure>An exception has occured. Email sending to Admin failed too.</Failure></Message>");
				}
	        }
	        
			//*******************************************
		}else{
			//Email Admin
			String mesg = "Can not connect to the URL at: "+url;
			if(1 == new SendEmail().sendEmail(emailSource.getHOST(), emailSource.getFROM_EMAIL(), emailSource.getADMIN_EMAIL(), emailSource.getPASS(), emailSource.getPORT(), null, "Provided URL not found.", mesg,emailSource.getFROM_NAME())){
				arg0.getOut().setBody("<Message><Failure>The PMS point couldn't connect at the URL. An email is sent to Admin.</Failure></Message>");
			}else{
				arg0.getOut().setBody("<Message><Failure>The PMS point couldn't connect at the URL. Email sending to Admin failed too.</Failure></Message>");
			}
		}
	}
	
	
	private String callCommodityGroups(Exchange arg0){
		String urLocator = "jetty:"+Constants.HOST+Constants.TOKEN+"/commodityGroups/updates/0";// REST URL here
    	
    	//Convert body to json and add in body.
    	arg0.getOut().setHeader(Exchange.HTTP_METHOD, "GET");
    	String val = (String) arg0.getContext().createProducerTemplate().requestBody(urLocator);
    	System.out.println("RESPONSE IS: "+val);
    	// Convert response into XML
    	String xmL = "<commodityGroups><message>"+val+"</message></commodityGroups>";
    	arg0.getOut().setBody(xmL);
    	//System.out.println("here");
		return val;
	}
	
	private String callProducts(Exchange arg0){
		String urLocator = "jetty:"+Constants.HOST+Constants.TOKEN+"/products/updates/0";// REST URL here
    	
    	//Convert body to json and add in body.
    	arg0.getOut().setHeader(Exchange.HTTP_METHOD, "GET");
    	String val = (String) arg0.getContext().createProducerTemplate().requestBody(urLocator);
    	System.out.println("RESPONSE IS: "+val);
    	// Convert response into XML
    	String xmL = "<products><message>"+val+"</message></products>";
    	arg0.getOut().setBody(xmL);
    	//System.out.println("here");
		return val;
	}
	
	private boolean isConnectable(String ur){
		boolean flag = false;
		int i = 0;
		while(i<5){
			try {
					   URL u = new URL ( ur );
					   HttpURLConnection huc = ( HttpURLConnection )  u.openConnection ();
					   huc.setRequestMethod ("GET");
						   System.out.println("Try No: "+(i+1));
						   huc.connect () ;
						   flag = true;
					   int code = huc.getResponseCode ( ) ;
					   System.out.println(code);
					   break;
				} catch (MalformedURLException e) {
					//e.printStackTrace();
				} catch (ProtocolException e) {
					//e.printStackTrace();
				} catch (IOException e) {
					//e.printStackTrace();
				}
			i++;
		}
		if(flag){
			System.out.println("Done");
		}else{
			System.out.println("Couldn't connect.");
		}
		return flag;
	}
	
	private static SOAPMessage createSOAPRequestForCreateAndLockTransaction(String value) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        String serverURI = "http://orderpad.korona.combase.de/";
        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("ord", serverURI);
        String orderNumber=XMLElementExtractor.extractXmlElementValue(value, "order_number");
        System.out.println("VALUE IS: 1"+orderNumber);
        
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("createAndLockBeleg", "ord");
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("clientId");
        soapBodyElem1.addTextNode(XMLElementExtractor.extractXmlElementValue(value, "terminalId"));
        SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("auftragsnummer");
    	soapBodyElem2.addTextNode(orderNumber);
        SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("kassierernummer");
        soapBodyElem3.addTextNode(XMLElementExtractor.extractXmlElementValue(value, "tableNumber"));
        System.out.println("VALUE IS: 2");
        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", serverURI  + "createAndLockBeleg");
        System.out.println("VALUE IS: 3");
        soapMessage.saveChanges();
        
        /* Print the request message */
        System.out.print("Request SOAP Message = ");
        soapMessage.writeTo(System.out);
        System.out.println();

        return soapMessage;
    }
	
	private static SOAPMessage createSOAPRequestForAddItems(String belegnummer, String artikelnummer, String menge, String infotexte) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        String serverURI = "http://orderpad.korona.combase.de/";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("ord", serverURI);
        
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("addArtikelposten", "ord");
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("clientId");
        soapBodyElem1.addTextNode(Constants.CLIENTID);
        SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("belegnummer");
    	soapBodyElem2.addTextNode(belegnummer);
        SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("kassierernummer");
        soapBodyElem3.addTextNode(Constants.KESSIERNUMBER);
        SOAPElement soapBodyElem4 = soapBodyElem.addChildElement("artikelnummer");
        soapBodyElem4.addTextNode(artikelnummer);
        SOAPElement soapBodyElem5 = soapBodyElem.addChildElement("menge");
        soapBodyElem5.addTextNode(menge);
        SOAPElement soapBodyElem6 = soapBodyElem.addChildElement("infotexte");
        soapBodyElem6.addTextNode(infotexte);

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", serverURI  + "addArtikelposten");

        soapMessage.saveChanges();

        /* Print the request message */
        System.out.print("Request SOAP Message = ");
        soapMessage.writeTo(System.out);
        System.out.println();

        return soapMessage;
    }
	
	private static SOAPMessage createSOAPRequestForReleaseTransaction(String belegnummer) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        String serverURI = "http://orderpad.korona.combase.de/";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("ord", serverURI);
        
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("releaseBeleg", "ord");
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("clientId");
        soapBodyElem1.addTextNode(Constants.CLIENTID);
        SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("belegnummer");
    	soapBodyElem2.addTextNode(belegnummer);

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", serverURI  + "releaseBeleg");

        soapMessage.saveChanges();

        /* Print the request message */
        System.out.print("Request SOAP Message = ");
        soapMessage.writeTo(System.out);
        System.out.println();

        return soapMessage;
    }

    /**
     * Method used to print the SOAP Response
     */
    private static String printSOAPResponse(SOAPMessage soapResponse) throws Exception {
    	if(soapResponse==null)
    		return "";
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        Source sourceContent = soapResponse.getSOAPPart().getContent();
        System.out.print("\nResponse SOAP Message = ");
        StreamResult result = new StreamResult(System.out);
        transformer.transform(sourceContent, result);
        
        System.out.println();
        StringWriter writer = new StringWriter();
        transformer.transform(sourceContent, new StreamResult(writer));
        String output = writer.toString();
        int ind = output.indexOf("<S");
        System.out.println("NOW IS: "+output.substring(ind));
        return output;
    }

}
