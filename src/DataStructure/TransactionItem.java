package DataStructure;

import java.util.Vector;

public class TransactionItem {
	String item = "";
	double itemWeight = 1;
	boolean isPublic = true;
	boolean isSensitive = false;
	boolean isPrivate = false;
	
	public TransactionItem(String is)
	{
		item = is;
	}
	
	public String getItem() {
		return item;
	}
	
	public TransactionItem()
	{
		
	}
	
	@Override
	public String toString()
	{
		return item;
	}
	public void setItem(String item) {
		this.item = item;
	}
	public double getItemWeight() {
		return itemWeight;
	}
	public void setItemWeight(double itemWeight) {
		this.itemWeight = itemWeight;
	}
	public boolean isPublic() {
		return isPublic;
	}
	public boolean isSensitive() {
		return isSensitive;
	}
	public boolean isPrivate() {
		return isPrivate;
	}
	
	public void SetProperty(boolean pub,boolean pri,boolean sen)
	{
		isPublic = pub;
		isPrivate = pri;
		isSensitive = sen;
	}
	
	public static Vector<TransactionItem> GetPublicItems(TransactionDataset D)
	{
		Vector<TransactionItem> res = new Vector<TransactionItem>();
		for (TransactionItem d : D.domain.GetData())
		{
			if (d.isPublic() == true)
				res.add(d);
		}
		return res;
	}
	
	public static Vector<TransactionItem> GetSensitiveItems(TransactionDataset D)
	{
		Vector<TransactionItem> res = new Vector<TransactionItem>();
		for (TransactionItem d : D.domain.GetData())
		{
			if (d.isSensitive() == true)
				res.add(d);
		}
		return res;
	}
	
	public static Vector<TransactionItem> GetNonpublicItems(TransactionDataset D)
	{
		Vector<TransactionItem> res = new Vector<TransactionItem>();
		for (TransactionItem d : D.domain.GetData())
		{
			if (d.isPrivate() == true)
				res.add(d);
		}
		return res;
	}

}
