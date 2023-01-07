package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.stream.IntStream;

import javax.json.Json;

public class DHKEHelper 
{
	private Client client = null;
	private BigInteger p = null;
	private BigInteger alpha = null;
	private BigInteger privateKey = null;
	private BigInteger publicKey = null;
	private BigInteger commonKey = null;
	private boolean commonKeyFlag = false;
	public DHKEHelper(Client client) 
	{ 
		this.client = client;
	}
	
	String initDomainParams(StringWriter sW, BufferedReader bR) throws IOException 
	{
		System.out.println("[system]: enter name, (agreed on) p, (agreed on) alpha");
		String[] values = bR.readLine().split(" ");
		client.setName(values[0]);
		String nameInitial = values[0].substring(0, 1).toLowerCase();
		boolean initFlag = false;
		if(p== null) 
		{
			p = new BigInteger(values[1]);
			setAlpha(new BigInteger(values[2]));
			initFlag = true;
		}
		Json.createWriter(sW).writeObject(Json.createObjectBuilder().add("name",  client.getName())
																	.add("p",  p.toString())
																	.add("alpha",  getAlpha().toString())
																	.add("initFlag",  initFlag).build());
		client.getPrintWriter().println(sW);
		return nameInitial;
			
	}
	
	void calcPublicKey(StringWriter sW, BufferedReader bR, String initial) throws IOException 
	{
		System.out.println("[system]: pick secret # '" + initial + "' from set {2, 3, ..., " + p.add(new BigInteger("-2")) + "}");
		setPrivateKey(new BigInteger(bR.readLine()));
		BigInteger pkValue = getAlpha().modPow(getPrivateKey(),  p);
		System.out.println("[" + client.getName() + "]: " + "p=" + p + ", alpha = " + getAlpha() + ", " + initial + "=" + getPrivateKey());
		System.out.println("[" + client.getName() + "]: " + initial.toUpperCase() + " <congruent> " + "alpha^" + initial.toLowerCase() + "mod p = " + getAlpha() + "^" + getPrivateKey() + " mod " + p + " = " + pkValue + " mod " + p);
		Json.createWriter(sW).writeObject(Json.createObjectBuilder().add("name",  client.getName())
																	.add("p",  p.toString())
																	.add("alpha",  getAlpha().toString())
																	.add("otherParty",  client.getOtherPartyInitial().substring(0, 1).toString())
																	.add("publicKeyValue",  pkValue.toString()).build());
		client.getPrintWriter().println(sW);
	}
	
	BigInteger calcCommonKey(StringWriter sW, BufferedReader bR, String initial) throws IOException 
	{
		bR.read();
		BigInteger s = getPublicKey().modPow(getPrivateKey(),  p);
		System.out.println("[" + client.getName() + "]: "+ "s <congruent> " + client.getOtherPartyInitial() + "^" + initial.toLowerCase() + "mod p " + " = " + getPublicKey() + "^" + getPrivateKey() + " mod " + p + " = " + s + " mod " + p);
		Json.createWriter(sW).writeObject(Json.createObjectBuilder().add("name",  client.getName())
																	.add("ready",  true).build());
		if(!isCommonKeyFlag()) client.getPrintWriter().println(sW);
		return s;
	}
	
	String encryptMessage(String message) 
	{
		String[] values = message.split(" ");
		BigInteger[] returnValues = new BigInteger[values.length];
		IntStream.range(0,  values.length).forEach(i -> returnValues[i] = (new BigInteger(values[i]).multiply(commonKey)).mod(p));
		StringBuffer c = new StringBuffer();
		for (int i = 0; i< returnValues.length; i++) 
		{ 
			c.append(returnValues[i] + " ");
		}
		return c.toString();
	}
	
	BigInteger[] decryptMessage(String message, BigInteger commonKey) 
	{
		String[] values = message.split(" ");
		BigInteger[] returnValues = new BigInteger[values.length];
		for (int i = 0; i < values.length; i++) 
		{
			returnValues[i] = (new BigInteger(values[i])).multiply(commonKey.modInverse(p)).mod(p);
			
		}
		return returnValues;
	}
	
	public void setP(BigInteger p) 
	{
		this.p = p;
	}
	
	public BigInteger getP() 
	{ 
		return p;
	}
	
	public BigInteger getAlpha() 
	{ 
		return alpha;
	}
	
	public void setAlpha(BigInteger alpha) 
	{ 
		this.alpha = alpha;
	}
	
	public BigInteger getPrivateKey() 
	{ 
		return privateKey;
	}
	
	public void setPrivateKey(BigInteger privateKey) 
	{ 
		this.privateKey = privateKey;
	}
	
	public BigInteger getPublicKey() 
	{ 
		return publicKey;
	}
	
	public void setPublicKey(BigInteger publicKeyValue) 
	{ 
		this.publicKey = publicKeyValue;
	}
	
	public BigInteger getCommonKey() 
	{ 	
		return commonKey;
	}
	
	public void setCommonKey(BigInteger commonKey) 
	{
		this.commonKey = commonKey;
	}
	
	public boolean isCommonKeyFlag() 
	{ 
		return commonKeyFlag; 
	}
	
	public void setCommonKeyFlag(boolean ommonFlag) 
	{ 
		this.commonKeyFlag = commonKeyFlag;
	}

}
