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

import java.util.Arrays;
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
public class EvalOscoreClient {
	
	private final static HashMapCtxDB db = HashMapCtxDB.getInstance();
	private final static String baseUri = "coap://[fd00::302:304:506:708]";
	private final static AlgorithmID alg = AlgorithmID.AES_CCM_16_64_128;
	private final static AlgorithmID kdf = AlgorithmID.HKDF_HMAC_SHA_256;

	private final static byte[] master_secret = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B,
			0x0C, 0x0D, 0x0E, 0x0F, 0x10 };
	private final static byte[] master_salt = { (byte) 0x9e, (byte) 0x7c, (byte) 0xa9, (byte) 0x22, (byte) 0x23,
			(byte) 0x78, (byte) 0x63, (byte) 0x40 };
	private final static byte[] sid = { (byte) 0x43 };
	private final static byte[] rid = { (byte) 0x53 };
	
	private static OSCoreCtx ctx_A;
	
	
	public static void main(String[] args) throws OSException {
		ctx_A = new OSCoreCtx(master_secret, true, alg, sid, rid, kdf, 32, master_salt, null);

		//Avoid CoAP retransmissions (Should no longer be needed)
		NetworkConfig config = NetworkConfig.getStandard();
		config.setInt(NetworkConfig.Keys.MAX_RETRANSMIT, 0);
		//Set timeout to be 4 seconds
		config.setInt(NetworkConfig.Keys.ACK_TIMEOUT, 4000);
		
		db.addContext("coap://[fd00::212:4b00:14b5:d967]", ctx_A);
		db.addContext(baseUri, ctx_A);
		//Util.printOSCOREKeyInformation(db, baseUri);

		String resourceUri = "/test/caps";
		//String proxyUri = "coap://127.0.0.1";
		String uriProxy = "coap://[fd00::212:4b00:14b5:d967]/test/caps";
		OscoreClient c = new OscoreClient(uriProxy);
		//OscoreClient c = new OscoreClient(baseUri + resourceUri);
		
	/*	for(int payload_len = 5; payload_len < 10; payload_len += 5) {
			System.out.println(payload_len);
			Request r = new Request(Code.POST);
			byte[] payload = new byte[payload_len];
			Arrays.fill(payload, (byte)0x61);
			r.setPayload(payload);
			r.getOptions().setProxyUri("coap://[fd00::302:304:506:708]/test/caps"); //test/caps
			//r.getOptions().setProxyScheme("/coap2coap");
			r.setURI("coap://127.0.0.1/coap2coap");
			CoapResponse resp = c.advanced(r);
			if(resp == null) {
				System.out.println("ERROR: Client application received no response!");
				return;
			}
			if( resp.getPayload().length != payload_len) {
				System.out.println("FUCKUP!" + payload_len);

				
			}
			for( int i = 0; i < resp.getPayload().length; i++) {
				System.out.print(resp.getPayload()[i]);
			}
			System.out.println("");	
		} */
		
		Request r = new Request(Code.POST);
		byte[] payload = new byte[1];
		Arrays.fill(payload, (byte)0x61);
		r.setPayload(payload);
		//r.getOptions().setProxyUri("coaps://[fd00::302:304:506:708]/test/caps");
		CoapResponse resp = c.advanced(r);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long[] timelist = new long[300];
		//for(int payload_len = 5; payload_len < 125; payload_len += 5) {
		for (int i = 0 ; i<300; i++) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}Results
			125.132515 117.06956 78.10317 48.253902 47.92449 65.166016 88.20023 53.66232 72.86549 86.44693 96.81325 39.103985 63.12325 54.764225 57.817284 62.27791 70.50415 78.53654 102.899185 56.979115 70.48769 63.60038 71.52514 86.23469 71.923546 40.370903 94.915504 71.8638 96.5698 86.83074 63.06249 95.68048 125.658005 77.35413 88.58531 78.629616 63.24868 80.028946 95.20123 88.8168 94.51699 34.6982 29.186537 96.12141 95.31157 101.100655 95.725395 93.66593 57.93152 71.49829 115.88468 81.254425 55.534977 108.65542 95.29572 96.72042 71.16995 62.97215 108.35429 79.57715 57.69866 77.236145 36.15649 85.19731 70.88637 32.278866 71.98958 48.460495 78.729836 40.689713 94.48106 31.355688 79.83748 71.29889 79.83444 39.711876 48.33022 61.491547 72.55687 88.01905 70.2252 79.99361 71.10514 78.846886 48.004868 86.52608 31.615938 64.27336 56.442493 54.323357 57.4486 71.22215 109.6529 57.762974 63.423607 61.64876 56.423237 72.4036 64.3757 78.122856 47.773666 55.442078 32.921776 86.562706 48.21142 79.85546 71.28403 102.63133 39.77119 64.09852 117.63537 87.57067 71.818245 93.202126 95.97994 62.748585 47.121807 56.257896 88.486206 85.004295 103.31521 78.98301 31.768427 87.85925 32.725883 32.290955 64.40803 94.237335 93.48481 63.693455 80.72059 70.133095 72.29621 71.558105 101.41727 103.9689 102.42247 32.10813 63.756577 65.53649 117.20354 111.29036 47.81736 40.555424 107.79605 50.017876 101.760086 102.84418 56.07357 79.1618 55.850983 87.3676 71.427704 33.153378 39.98666 54.430035 71.97671 48.5546 31.121435 72.452675 110.77278 86.28026 87.94344 55.610233 62.99663 72.22943 78.86486 102.48586 63.991306 71.014 31.675055 71.29382 79.763855 70.31829 55.692642 96.802895 46.865734 87.83611 71.48257 55.090958 71.689835 32.423336 109.069664 30.16642 74.899765 70.17397 80.25764 118.22036 38.689816 34.246246 32.30884 101.28819 57.13485 95.28939 77.870384 72.05201 48.47655 54.492195 57.272408 78.82969 78.755875 73.07713 70.6304 78.67166 65.382385 63.663235 62.23781 103.30328 71.289696 63.408337 95.48359 72.19208 70.831085 64.67546 94.96001 78.91496 64.14681 56.225952 62.93371 103.39272 57.107113 70.999214 41.107048 86.52185 85.83571 88.52955 40.65903 69.48184 57.54007 110.521706 110.29624 32.387287 71.65421 64.45004 47.629536 94.417984 64.17108 47.864433 78.93361 55.534363 56.829548 118.71272 94.64718 79.497246 78.7563 110.487595 79.55586 55.364464 79.62861 71.915565 101.41728 49.83568 48.287304 63.366505 64.3901 55.597168 31.617369 63.33217 33.113174 56.138557 69.9656 41.060097 71.75503 39.160034 63.77006 96.20215 85.79862 64.88573 39.481346 71.675285 79.3367 94.711136 38.98082 72.22987 55.316013 55.898354 87.3783 70.044075 102.76884 86.43681 88.181915 55.120777 41.297947 78.802956 70.82558 63.792 29.536165 50.66309 63.9702 71.359215 79.36211 71.49076 32.766426 86.59775 70.82394 71.3561 87.917755 55.414806 64.31117 62.914722 
			avrage 71.07641199333334
			sd 21.676741875294024
			int payload_len = 1;
			System.out.println(payload_len);
			r = new Request(Code.POST);
			payload = new byte[payload_len];
			Arrays.fill(payload, (byte)0x61);
			r.setPayload(payload);
			//r.getOptions().setProxyUri("coaps://[fd00::302:304:506:708]/test/caps");
			long startTime = System.nanoTime();
			resp = c.advanced(r);
			long endTime = System.nanoTime();
			long timeElapsed = endTime - startTime;
			timelist[i] = timeElapsed;
			if(resp == null) {
				System.out.println("ERROR: Client application received no response!");
				return;
			}
			if( resp.getPayload().length != payload_len) {
				System.out.println("FUCKUP!" + payload_len);
			}
		}
		System.out.println("Done!");
		System.out.println("Results");
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
	/*	Request r = new Request(Code.POST);
		byte[] payload = {0x61, 0x61, 0x61, 0x61}; 
		r.setPayload(payload);
		
		CoapResponse resp = c.advanced(r);
		
		System.out.println("Original CoAP message:");
		System.out.println("Uri-Path: " + c.getURI());
		System.out.println(Utils.prettyPrint(r));
		System.out.println("");
		
		if(resp == null) {
			System.out.println("ERROR: Client application received no response!");
			return;
		}
		
		System.out.println("Parsed CoAP response: ");
		System.out.println("Response code:\t" + resp.getCode());
		System.out.println("Content-Format:\t" + resp.getOptions().getContentFormat());
		System.out.println("Payload:\t" + resp.getResponseText()); */
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
