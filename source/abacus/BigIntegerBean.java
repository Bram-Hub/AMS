package abacus;

import java.math.BigInteger;

public class BigIntegerBean
{	
	BigInteger val = null;
	
	public BigIntegerBean() 
	{
		val = BigInteger.ZERO;
	}
	public BigIntegerBean(BigInteger i)
	{
		val = i;
	}
	
	public BigIntegerBean(String s)
	{
		val = new BigInteger(s);
	}
	
	public byte[] getVal()
	{
		return val.toByteArray();
	}
	
	public void setVal(byte[] newVal)
	{
		val = new BigInteger(newVal);
	}
}
