package mikera.vectorz.impl;

import java.nio.DoubleBuffer;

import mikera.vectorz.AVector;

/**
 * A vector class implemented using a java.nio.DoubleBuffer
 * 
 * Intended for use with native libraries that require interop with buffer memory
 * 
 * @author Mike
 *
 */
public class BufferVector extends ASizedVector {

	final DoubleBuffer buffer;
	
	protected BufferVector(int length) {
		this(DoubleBuffer.allocate(length), length);
	}

	protected BufferVector(DoubleBuffer buf, int length) {
		super(length);
		this.buffer=buf;
	}
	
	public static BufferVector wrap(double[] source) {
		return new BufferVector(DoubleBuffer.wrap(source),source.length);
	}
	
	public static BufferVector wrap(DoubleBuffer source, int length) {
		return new BufferVector(source,length);
	}
	
	public static BufferVector create(AVector v) {
		return wrap(v.toDoubleArray());
	}
	
	public static BufferVector createLength(int length) {
		return new BufferVector(length);
	}

	@Override
	public double get(int i) {
		return buffer.get(i);
	}

	@Override
	public void set(int i,double value) {
		buffer.put(i,value);
	}
	
	@Override
	public double unsafeGet(int i) {
		return buffer.get(i);
	}

	@Override
	public void unsafeSet(int i, double value) {
		buffer.put(i,value);
	}

	@Override
	public boolean isFullyMutable() {
		return true;
	}

	@Override
	public BufferVector exactClone() {
		double[] newArray=new double[length];
		buffer.get(newArray);
		buffer.clear();
		return BufferVector.wrap(newArray);
	}

	@Override
	public double dotProduct(double[] data, int offset) {
		double result=0.0;
		for (int i=0; i<length; i++) {
			result+=data[offset+i]*buffer.get(i);
		}
		return result;
	}



}
