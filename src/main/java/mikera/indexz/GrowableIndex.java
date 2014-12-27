package mikera.indexz;

public class GrowableIndex extends AIndex {

	private int[] data;
	private int count;
	
	@Override
	public int get(int i) {
		if ((i<0)||(i>=count)) throw new IndexOutOfBoundsException("Index: "+i);
		return data[i];
	}

	@Override
	public int length() {
		return count;
	}

	@Override
	public void set(int i, int value) {
		if ((i<0)||(i>=count)) {
			if (i==count) {
				append(i);
				return;
			}
			throw new IndexOutOfBoundsException("Index: "+i);
		}
		data[i]=value;
	}
	
	@Override
	public void copyTo(int[] array, int offset) {
		System.arraycopy(data, 0, array, offset, count);
	}

	public void append(int i) {
		ensureCapacity(count+1);
		data[count++]=i;
	}

	private void ensureCapacity(int capacity) {
		if (data.length>=capacity) return;
		
		int nLen=Math.max(capacity, data.length*2+5);
		int[] ndata=new int[nLen];
		System.arraycopy(data, 0, ndata, 0, count);
		data=ndata;
	}

}
