package com.integration.td.router;


import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spring.Main;

import com.integration.td.constants.Constants;
import com.integration.td.exception.CustomExceptionProcessor;
import com.integration.td.processes.JMSProcessor;
import com.integration.td.processes.RestProcessor;
public class IntegrationRouteBuilder extends RouteBuilder {

	private final String HOSTNAME = "smtp.gmail.com";
	private final String PORT = "587";
	private final String PASSWORD = "rahat547";
	private final String USERNAME = "igeintegration@gmail.com";
	private final String FROM = "igeintegration@gmail.com";
	private final String TO = "rahat.jaan@gmail.com";
	
	public static void main(String[] args) throws Exception{
        new Main().run(args);
    }
	
	
	public void configure() {

		onException(Exception.class).handled(false).process(new CustomExceptionProcessor());
		
		receiveTDOrder();// Received Touch Dine Order
		fetchDynamicMenu();//Fetch dynamic menu from korona cloud
		commodityGroups();//Fetch all commodity groups
	}
	
	
	private void receiveTDOrder() {
		Map<String, String> xmlJsonOptions = new HashMap<String, String>();
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.ENCODING, "UTF-8");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.ROOT_NAME, "receiveTDOrder");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.FORCE_TOP_LEVEL_OBJECT, "true");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.SKIP_NAMESPACES, "true");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.REMOVE_NAMESPACE_PREFIXES, "true");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.EXPANDABLE_PROPERTIES, "d e");

		
		from("jetty:http://0.0.0.0:8888/receivetdorder")
		.unmarshal().xmljson()	
		.beanRef("inRoomDiningProcessor")	
		.choice()
		.when(simple("${in.body.tenant.outboundType} == '404'"))
		.beanRef("responseProcessor")
		.when(simple("${in.body.tenant.outboundType} == '1'"))
		.setHeader("OutboundUrl").simple("${in.body.tenant.outboundUrl}")
		.setHeader("flow").constant(Constants.RECEIVETDORDER)
		.setHeader("CamelHttpMethod").constant("POST")
		.setHeader("Content-Type").constant("application/x-www-form-urlencoded")
		.setBody(simple("payload=${in.body}"))
		//.to("http://localhost:8080/POSMockup/InRoomDining")
		//.process(new DynamicRouteProcessor())
		.beanRef("dynamicRouteBuilder")
		.marshal().xmljson(xmlJsonOptions)
		.process(new Processor(){
			public void process(Exchange arg0) throws Exception {
				System.out.println(arg0.getIn().getBody().toString());
			}
		})
		//.to("uri:"+simple("${in.body.tenant.outboundUrl}"))
		.when(simple("${in.body.tenant.outboundType} == '2'"))
		.setBody(this.body())
		.to("jms:orders")
		.when(simple("${in.body.tenant.outboundType} == '3'"))
		.setHeader("subject", constant("TEST"))
		.to("smtp://" + HOSTNAME + ":" + PORT + "?password=" + PASSWORD
				+ "&username=" + USERNAME + "&from=" + FROM + "&to="
				+ TO + "&mail.smtp.starttls.enable=true")
		.otherwise()
		.beanRef("responseProcessor");
		
	}
	
	private void fetchDynamicMenu() {
		Map<String, String> xmlJsonOptions = new HashMap<String, String>();
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.ENCODING, "UTF-8");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.ROOT_NAME, "receiveTDOrder");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.FORCE_TOP_LEVEL_OBJECT, "true");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.SKIP_NAMESPACES, "true");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.REMOVE_NAMESPACE_PREFIXES, "true");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.EXPANDABLE_PROPERTIES, "d e");

		
		from("jetty:http://0.0.0.0:8888/fetchdynamicmenu")
		.unmarshal().xmljson()	
		.beanRef("inRoomDiningProcessor")	
		.choice()
		.when(simple("${in.body.tenant.outboundType} == '404'"))
		.beanRef("responseProcessor")
		.when(simple("${in.body.tenant.outboundType} == '1'"))
		.setHeader("OutboundUrl").simple("${in.body.tenant.outboundUrl}")
		.setHeader("flow").constant(Constants.FETCHDYNAMICMENU)
		.setHeader("CamelHttpMethod").constant("POST")
		.setHeader("Content-Type").constant("application/x-www-form-urlencoded")
		.setBody(simple("payload=${in.body}"))
		//.to("http://localhost:8080/POSMockup/InRoomDining")
		//.process(new DynamicRouteProcessor())
		.beanRef("dynamicRouteBuilder")
		.marshal().xmljson(xmlJsonOptions)
		.process(new Processor(){
			public void process(Exchange arg0) throws Exception {
				System.out.println(arg0.getIn().getBody().toString());
			}
		})
		//.to("uri:"+simple("${in.body.tenant.outboundUrl}"))
		.when(simple("${in.body.tenant.outboundType} == '2'"))
		.setBody(this.body())
		.to("jms:orders")
		.when(simple("${in.body.tenant.outboundType} == '3'"))
		.setHeader("subject", constant("TEST"))
		.to("smtp://" + HOSTNAME + ":" + PORT + "?password=" + PASSWORD
				+ "&username=" + USERNAME + "&from=" + FROM + "&to="
				+ TO + "&mail.smtp.starttls.enable=true")
		.otherwise()
		.beanRef("responseProcessor");
		
	}
	
	private void commodityGroups() {
		Map<String, String> xmlJsonOptions = new HashMap<String, String>();
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.ENCODING, "UTF-8");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.ROOT_NAME, "receiveTDOrder");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.FORCE_TOP_LEVEL_OBJECT, "true");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.SKIP_NAMESPACES, "true");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.REMOVE_NAMESPACE_PREFIXES, "true");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.EXPANDABLE_PROPERTIES, "d e");

		
		from("jetty:http://0.0.0.0:8888/commoditygroups")
		.unmarshal().xmljson()	
		.beanRef("inRoomDiningProcessor")	
		.choice()
		.when(simple("${in.body.tenant.outboundType} == '404'"))
		.beanRef("responseProcessor")
		.when(simple("${in.body.tenant.outboundType} == '1'"))
		.setHeader("OutboundUrl").simple("${in.body.tenant.outboundUrl}")
		.setHeader("flow").constant(Constants.COMMODITYGROUPS)
		.setHeader("CamelHttpMethod").constant("POST")
		.setHeader("Content-Type").constant("application/x-www-form-urlencoded")
		.setBody(simple("payload=${in.body}"))
		//.to("http://localhost:8080/POSMockup/InRoomDining")
		//.process(new DynamicRouteProcessor())
		.beanRef("dynamicRouteBuilder")
		.marshal().xmljson(xmlJsonOptions)
		.process(new Processor(){
			public void process(Exchange arg0) throws Exception {
				System.out.println(arg0.getIn().getBody().toString());
			}
		})
		//.to("uri:"+simple("${in.body.tenant.outboundUrl}"))
		.when(simple("${in.body.tenant.outboundType} == '2'"))
		.setBody(this.body())
		.to("jms:orders")
		.when(simple("${in.body.tenant.outboundType} == '3'"))
		.setHeader("subject", constant("TEST"))
		.to("smtp://" + HOSTNAME + ":" + PORT + "?password=" + PASSWORD
				+ "&username=" + USERNAME + "&from=" + FROM + "&to="
				+ TO + "&mail.smtp.starttls.enable=true")
		.otherwise()
		.beanRef("responseProcessor");
		
	}
	
}
