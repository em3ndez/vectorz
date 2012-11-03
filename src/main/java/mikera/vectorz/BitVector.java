package mikera.vectorz;

/**
 * Vector of bits - constrained to 0.0 / 1.0 values
 * @author Mike
 */
public final class BitVector extends AVector {
	private static final long serialVersionUID = 349277216077562294L;
	public static final double BIT_ON=1.0;
	public static final double BIT_OFF=0.0;
	public static final double BIT_THRESHOLD=0.5;

	private final int length;
	private long[] data;
	
	public BitVector(int length) {
		this.length=length;
		data=new long[requiredArraySize(length)];
		
	}
	
	private int requiredArraySize(int length) {
		assert(length>=0);
		return (length+63)/64;
	}
	
	

	public BitVector(AVector source) {
		this(source.length());
		set(source);
	}
	
	public BitVector(BitVector source) {
		this(source.data,source.length());
	}


	private BitVector(long[] data, int length) {
		this.length=length;
		this.data=data;
	}

	@Override
	public int length() {
		return length;
	}

	@Override
	public double get(int i) {
		return ((((data[i/64])>>>(i%64))&1)==0) ? BIT_OFF : BIT_ON;
	}
	
	@Override
	public boolean isMutable() {
		return true;
	}
	
	@Override
	public boolean isFullyMutable() {
		return false;
	}
	
	
	@Override
	public boolean isReference() {
		return false;
	}

	@Override
	public void set(int i, double value) {
		if ((i<0)||(i>=length)) throw new IndexOutOfBoundsException("Index: "+i);
		int b=i%64;
		long mask = (1<<b);
		int p=i/64;
		data[p]=(data[p]&(~mask))+(value>=BIT_THRESHOLD?mask:0);
	}
	
	@Override
	public AVector clone() {
		AVector v=Vectorz.newVector(length);
		v.set(this);
		return v;
	}

	public static BitVector of(double... values) {
		int len=values.length;
		BitVector b=new BitVector(len);
		b.setValues(values);
		return b;
	}
}
