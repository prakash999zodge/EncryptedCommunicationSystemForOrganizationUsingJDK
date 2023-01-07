package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.util.stream.IntStream;

import javax.json.Json;

public class Client 
{
	private String name = null;
	private String otherPartyInitial = null;
	private PrintWriter printWriter;

	public static void main(String[] args) throws IOException 
	{
		Client client = new Client();
		DHKEHelper dhkeHelper = new DHKEHelper(client);
		Socket socket = new Socket("localhost", 4444);
		new ClientThread(socket, client, dhkeHelper).start();
		client.printWriter = new PrintWriter(socket.getOutputStream(), true);
		if(client.handlePreCommunicate(dhkeHelper))
		{
			client.handleCommunicate(dhkeHelper);
		}
	}
	
	public boolean handlePreCommunicate(DHKEHelper dhkeHelper) throws IOException 
	{
		boolean readyToCommunicateFlag = false;
		BufferedReader bR = new BufferedReader(new InputStreamReader(System.in));
		String nameInitial = null;
		BigInteger s = null;
		boolean flag = true;
		while(flag) 
		{
			StringWriter sW = new StringWriter();
			if(getName() == null) {nameInitial = dhkeHelper.initDomainParams(sW,  bR);}
			else if(dhkeHelper.getPrivateKey() == null && getOtherPartyInitial() != null) 
			{
				dhkeHelper.calcPublicKey(sW,  bR,  nameInitial);
			}
			else if(bR.ready()) 
			{
				s = dhkeHelper.calcCommonKey(sW,  bR,  nameInitial);
				dhkeHelper.setCommonKey(s);
				readyToCommunicateFlag = true;
				break;
			}
		}
		return readyToCommunicateFlag;
	}
	
	private void handleCommunicate(DHKEHelper dhkeHelper) throws IOException 
	{
		BufferedReader bR = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("[system]: ready to send & receive message, or exit");
		while (true) 
		{
			String value = bR.readLine();
			if(value.equals("e")) 
			{
				System.exit(0);
			}
			StringBuffer asciiMessage = new StringBuffer();
			IntStream.range(0,  value.length()).forEach( x -> asciiMessage.append(characterToAscii(value.charAt(x)) + " "));
			System.out.println("[" + getName() + "]: map char to ascii message ==> " + asciiMessage.toString());
			String c = dhkeHelper.encryptMessage(asciiMessage.toString());
			System.out.println("[" + getName() + "]: encrypt ascii & obtain ciphertext message ==> " +c);
			System.out.println("[" + getName() + "]: send encrypted message\n ");
			StringWriter sW = new StringWriter();
			Json.createWriter(sW).writeObject(Json.createObjectBuilder().add("name",  getName()).add("message", c.toString()).build());
			printWriter.println(sW);
		}
		
	}
	
	public String getName() 
	{ 
		return name;
	}
	
	public void setName(String name) 
	{ 
		this.name = name;
	}
	
	public String getOtherPartyInitial() 
	{	
		return otherPartyInitial;
	}
	
	public void setOtherPartyInitial(String otherPartyInitial) 
	{ 
		this.otherPartyInitial = otherPartyInitial;
	}
	
	public PrintWriter getPrintWriter() 
	{ 
		return printWriter;
	}
	
	static int characterToAscii(char character) 
	{ 
		return (int) character;
	}
	
	static char asciiToCharacter(int ascii) 
	{ 
		return (char) ascii; 
	}

}
