package edu.tamu.aser.race;

public class Race {

    private int ID1, ID2;
    public Race(int ID1, int ID2)
    {
    	if(ID1<ID2)
    	{
        this.ID1 = ID1;
        this.ID2 = ID2;
    	}
    	else
    	{
    		this.ID1= ID2;
    		this.ID2 = ID1;
    	}
    }
    
    @Override
    public int hashCode()
    {
        return ID1+ID2;
    }
    @Override
    public boolean equals(Object o)
    {
        if(o instanceof Race)
        {
            if((((Race) o).ID1 == ID1
                    &&((Race) o).ID2 == ID2))
                return true;        
        }
        
        return false;
    }
    
    @Override
    public String toString()
    {
        return ID1+"-"+ID2;
    }
}
