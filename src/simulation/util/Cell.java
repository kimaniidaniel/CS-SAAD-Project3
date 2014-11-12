package simulation.util;

import java.util.Iterator;

public interface Cell<T> {
	
	public void setTop(T top);
	
	public T getTop();
	
	public void setBottom(T bottom);
	
	public T getBottom();
	
	public void setRight(T right);
	
	public T getRight();
	
	public void setLeft(T left);
	
	public T getLeft();

	public float getTemp();
	
	public void setTemp(float temp);
	
	public void swapTemp();
	
	public void visited(boolean visited);
	
	public Iterator<T> getChildren(boolean unvisited);
	
	public void setX(int x);
	
	public int getX();
	
	public void setY(int y);
	
	public int getY();
}