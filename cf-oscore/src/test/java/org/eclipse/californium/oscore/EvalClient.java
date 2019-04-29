/*******************************************************************************
 * Copyright (c) 2018 RISE SICS and others.
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
 *    Tobias Andersson (RISE SICS)
 *    Rikard Höglund (RISE SICS)
 *    
 ******************************************************************************/
package org.eclipse.californium.oscore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LongSummaryStatistics;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;

import org.eclipse.californium.cose.AlgorithmID;

/**
 * OSCORE Client for interop testing
 * 
 * Following test spec:
 * https://ericssonresearch.github.com/OSCOAP/test-spec5.html
 * 
 * Author: Rikard Höglund
 *
 */
public class EvalClient {
	
	public final String testedProtocol = "coap"; //(coap||coaps||oscore)
	
	//oscore setup data
	private final static HashMapCtxDB db = HashMapCtxDB.getInstance();
	private final static String baseUri = "coap://[fd00::302:304:506:708]";
	private final static AlgorithmID alg = AlgorithmID.AES_CCM_16_64_128;
	private final static AlgorithmID kdf = AlgorithmID.HKDF_HMAC_SHA_256;
	private final static byte[] master_secret = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B,0x0C, 0x0D, 0x0E, 0x0F, 0x10 };
	private final static byte[] master_salt = { (byte) 0x9e, (byte) 0x7c, (byte) 0xa9, (byte) 0x22, (byte) 0x23, (byte) 0x78, (byte) 0x63, (byte) 0x40 };
	private final static byte[] sid = { (byte) 0x43 };
	private final static byte[] rid = { (byte) 0x53 };
	private final static int numberIterations = 100;
	
	
	public static void main(String[] args) throws OSException, InterruptedException {
		
		//OSCORE SETUP
		NetworkConfig config = NetworkConfig.getStandard();//Avoid CoAP retransmissions (Should no longer be needed)
		config.setInt(NetworkConfig.Keys.MAX_RETRANSMIT, 0);
		config.setInt(NetworkConfig.Keys.ACK_TIMEOUT, 4000);		//Set timeout to be 4 seconds
		OSCoreCtx ctx_A = new OSCoreCtx(master_secret, true, alg, sid, rid, kdf, 32, master_salt, null);;
		db.addContext("coap://[fd00::212:4b00:14b5:d967]", ctx_A); 		//contexts for oscore
		db.addContext(baseUri, ctx_A);

		//DTLS SETUP [TODO]
		
		//EXPERIMENT SETUP
		String uriServer = "coap://[fd00::212:4b00:14b5:d967]/test/caps";
		CoapClient c = new CoapClient(uriServer);
		Request r = new Request(Code.POST);
		
		//send channel opener
		byte[] payload = new byte[1];
		Arrays.fill(payload, (byte)0x61);
		r.setPayload(payload);
		CoapResponse resp = c.advanced(r); //send request
		
		//wait for a while
		Thread.sleep(1000);
	
		int[] payloadSizes = {1,16,32,48,64,80,96,112,128};
		HashMap<Integer,ArrayList<Long>> data = new HashMap<Integer,ArrayList<Long>>();
		
		for(int payloadSize : payloadSizes) {//try many sizes of payload
			data.put(payloadSize, new ArrayList<Long>());
			for (int i = 0 ; i<numberIterations; i++) {//try many times for stistical significance
			
				//create request and fill it up
				r = new Request(Code.POST);
				payload = new byte[payloadSize];
				Arrays.fill(payload, (byte)0x61);
				r.setPayload(payload);
				
				//send request and measure response time
				long startTime = System.nanoTime();
				resp = c.advanced(r);
				long endTime = System.nanoTime();
				long timeElapsed = endTime - startTime;
				
				data.get(payloadSize).add(timeElapsed);
				if(resp == null) {
					System.out.println("ERROR: Client application received no response!");
					return;
				}
				if( resp.getPayload().length != payloadSize) {
					System.out.println("FUCKUP!" + payloadSize);
				}
				
				Thread.sleep(100);
			}
			
		}
		System.out.println(data.toString());
		/*
		LongSummaryStatistics stat = new LongSummaryStatistics();
		for (int i = 0 ; i<300; i++) {
			System.out.print(timelist[i] / (float)1000000 + " ");
			stat.accept(timelist[i]);
		}
		System.out.println("");
		System.out.println("avrage "+  stat.getAverage()/(float)1000000);
		System.out.println("sd "+  calculateSD(timelist)/(float)1000000);
		try {
			Thread.sleep(100000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		*/
	
	}

	public static double calculateSD(long numArray[])
    {
        double sum = 0.0, standardDeviation = 0.0;
        int length = numArray.length;

        for(double num : numArray) {
            sum += num;
        }

        double mean = sum/length;

        for(double num: numArray) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation/length);
    }

	
	
	/**
	 * Separate class to handle an OSCORE client instance
	 * 
	 * @author segrid-2
	 *
	 */
	public static class OscoreClient
	{
		private String uri;
		private Code method;
		
		public CoapClient c;
		
		public OscoreClient(Code method, String uri)
		{	
			OSCoreCoapStackFactory.useAsDefault();
			c = new CoapClient(uri);
			
			this.method = method;
			this.uri = uri;
		}
		
		public OscoreClient(String uri)
		{
			this(null, uri);
		}
		
		public String getURI() {
			return uri;
		}
		
		/**
		 * Send a CoapRequest via OSCORE
		 * 
		 * @return Response to CoAP request
		 */
		CoapResponse send()
		{
			OSCoreCoapStackFactory.useAsDefault();
			CoapClient c = new CoapClient(uri);

			Request r = new Request(method);
			r.getOptions().setOscore(new byte[0]);
			CoapResponse resp = c.advanced(r);
			
			return resp;
		}
		
		/**
		 * Sends an arbitrary CoAP request using OSCORE
		 * 
		 * @return Response to CoAP request
		 */
		CoapResponse advanced(Request r)
		{
			if(!r.getOptions().hasOscore()) {
				r.getOptions().setOscore(new byte[0]);
			}
			CoapResponse resp = c.advanced(r);
			
			return resp;
			
		}

	}

}
