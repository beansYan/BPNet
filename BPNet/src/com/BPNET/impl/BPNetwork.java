package com.BPNET.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.Scanner;

import com.BPNET.Net;

public class BPNetwork implements Net{
	
   private double input[],output[];    //�������.
   private double X[][];               //ÿ������,���� X[0][1],��ʾ��0��ĵ�1����Ԫ�����.���� X[k][i] = Xi(k) = f(Ui(k)) .Xi.
   private double weight[][][];        //ÿ����֮��ĸ�����Ԫ���ӵ�Ȩֵ ,���� weight[0][1][2],��ʾ��0���1����Ԫ����1���2����Ԫ֮���������������Ȩֵ .Wij.
   private double deltaWeight[][][];   //ÿ�ε���Ȩֵ�ı���.��Wij.
   private int layers;                 //�����������.
   private int nodeNumOfLayer[];       //ÿһ�����Ԫ��.
   private double Y[];                 //��ʦ�ź�.Yi.
   private double d[][];               //ÿһ��ÿ����Ԫ��dֵ,���ڷ��򴫲�.di(k).
   
   private static final double STEP = 0.2;    //ѧϰ����,��.    
   private static final double E = 0.005;     //��Χ,e.
   private static String FPREFIX = System.getProperty("user.dir")+File.separator+"data"+File.separator+"weight"+File.separator;
   private static final String DEFAULT_FILENAME = "weight.txt";
   
   /**
    * ��BP�������ļ���ȡ���ļ���BP���������,������Ԫ֮��Ȩ�ص������Ϣ
    * @param filename   �ļ���,�������׺,�ļ������ app/data/weight ��,������Ҫ�޸��ļ�ǰ׺.<br/>���Ϊnull��Ĭ��ΪĬ���ļ���.
    * @throws FileNotFoundException
    */
   public BPNetwork(String filename) throws FileNotFoundException{
	      if(filename==null)
	    	  filename = DEFAULT_FILENAME;
	      File fl = new File(FPREFIX+filename);
	      if(!fl.exists()){
	    	  System.out.println("weight file <"+filename+"> isn't exists.");
	          System.exit(0);
	      }
	      Scanner sc = new Scanner(fl);
	      layers = sc.nextInt();
	      nodeNumOfLayer = new int[layers];
	      for(int i=0;i<layers;i++)
	    	  nodeNumOfLayer[i] = sc.nextInt();   
		  for(int i=0;i<nodeNumOfLayer.length-1;i++){
			  int left = this.nodeNumOfLayer[i];
			  int right= this.nodeNumOfLayer[i+1];
			  //this.weight[i] = new double[left][right];
			  deltaWeight[i] = new double[left][right];
		  }
		  for(int k=0;k<layers-1;k++){
			  for(int i=0;i<nodeNumOfLayer[k];i++)
				for(int j=0;j<nodeNumOfLayer[k+1];j++)
				  weight[k][i][j] = sc.nextDouble();
		  }
	       this.input = new double[this.nodeNumOfLayer[0]];
	       this.output = new double[this.nodeNumOfLayer[this.nodeNumOfLayer.length-1]];
   }
   
   /**
    * �Զ�������������
    * @param layers  ���������
    * @param nodeNumOfLayer  ÿ�����Ԫ��
    */
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
   
   /**
    * ��ѵ��������ѧϰ
    * @param trainData ѵ������
    * @param tSignal   ��ʦ�ź�
 * @throws IOException 
    */
   public void learn(double trainData[][],int tSignal[]) throws IOException{
	   if(trainData.length!=tSignal.length){
		   System.out.println("ѵ�������ݸ������Ӧ��ǩ(��ʦ�ź�)����������");
		   return;
	   }
	   int sum = trainData.length;
	   for(int i=0;i<sum;i++){
		   learnOne(trainData[i], tSignal[i]);
		   System.out.println("Learn rate" + ((((double)(i+1))/(double)(sum))*100) + "%");
	   }
	   System.out.println("Learning finish!");           //ѵ������
       writeWeightToFile();                              //��ѵ�����д�뵽�ļ�����
   }
   
   /**
    * ��ʼ��Ȩ�ؾ���
    */
   private void initWeight(){
	   Random random = new Random(20000101);
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
   private void learnOne(double data[],int tSignal){
	  for(int i=0;i<data.length;i++)   //ע�������ź�input
		  input[i] = data[i];
	  for(int i=0;i<100;i++){          //��������ѧϰ�������Ϊ100��
        this.forward_propagating();    //���򴫲�
        if(i==0){                      //����ǵ�һ��ѧϰ,��ע���ʦ�ź�.
        	for(int j=0;j<output.length;j++){
        		if(j==tSignal)
        			Y[i] = 1;
        		else
        			Y[i] = 0;
        	}
        }        
        if(this.scopeJudge())          //���ʦ�źűȶ�
        	break;
        this.backard_propagating();    //������ֽ������,���з��򴫲�.
	  }
   }
   
   /**
    * ���н����Χ�ж�
    * @return
    */
   private boolean scopeJudge(){
	     boolean e = true;
	     for(int i=0;i<output.length;i++){
            if(output[i]==Y[i])
            	e = e&true;
            else{
            	if(output[i]<Y[i])
            		e=e&((output[i]+E)>Y[i]?true:false);
            	else if(output[i]>Y[i])
            		e=e&((output[i]-E)<Y[i]?true:false);
            }
	     }
	     return e;
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
	   //��Wij = -��*di(k)*Xj(k-1).( k<m AND di(k)=(Xi(k)*(1-Xi(k))*(l)��(Wli*dl(k+1)) ) )
	   //��ʼ����m��(mΪ�����)d��Ȩֵ
	   int m = X.length-1;
	   for(int i=0;i<d[m].length;i++)
		   d[m][i] = (X[m][i]-Y[i])*X[m][i]*(1-X[m][i]); //di(m)=(Xi(m)-Yi)*Xi(m)*(1-Xi(m))
	   for(int j=0;j<X[m-1].length;j++){
		   for(int i=0;i<X[m].length;i++){
			   weight[m-1][j][i] += (-1)*STEP*d[m][i]*X[m-1][j];   // ��Wij = -��*di(m)*Xj(m-1)  (k=m)
		   }
	   }
	   for(int k=layers-2;k>0;k--){
		   for(int i=0;i<X[k].length;i++){
			   double sum_wd = 0;
			   for(int l=0;l<X[k+1].length;l++)
				   sum_wd += (weight[k][i][l]*d[k+1][l]);    //(l)��(Wli*dl(k+1))
			   d[k][i] =  X[k][i]*(1-X[k][i])*sum_wd;        //di(k)=(Xi(k)*(1-Xi(k))*(l)��(Wli*dl(k+1)) )
		   }
		   for(int j=0;j<X[k-1].length;j++)
			   for(int i=0;i<X[k].length;i++)
				   weight[k-1][j][i] += (-1)*STEP*d[k][i]*X[k-1][j];	// ��Wij = -��*di(k)*Xj(k-1)   		   
	   }
   }
   
   /**
    * ��������д�뵽�ļ�����
    * @throws IOException
    */
   private void writeWeightToFile() throws IOException{
   	   File f = new File(FPREFIX+DEFAULT_FILENAME);    		
   	   if(!f.exists())
   			f.createNewFile();
   	   String writeContent = "";
   	   writeContent += (layers+"\r\n");
   	   for(int i=0;i<layers;i++)
   		   writeContent += (nodeNumOfLayer[i]+"\r\n");
	   for(int k=0;k<layers-1;k++){
		   for(int i=0;i<nodeNumOfLayer[k];i++)
		      for(int j=0;j<nodeNumOfLayer[k+1];j++)
				  writeContent += weight[k][i][j];
	   }
       OutputStream fotps = new FileOutputStream(f);
       fotps.write(writeContent.getBytes());
       System.out.println("Net has been write to file <"+f.getPath()+" "+f.getName()+">.");
       fotps.close();
   }
}
