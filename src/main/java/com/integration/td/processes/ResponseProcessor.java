package com.integration.td.processes;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class ResponseProcessor implements Processor{
	public void process(Exchange exchange) throws Exception {
		exchange.getOut().setBody("{\"Error\":\"Tenant not Found.\"}");
		System.out.println("RESPONSE PROCESSOR: "+exchange.getIn().getBody().toString());
    }
}
