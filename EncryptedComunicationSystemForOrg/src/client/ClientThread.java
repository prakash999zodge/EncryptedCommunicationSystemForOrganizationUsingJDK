package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Arrays;
import java.util.stream.IntStream;

import javax.json.Json;
import javax.json.JsonObject;

public class ClientThread extends Thread 
{
	private BufferedReader reader;
	private Client client;
	private DHKEHelper dhkeHelper;
	
	public ClientThread(Socket socket, Client client, DHKEHelper dhkeHelper) throws IOException 
	{
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.client = client;
		this.dhkeHelper = dhkeHelper;
	}
	
	public void run() 
	{
		while(true) 
		{
			JsonObject jsonObject = Json.createReader(reader).readObject();
			if(jsonObject.containsKey("p") && dhkeHelper.getP() == null) 
			{
				dhkeHelper.setP(new BigInteger(jsonObject.getString(("p"))));
				dhkeHelper.setAlpha(new BigInteger(jsonObject.getString(("alpha"))));
				client.setOtherPartyInitial(jsonObject.getString(("name")));
				System.out.println("[system]: enter name");
				
			} 
			else if(jsonObject.containsKey("publicKeyValue")) 
			{
				if(dhkeHelper.getPrivateKey()!= null) 
				{
					System.out.println("[system]: press enter to generate common key");
				}
				dhkeHelper.setPublicKey(new BigInteger(jsonObject.getString(("publicKeyValue"))));
				client.setOtherPartyInitial(jsonObject.getString("name").substring(0, 1).toUpperCase());
				
			} 
			else if(jsonObject.containsKey("ready") && !dhkeHelper.isCommonKeyFlag()) 
			{
				System.out.println("[system]: press enter to generate common key");
				dhkeHelper.setCommonKeyFlag(true);
				
			} 
			else if(jsonObject.containsKey("message")) 
			{
				handleIncomingMessage(jsonObject);
			}
		}
		
	}
	
	private void handleIncomingMessage(JsonObject jsonObject) 
	{
		String message = jsonObject.getString("message");
		String name = "["+client.getName()+"]:";
		System.out.println(name+" receive encrypted message ==> "+message);
		BigInteger[] returnValues = dhkeHelper.decryptMessage(message,  dhkeHelper.getCommonKey());
		System.out.println(name+" decrypt ciphertext & obtain ascii message ==> " + Arrays.toString(returnValues));
		StringBuffer stringMessage = new StringBuffer();
		IntStream.range(0,  returnValues.length).forEach(x -> stringMessage.append(Client.asciiToCharacter(returnValues[x].intValue())));
		System.out.println(name+" map ascii to char & obtain original message ==> "+ stringMessage.toString());
		System.out.println("["+jsonObject.getString("name") +"]: "+ stringMessage.toString());
		
	}

}
