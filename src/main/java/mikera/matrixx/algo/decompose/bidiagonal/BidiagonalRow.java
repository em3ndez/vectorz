/*
 * Copyright (c) 2009-2013, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mikera.matrixx.algo.decompose.bidiagonal;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.algo.decompose.qr.impl.QRHelperFunctions;

/**
 * <p>
 * Performs a {@link org.ejml.alg.dense.decomposition.bidiagonal.BidiagonalDecomposition} using
 * householder reflectors.  This is efficient on wide or square matrices.
 * </p>
 *
 * @author Peter Abeles
 */
public class BidiagonalRow {
    // A combined matrix that stores te upper Hessenberg matrix and the orthogonal matrix.
    private Matrix UBV;

    // number of rows
    private int m;
    // number of columns
    private int n;
    // the smaller of m or n
    private int min;

    // the first element in the orthogonal vectors
    private double gammasU[];
    private double gammasV[];
    // temporary storage
    private double b[];
    private double u[];

    /**
     * Computes the decomposition of the provided matrix.  If no errors are detected then true is returned,
     * false otherwise.
     *
     * @param A  The matrix that is being decomposed.  Not modified.
     * @return If it detects any errors or not.
     */
    public boolean decompose( AMatrix A  )
    {
    	UBV = Matrix.create(A);
    	
    	m = UBV.rowCount();
    	n = UBV.columnCount();
    	
    	min = Math.min(m,  n);
    	int max = Math.max(m,  n);
    	
    	b = new double[max+1];
    	u = new double[max+1];
    	
    	gammasU = new double[m];
    	gammasV = new double[n];
    	
    	for( int k = 0; k < min; k++ ) {
//          UBV.print();
          computeU(k);
//          System.out.println("--- after U");
//          UBV.print();
          computeV(k);
//          System.out.println("--- after V");
//          UBV.print();
	    }
	
	    return true;
    }

    /**
     * The raw UBV matrix that is stored internally.
     *
     * @return UBV matrix.
     */
    public AMatrix getUBV() {
//    	TODO: Should this be here or in the Result?
        return UBV;
    }

    public void getDiagonal(double[] diag, double[] off) {
//    	TODO: change interface (or remove?)
        diag[0] = UBV.get(0);
        for( int i = 1; i < n; i++ ) {
            diag[i] = UBV.unsafeGet(i,i);
            off[i-1] = UBV.unsafeGet(i-1,i);
        }
    }

    protected void computeU( int k) {
        double b[] = UBV.data;

        // find the largest value in this column
        // this is used to normalize the column and mitigate overflow/underflow
        double max = 0;

        for( int i = k; i < m; i++ ) {
            // copy the householder vector to vector outside of the matrix to reduce caching issues
            // big improvement on larger matrices and a relatively small performance hit on small matrices.
            double val = u[i] = b[i*n+k];
            val = Math.abs(val);
            if( val > max )
                max = val;
        }

        if( max > 0 ) {
            // -------- set up the reflector Q_k
            double tau = QRHelperFunctions.computeTauAndDivide(k,m,u ,max);

            // write the reflector into the lower left column of the matrix
            // while dividing u by nu
            double nu = u[k] + tau;
            QRHelperFunctions.divideElements_Bcol(k+1,m,n,u,b,k,nu);
            u[k] = 1.0;

            double gamma = nu/tau;
            gammasU[k] = gamma;

            // ---------- multiply on the left by Q_k
            QRHelperFunctions.rank1UpdateMultR(UBV,u,gamma,k+1,k,m,this.b);

            b[k*n+k] = -tau*max;
        } else {
            gammasU[k] = 0;
        }
    }

    protected void computeV(int k) {
        double b[] = UBV.data;

        int row = k*n;

        // find the largest value in this column
        // this is used to normalize the column and mitigate overflow/underflow
        double max = QRHelperFunctions.findMax(b,row+k+1,n-k-1);

        if( max > 0 ) {
            // -------- set up the reflector Q_k

            double tau = QRHelperFunctions.computeTauAndDivide(k+1,n,b,row,max);

            // write the reflector into the lower left column of the matrix
            double nu = b[row+k+1] + tau;
            QRHelperFunctions.divideElements_Brow(k+2,n,u,b,row,nu);

            u[k+1] = 1.0;

            double gamma = nu/tau;
            gammasV[k] = gamma;

            // writing to u could be avoided by working directly with b.
            // requires writing a custom rank1Update function
            // ---------- multiply on the left by Q_k
            QRHelperFunctions.rank1UpdateMultL(UBV,u,gamma,k+1,k+1,n);

            b[row+k+1] = -tau*max;
        } else {
            gammasV[k] = 0;
        }
    }

    /**
     * Returns gammas from the householder operations for the U matrix.
     *
     * @return gammas for householder operations
     */
    public double[] getGammasU() {
        return gammasU;
    }

    /**
     * Returns gammas from the householder operations for the V matrix.
     *
     * @return gammas for householder operations
     */
    public double[] getGammasV() {
        return gammasV;
    }
}