/*******************************************************************************
 * Copyright (c) 2015 Institute for Pervasive Computing, ETH Zurich and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *    Matthias Kovatsch - creator and main architect
 ******************************************************************************/
package org.eclipse.californium.examples;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.server.ServerMessageDeliverer;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.examples.CredentialsUtil.Mode;
import org.eclipse.californium.proxy.DirectProxyCoapResolver;
import org.eclipse.californium.proxy.ProxyHttpServer;
import org.eclipse.californium.proxy.resources.ForwardingResource;
import org.eclipse.californium.proxy.resources.ProxyCoapClientResource;
import org.eclipse.californium.proxy.resources.ProxyHttpClientResource;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig.Builder;
import org.eclipse.californium.scandium.dtls.pskstore.StaticPskStore;

/**
 * Http2CoAP: Insert in browser:
 *     URI: http://localhost:8080/proxy/coap://localhost:PORT/target
 * 
 * CoAP2CoAP: Insert in Copper:
 *     URI: coap://localhost:PORT/coap2coap
 *     Proxy: coap://localhost:PORT/targetA
 *
 * CoAP2Http: Insert in Copper:
 *     URI: coap://localhost:PORT/coap2http
 *     Proxy: http://lantersoft.ch/robots.txt
 */
public class ExampleCrossProxy {
	
	private static final int PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);

	private CoapServer targetServerA;
	private static String identityClient = "user"; 
    private static String pskClient = "password"; 
    private static String identityServer = "user"; 
    private static String pskServer = "password"; 

   
    InetSocketAddress localAddressSecure; //Address of DTLS listener 
	public static final int DTLS_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_SECURE_PORT);
	public static final List<Mode> SUPPORTED_MODES = Arrays
			.asList(new Mode[] { Mode.PSK, Mode.NO_AUTH });
	
	
	public ExampleCrossProxy() throws IOException {
		
		
		// Create CoAP Server on PORT with proxy resources form CoAP to CoAP and HTTP
		targetServerA = new CoapServer(PORT);
		//DTLS initialization
		localAddressSecure = new InetSocketAddress("0.0.0.0", DTLS_PORT);
		DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder();
		builder.setAddress(new InetSocketAddress(DTLS_PORT));
		builder.setPskStore(new StaticPskStore(identityClient, pskClient.getBytes()));

		DTLSConnector connector = new DTLSConnector(builder.build());
		CoapEndpoint.Builder coapBuilder = new CoapEndpoint.Builder();
		coapBuilder.setConnector(connector);
		targetServerA.addEndpoint(coapBuilder.build());
      
		
		ForwardingResource coap2coap = new ProxyCoapClientResource(targetServerA.getRoot().getURI(),identityServer, pskServer);
		targetServerA.setMessageDeliverer(new ServerMessageDeliverer(coap2coap));

		targetServerA.start();


		
		System.out.println("CoAP resource \"target\" available over HTTP at: http://localhost:8080/proxy/coap://localhost:PORT/target");
	}
	
	/**
	 * A simple resource that responds to GET requests with a small response
	 * containing the resource's name.
	 */
	private static class TargetResource extends CoapResource {
		
		private int counter = 0;
		
		public TargetResource(String name) {
			super(name);
		}
		
		@Override
		public void handleGET(CoapExchange exchange) {
			exchange.respond("Response "+(++counter)+" from resource " + getName());
		}
	}
	
	public static void main(String[] args) throws Exception {
		new ExampleCrossProxy();
	}

}
