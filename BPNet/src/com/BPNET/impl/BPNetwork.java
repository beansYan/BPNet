package com.BPNET.impl;

import java.util.Random;

import com.BPNET.Net;

public class BPNetwork implements Net{
   private double input[],output[];    //�������.
   private double X[][];               //ÿ������,���� X[0][1],��ʾ��0��ĵ�1����Ԫ�����.���� X[k][i] = Xi(k) = f(Ui(k)) .Xi.
   private double weight[][][];        //ÿ����֮��ĸ�����Ԫ���ӵ�Ȩֵ ,���� weight[0][1][2],��ʾ��0���1����Ԫ����1���2����Ԫ֮���������������Ȩֵ .Wij
   private double deltaWeight[][][];   //ÿ�ε���Ȩֵ�ı���.��Wij.
   private int layers;                 //�����������.
   private int nodeNumOfLayer[];       //ÿһ�����Ԫ��.
   private double Y[];                 //��ʦ�ź�.Yi.
   private double d[][];               //ÿһ��ÿ����Ԫ��dֵ,���ڷ��򴫲�.di(k).
   private static final double STEP = 0.2;    //ѧϰ����,��.    

public BPNetwork(int layers,int nodeNumOfLayer[]){
	   if(layers<2){
		   System.out.println("At least 2 layers.");
		   return;
	   }
	   this.layers = layers;
	   this.nodeNumOfLayer = nodeNumOfLayer;

	   this.weight = new double[this.layers-1][1][1];
	   this.deltaWeight = new double[this.layers-1][1][1];
	   this.d = new double[this.layers][1];
	   for(int i=0;i<nodeNumOfLayer.length-1;i++){
		   int left = this.nodeNumOfLayer[i];
		   int right= this.nodeNumOfLayer[i+1];
		   this.weight[i] = new double[left][right];
		   this.deltaWeight[i] = new double[left][right];
	   }
	   for(int i=0;i<nodeNumOfLayer.length;i++){
	       this.d[i] = new double[nodeNumOfLayer[i]];
	   }
	   this.initWeight();                         //init weight matrix
	   this.X = new double[this.layers][1];
       for(int i=0;i<this.nodeNumOfLayer.length;i++)
    	   this.X[i] = new double[this.nodeNumOfLayer[i]];  
       this.input = new double[this.nodeNumOfLayer[0]];
       this.output = new double[this.nodeNumOfLayer[this.nodeNumOfLayer.length-1]];
   }
   
   public void learn(double trainData[][],double tSignal[][]){
	   
   }
   /**
    * ��ʼ��Ȩ�ؾ���
    */
   private void initWeight(){
	   Random random = new Random(19881211);
	   for(int i=0;i<this.weight.length;i++)
		   for(int j=0;j<this.weight[i].length;j++)
			   for(int k=0;k<this.weight[i][j].length;k++){
				   double o = random.nextDouble();
				   this.weight[i][j][k] = o>0.5?o:-o;
			   }
   }
   /**
    * ���е�������ѧϰ.�÷���ֻ�ܱ�learn��������.
    * @param data
    */
   private void learnOne(double data[]){
      this.forward_propagating();    //���򴫲�
                                     //���ʦ�źűȶ�
      this.backard_propagating();    //������ֽ������,���з��򴫲�.
   }
   /**
    * ���򴫲�����
    * ��ȡoutput
    */
   private void forward_propagating(){
	   for(int i=0;i<input.length;i++)
		   X[0][i] = Tools.sigmod(input[i]);            //�����0����Ԫ�����.X[0][i] = Xi(0) = f(Ui(0)).f(Ui(0)) = sigmod(Ui(0)).
	   for(int k=1;k<X.length;k++){
		   for(int j=0;j<X[k].length;j++){
			   double U = 0;
			   for(int i=0;i<X[k-1].length;i++)
				   U += (X[k-1][i]*weight[k-1][i][j]);  //Ui(k)=(j)��(Wij*Xj(k-1)).
			   X[k][j] = Tools.sigmod(U);               //X[k][i] = Xi(k) = f(Ui(k)).
		   }
	   }
	   for(int i=0;i<output.length;i++)
		   output[i] = X[X.length-1][i];
   }
   /**
    * ���򴫲�
    * ���޸�ÿ��Ȩֵ
    */
   private void backard_propagating(){
	   //��⹫ʽ
	   //��Wij = -��*di(m)*Xj(m-1).( k=m AND di(m)=(Xi(m)-Yi)*Xi(m)*(1-Xi(m)) )
	   //��Wij = -��*di(k)*Xj(k-1).( k<m AND di(k)=)
	   //��ʼ����m��(mΪ�����)d��Ȩֵ
	   int m = X.length-1;
	   for(int i=0;i<d[m].length;i++)
		   d[m][i] = (X[m][i]-Y[i])*X[m][i]*(1-X[m][i]); //di(m)=(Xi(m)-Yi)*Xi(m)*(1-Xi(m))
	   for(int j=0;j<X[X.length-2].length;j++){
		   for(int i=0;i<X[X.length-1].length;i++){
			   weight[layers-2][j][i] += (-1)*STEP*d[d.length-1][i]*X[X.length-2][j];        // ��Wij = -��*di(k)*Xj(k-1)
		   }
	   }
	   for(int k=this.layers-2;k>0;k--){
		   for(int i=0;i<X[k].length;i++){
			   double sum_wd = 0;
			   for(int l=0;l<X[k+1].length;l++)
				   sum_wd += (weight[k][i][l]*d[k+1][l]);
			   d[k][i] =  X[k][i]*(1-X[k][i])*sum_wd;
		   }
		   for(int j=0;j<X[k-1].length;j++)
			   for(int i=0;i<X[k].length;i++)
				   weight[k-1][j][i] += (-1)*STEP*d[k][i]*X[k-1][j];			   		   
	   }
   }
public double[] getInput() {
	return input;
}

public void setInput(double[] input) {
	this.input = input;
}

public double[] getOutput() {
	return output;
}

public void setOutput(double[] output) {
	this.output = output;
}

public double[][] getX() {
	return X;
}

public void setX(double[][] x) {
	X = x;
}

public double[][][] getWeight() {
	return weight;
}

public void setWeight(double[][][] weight) {
	this.weight = weight;
}

public int getLayers() {
	return layers;
}

public void setLayers(int layers) {
	this.layers = layers;
}

public int[] getNodeNumOfLayer() {
	return nodeNumOfLayer;
}

public void setNodeNumOfLayer(int[] nodeNumOfLayer) {
	this.nodeNumOfLayer = nodeNumOfLayer;
}

public double[][][] getDeltaWeight() {
	return deltaWeight;
}

public void setDeltaWeight(double[][][] deltaWeight) {
	this.deltaWeight = deltaWeight;
}

public double[] getY() {
	return Y;
}

public void setY(double y[]) {
	Y = y;
}
}
