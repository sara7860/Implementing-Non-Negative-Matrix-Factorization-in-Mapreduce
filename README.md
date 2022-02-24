# Implementing-Non-Negative-Matrix-Factorization-in-Mapreduce

We have demonstrated in this project the three alternative implementations of MapReduce matrix multiplication according to the matrix characteristics. Using these methods, the number of MapReduce jobs required to complete NMF of any matrix was successfully minimized. We have also implemented MapReduce in a few utility classes for this program such as finding the Transpose of a Matrix and Updating the Matrix.

Matrices of dimension million-by-thousand with millions of nonzero elements can be factorized within several hours on a MapReduce cluster. However, due to the limited processing power our PCs as well as other restrictions of VirtualBox VM, we have limited our experiments to a 1000x10 matrix. 
