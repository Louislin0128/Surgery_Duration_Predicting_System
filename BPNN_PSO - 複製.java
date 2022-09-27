package predict;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.IterativeClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Utils;

public class BPNN extends AbstractClassifier implements IterativeClassifier {
	private static final long serialVersionUID = -416172372730928306L;
	private boolean resume = false;
	/**輸入層 | 輸出層個數 | 隱藏層個數 | 欄位總個數(inputNum+outputNum) | 迭代次數*/
	private int inputNum = 0, outputNum = 0, hiddenNum = 10, totalNum = 0;
	/**已執行迴圈次數 | 總迴圈次數*/
	private int numOfPerform = 0, epoch = 1000;
	/**學習率*/
	private double eta = 0.5, alpha = 0;
	/** 紀錄各節點輸出值 */
	private double[] X, T, H, Y;
	/** 紀錄有向邊上的權重值 */
	private double[][] W_xh, W_hy;
	/** 紀錄有向邊上的權重值變化量 */
	private double[][] dW_xh, dW_hy;
	/** 紀錄隱藏層與輸出層節點偏權值 */
	private double[] Q_h, Q_y;
	/** 紀錄隱藏層與輸出層節點偏權值變化量 */
	private double[] dQ_h, dQ_y;
	/** 紀錄隱藏層與輸出層節點輸出值變化量 */
	private double[] delta_h, delta_y;
	
	private double c1 = 1.5, c2 = 1.5, r1 = 0.0, r2 = 0.0,w = 1.8, w0 = w, w1 = 1.2;	//自己舊位址、鄰居位址、慣性權重
	private int numParticle = 20, maxgen1, cnt = 0;	//族群大小
	private double[][] xp ;		//粒子位置
	private double[][] v ;		//粒子速率
	private double[][] Pbest;	//過去發現最佳位置
	private double[][] Gbest;	//全域最佳位置
	private double[] xlo;		//最小長度
	private double[] xhi;		//最大長度
	private PSOparameter Pg;
	private ArrayList<PSOparameter> mPP = new ArrayList<>();		//粒子群位置
	private ArrayList<PSOparameter> mPV = new ArrayList<>();		//粒子群速率
	private HashMap<Integer, PSOparameter> Pb = new HashMap<>();    //一顆粒子歷代中出現最好的解
	private boolean check = false;
	private double mse = 0.0,lastmse = 0.0;
	
	private BigDecimal bd;
	
	/**設定學習率*/
	public void setLearningRate(double l) {
		if (l > 0 && l <= 1) {
			eta = l;
	    }else {
	    	throw new IllegalArgumentException("必須>0或<=1");
	    }
	}
	/**得到學習率*/
	public double getLearningRate() {
		return eta;
	}
	
	public void setAlpha(double m) {
		if (m >= 0 && m <= 1) {
			alpha = m;
		}else {
			throw new IllegalArgumentException("必須>=0或<=1");
		}
	}
	public double getAlpha() {
		return alpha;
	}
	
	/**設定隱藏層*/
	public void setHiddenLayers(int h) {
		if(h > 0) {
			hiddenNum = h;
		}else {
			throw new IllegalArgumentException("必須>0");
		}
	}
	/**得到隱藏層*/
	public int getHiddenLayers() {
		return hiddenNum;
	}	
	/**設定訓練次數*/
	public void setTrainingTime(int n) {
		if(n > 0) {
			epoch = n;
		}else {
			throw new IllegalArgumentException("必須>0");
		}
	}
	/**得到訓練次數*/
	public int getTrainingTime() {
		return epoch;
	}
	
	/**宣告陣列 將陣列填入初始值*/
	private void initialize(int inputNum, int hiddenNum, int outputNum) {
		X = new double[inputNum];
		H = new double[hiddenNum];
		Y = new double[outputNum];
		T = new double[outputNum];
		W_xh = new double[inputNum][hiddenNum];
		W_hy = new double[hiddenNum][outputNum];
		dW_xh = new double[inputNum][hiddenNum];
		dW_hy = new double[hiddenNum][outputNum];
		Q_h = new double[hiddenNum];
		Q_y = new double[outputNum];
		dQ_h = new double[hiddenNum];
		dQ_y = new double[outputNum];
		delta_h = new double[hiddenNum];
		delta_y = new double[outputNum];
		
		xlo = new double[numParticle];
		xhi = new double[numParticle];
		xp = new double[totalNum + 2][hiddenNum];
		v = new double[totalNum + 2][hiddenNum];
		Pbest = new double[totalNum + 2][hiddenNum];
		Gbest = new double[totalNum + 2][hiddenNum];
		maxgen1 = epoch * 3 / 5;
		
		for (int h = 0; h < hiddenNum; h++)
			for (int i = 0; i < inputNum; i++) {
				dW_xh[i][h] = 0.0;
			}
		for (int j = 0; j < outputNum; j++)
			for (int h = 0; h < hiddenNum; h++) {
				dW_hy[h][j] = 0.0;
			}
		for (int h = 0; h < hiddenNum; h++) {
			dQ_h[h] = 0.0;
			delta_h[h] = 0.0;
		}
		for (int j = 0; j < outputNum; j++) {
			dQ_y[j] = 0.0;
			delta_y[j] = 0.0;
		}
	}
	
	private void initPSO() throws Exception{
		for(int num = 0; num < numParticle; num++) {
			W_xh = new double[inputNum][hiddenNum];
			W_hy = new double[hiddenNum][outputNum];
			Q_h = new double[hiddenNum];
			Q_y = new double[outputNum];
			
			for (int h = 0; h < hiddenNum; h++) {
				Q_h[h] = Math.random();
				for (int i = 0; i < inputNum; i++) 
					W_xh[i][h] = Math.random();
			}

			for (int j = 0; j < outputNum; j++) {
				Q_y[j] = Math.random();
				for (int h = 0; h < hiddenNum; h++) 
					W_hy[h][j] = Math.random();
			}
			next();
			PSOparameter PP = new PSOparameter(W_xh, W_hy,Q_h,Q_y, mse);
			mPP.add(PP);
			
			//最大/最小速率設定
			xlo[num] = -5;
			xhi[num] = 5;
		}
	}

	private void Initialize_V() {		
		for(int num = 0; num < numParticle; num++) {
			W_xh = new double[inputNum][hiddenNum];
			W_hy = new double[hiddenNum][outputNum];
			Q_h = new double[hiddenNum];
			Q_y = new double[outputNum];
			
			for (int h = 0; h < hiddenNum; h++) {
				Q_h[h] = Math.random();
				for (int i = 0; i < inputNum; i++) 
					W_xh[i][h] = Math.random();
			}
			for (int j = 0; j < outputNum; j++) {
				Q_y[j] = Math.random();
				for (int h = 0; h < hiddenNum; h++) 
					W_hy[h][j] = Math.random();
			}
			
			PSOparameter PP = new PSOparameter(W_xh, W_hy,Q_h,Q_y);
			mPV.add(PP);
		}
	}
	
	Instances instances;
	@Override
	public void initializeClassifier(Instances data) throws Exception {
		numOfPerform = 0;
		
		getCapabilities().testWithFail(data); // 分類器是否可以處理資料
		Instances insts = new Instances(data);
		insts.deleteWithMissingClass();	//刪除具有缺失值類別的實例
		
		instances = new Instances(insts);
		totalNum = instances.numAttributes();
		outputNum = instances.numClasses();
		inputNum = totalNum - outputNum;
		
		initialize(inputNum, hiddenNum, outputNum);
		initPSO();
		Initialize_V();
		for (int i = 0; i < numParticle; i++) {		//挑選最好的個體，找最小的
			Pb.put(i, mPP.get(i));
		}
	    Pg = Pb.get(0);
	    for (int i = 1; i < numParticle; i++) {
	    	if (Pg.getFitness() > Pb.get(i).getFitness()) {
	    		Pg = Pb.get(i);
	        }
	    }
	    lastmse = Pg.getFitness();
	}
	
	@Override
	public boolean next()  {		
		double sum = 0.0;
		mse = 0.0;
		for(Instance instance: instances) {	// 遍歷所有資料集內容
			for (int i = 0; i < inputNum; i++) {
				X[i] = instance.value(i);
			}
			for (int i = inputNum; i < totalNum; i++) {
				T[i - inputNum] = instance.value(i);
			}
			for (int h = 0; h < hiddenNum; h++) {
				sum = 0.0;
				for (int i = 0; i < inputNum; i++)
					sum += X[i] * W_xh[i][h];
				H[h] = (float) 1.0 / (1.0 + Math.exp(-(sum - Q_h[h])));
			}
			for (int j = 0; j < outputNum; j++) {
				sum = 0.0;
				for (int h = 0; h < hiddenNum; h++)
					sum += H[h] * W_hy[h][j];
				Y[j] = (float) 1.0 / (1.0 + Math.exp(-(sum - Q_y[j])));
			}
			for (int j = 0; j < outputNum; j++) {				//計算mse(累加)
				mse += (T[j] - Y[j]) * (T[j] - Y[j]);
			}
		}
		mse = mse / instances.size(); // mse
		return true;
	}
	
	private void Backpropagation() {
		double sum = 0.0;
		mse = 0.0;
		for(Instance instance: instances) {	// 遍歷所有資料集內容
			for (int i = 0; i < inputNum; i++) {
				X[i] = instance.value(i);
			}
			for (int i = inputNum; i < totalNum; i++) {
				T[i - inputNum] = instance.value(i);
			}
			for (int h = 0; h < hiddenNum; h++) {
				sum = 0.0;
				for (int i = 0; i < inputNum; i++)
					sum += X[i] * W_xh[i][h];
				H[h] = (float) 1.0 / (1.0 + Math.exp(-(sum - Q_h[h])));
			}
			for (int j = 0; j < outputNum; j++) {
				sum = 0.0;
				for (int h = 0; h < hiddenNum; h++)
					sum += H[h] * W_hy[h][j];
				Y[j] = (float) 1.0 / (1.0 + Math.exp(-(sum - Q_y[j])));
			}
			
			for (int j = 0; j < outputNum; j++)
				delta_y[j] = Y[j] * (1.0 - Y[j]) * (T[j] - Y[j]);
			for (int h = 0; h < hiddenNum; h++) {
				sum = 0.0;
				for (int j = 0; j < outputNum; j++)
					sum += W_hy[h][j] * delta_y[j];
				delta_h[h] = H[h] * (1.0 - H[h]) * sum;
			}
			for (int j = 0; j < outputNum; j++)
				for (int h = 0; h < hiddenNum; h++)
					dW_hy[h][j] = eta * delta_y[j] * H[h] + alpha * dW_hy[h][j];
	
			for (int j = 0; j < outputNum; j++)
				dQ_y[j] = -eta * delta_y[j] + alpha * dQ_y[j];
	
			for (int h = 0; h < hiddenNum; h++)
				for (int i = 0; i < inputNum; i++)
					dW_xh[i][h] = eta * delta_h[h] * X[i] + alpha * dW_xh[i][h];
	
			for (int h = 0; h < hiddenNum; h++)
				dQ_h[h] = -eta * delta_h[h] + alpha * dQ_h[h];
	
			for (int j = 0; j < outputNum; j++)
				for (int h = 0; h < hiddenNum; h++)
					W_hy[h][j] = W_hy[h][j] + dW_hy[h][j];
	
			for (int j = 0; j < outputNum; j++)
				Q_y[j] = Q_y[j] + dQ_y[j];
	
			for (int h = 0; h < hiddenNum; h++)
				for (int i = 0; i < inputNum; i++)
					W_xh[i][h] = W_xh[i][h] + dW_xh[i][h];
	
			for (int h = 0; h < hiddenNum; h++)
				Q_h[h] = Q_h[h] + dQ_h[h];
			
			for (int j = 0; j < outputNum; j++) {				//計算mse(累加)
				mse += (T[j] - Y[j]) * (T[j] - Y[j]);
			}
		}
		mse = mse / instances.size(); // mse
	}
	
	PSOparameter PP;
	private void evulation() {
		int gen = numOfPerform + 1;
		
		for(int i=0; i<numParticle; i++) {			//更新每顆粒子的速率與位置
		   	r1 = Math.random(); r2 = Math.random();
		   	
		   	v = mPV.get(i).getParticleVelocity();
		   	xp = mPP.get(i).getParticlePosition();
		   	Pbest = Pb.get(i).getParticlePosition();
		   	Gbest = Pg.getParticlePosition();
		   	
		   	for(int k=0; k<totalNum + 2; k++) {
		   		for(int j=0; j<hiddenNum; j++) {
		   			v[k][j] =  w * v[k][j] + c1 * r1 * (Pbest[k][j] - xp[k][j]) 
			  				+ c2 * r2 * (Gbest[k][j] - xp[k][j]);
		   			
			        if(v[k][j] > xhi[i]) v[k][j] %= xhi[i];		//越界處理
			        if(v[k][j] < xlo[i]) v[k][j] %= xlo[i];
			       
			        xp[k][j] = xp[k][j] + v[k][j];			        
			        if(xp[k][j] > xhi[i]) xp[k][j] %= xhi[i];	//越界處理
			        if(xp[k][j] < xlo[i]) xp[k][j] %= xlo[i];
			        		        
			        if(k == totalNum + 1) break; 	//到Q_y就要跳出了，因為只有一個值
		   		}
		   	}

			W_xh = new double[inputNum][hiddenNum];
			W_hy = new double[hiddenNum][outputNum];
			Q_h = new double[hiddenNum];
			Q_y = new double[outputNum];
			
			for (int h = 0; h < hiddenNum; h++) {	//位置存回之後，重新評估適存值
				for (int k = 0; k < inputNum; k++) {
					W_xh[k][h] = xp[k][h];
				}
			}
			for (int j = 0; j < outputNum; j++) {
				for (int h = 0; h < hiddenNum; h++) {
					W_hy[h][j] = xp[j + inputNum][h];
				}
			}
			for(int j = 0; j < hiddenNum; j++) {
				Q_h[j] = xp[totalNum][j];
			}
			for(int j = 0; j < outputNum; j++) {
				Q_y[j] = xp[totalNum + 1][j];
			}
			next();
			mPP.remove(i);	//刪除指定index的元素
	        PP = new PSOparameter(W_xh, W_hy,Q_h,Q_y, mse);	//更新粒子位址
	        mPP.add(i,PP);
	        
	        W_xh = new double[inputNum][hiddenNum];
			W_hy = new double[hiddenNum][outputNum];
			Q_h = new double[hiddenNum];
			Q_y = new double[outputNum];
			
			for (int h = 0; h < hiddenNum; h++) {	//速率存回之後
				for (int k = 0; k < inputNum; k++) {
					W_xh[k][h] = v[k][h];
				}
			}
			for (int j = 0; j < outputNum; j++) {
				for (int h = 0; h < hiddenNum; h++) {
					W_hy[h][j] = v[j + inputNum][h];
				}
			}
			for(int j = 0; j < hiddenNum; j++) {
				Q_h[j] = v[totalNum][j];
			}
			for(int j = 0; j < outputNum; j++) {
				Q_y[j] = v[totalNum + 1][j];
			}
			mPV.remove(i);	//刪除指定index的元素
	        PP = new PSOparameter(W_xh, W_hy,Q_h,Q_y);	//更新粒子速率
	        mPV.add(i,PP);
	    }
		for(int num = 0; num < numParticle; num++) {	//並挑選最好的個體
			if (Pb.get(num).getFitness() > mPP.get(num).getFitness()) {
                Pb.put(num, mPP.get(num));
            }
            if (Pg.getFitness() > Pb.get(num).getFitness()) {
                Pg = Pb.get(num);
            }
		}
		// Optimization
		if(1 <= gen && gen <= maxgen1) {	//自適應慣行權重	
			w = w0 - (w1 / maxgen1) * gen;
		}else {
			w =  (w0 - w1) * Math.exp((maxgen1 - gen)/100.0);
		}
	}
	
	@Override
	public void done() throws Exception {
		
	}

	@Override
	public void buildClassifier(Instances data) throws Exception {
	    instances = null;

	    // Initialize classifier
	    initializeClassifier(data);
	    
	    // For the given number of iterations
	    int tnt = 0;
	    while (numOfPerform != epoch ) {
	    	if(check) {
	    		for(int i=0; i<numParticle; i++) {
	    			Backpropagation();
	    			mPP.remove(i);	//刪除指定index的元素
	    	        PP = new PSOparameter(W_xh, W_hy,Q_h,Q_y,mse);	//更新粒子位址
	    	        mPP.add(i,PP);
	    		}
	    		for(int num = 0; num < numParticle; num++) {	//並挑選最好的個體
	    			if (Pb.get(num).getFitness() > mPP.get(num).getFitness()) {
	                    Pb.put(num, mPP.get(num));
	                }
	                if (Pg.getFitness() > Pb.get(num).getFitness()) {
	                    Pg = Pb.get(num);
	                }
	    		}
	    		tnt++;
	    		if(tnt == 50) {
	    			check = false;	//執行過一次，就跳出
	    			tnt = 0;
	    		}
			}else {
				evulation();	 
			}
	    	
	    	bd = new BigDecimal(Pg.getFitness()).setScale(8, RoundingMode.HALF_UP);
	    	if(lastmse == bd.doubleValue() && !check) {
		    	cnt++;
		    	if(cnt >= 500) {
		    		System.out.println("y" + "，在第 " + numOfPerform + "次迭代轉換演算法!!" + "\n");
		    		check = true;
		    		cnt = 0;
		    	}
		    }else {
		    	lastmse = bd.doubleValue();
		    	cnt = 0;
		    }
	    	mse = Pg.getFitness();
	    	
	    	if ((numOfPerform % 100) == 0) { // 每一輪(迭代100次)輸出一次rmse
	    		System.out.println(Math.sqrt(mse));
//				System.out.printf("Icycle=%4d rmse=%-8.6f\n", numOfPerform, Math.sqrt(mse));
			}
	    	numOfPerform++;
	    }
	    //將最佳結果回存，接著進行test function，才會正常
	  	Gbest = Pg.getParticlePosition();
	  	for (int h = 0; h < hiddenNum; h++) {			//位置存回之後，重新評估適存值
			for (int k = 0; k < inputNum; k++) {
				W_xh[k][h] = Gbest[k][h];
				System.out.printf(W_xh[k][h] + " ");	//輸出最佳粒子的內容
			}
			System.out.println();
		}
		for (int j = 0; j < outputNum; j++) {
			for (int h = 0; h < hiddenNum; h++) {
				W_hy[h][j] = Gbest[j + inputNum][h];
				System.out.printf(W_hy[h][j] + " ");
			}
			System.out.println();
		}
		for(int j = 0; j < hiddenNum; j++) {
			Q_h[j] = Gbest[totalNum][j];
			System.out.printf(Q_h[j] + " ");
		}
		System.out.println();
		for(int j = 0; j < outputNum; j++) {
			Q_y[j] = Gbest[totalNum + 1][j];
			System.out.printf(Q_y[j] + " ");
		}
	    // Clean up
	    done();
	}
	
	@Override
	public double classifyInstance(Instance instance) throws Exception {
		//執行test函式，預測一筆資料
		for (int i = 0; i < inputNum; i++) {
			X[i] = instance.value(i);
		}
		
		double sum = 0.0;
		for (int h = 0; h < hiddenNum; h++) {
			sum = 0.0;
			for (int i = 0; i < inputNum; i++)
				sum += X[i] * W_xh[i][h];
			H[h] = (float) 1.0 / (1.0 + Math.exp(-(sum - Q_h[h])));
		}
		
		for (int j = 0; j < outputNum; j++) {
			sum = 0.0;
			for (int h = 0; h < hiddenNum; h++)
				sum += H[h] * W_hy[h][j];
			Y[j] = (float) 1.0 / (1.0 + Math.exp(-(sum - Q_y[j])));
		}
		return Y[0];
	}
	
	@Override
	public double[] distributionForInstance(Instance instance) throws Exception {
		return new double[] {classifyInstance(instance)};
	}
	
	@Override
	public Enumeration<Option> listOptions() {
		Vector<Option> newVector = new Vector<Option>(10);
		newVector.addElement(new Option(
			      "\tLearning rate for the backpropagation algorithm.\n"
			        + "\t(Value should be between 0 - 1, Default = 0.3).", "L", 1,
			      "-L <learning rate>"));
		newVector.addElement(new Option(
			      "\tMomentum rate for the backpropagation algorithm.\n"
			        + "\t(Value should be between 0 - 1, Default = 0.2).", "M", 1,
			      "-M <momentum>"));
		newVector.addElement(new Option("\tNumber of epochs to train through.\n"
			      + "\t(Default = 500).", "N", 1, "-N <number of epochs>"));
		newVector.addAll(Collections.list(super.listOptions()));
		return newVector.elements();
	}
	
	@Override
	public void setOptions(String[] options) throws Exception {
		String learningString = Utils.getOption('L', options);
	    if (learningString.length() != 0) {
	    	setLearningRate(Double.parseDouble(learningString));
	    } else {
	    	setLearningRate(0.7);
	    }
	    String momentumString = Utils.getOption('M', options);
	    if (momentumString.length() != 0) {
	    	setAlpha(Double.parseDouble(momentumString));
	    } else {
	    	setAlpha(0.2);
	    }
	    String hiddenLayers = Utils.getOption('H', options);
	    if (hiddenLayers.length() != 0) {
	    	setHiddenLayers(Integer.parseInt(hiddenLayers));
	    } else {
	    	setHiddenLayers(30);
	    }
	    String epochsString = Utils.getOption('N', options);
	    if (epochsString.length() != 0) {
	    	setTrainingTime(Integer.parseInt(epochsString));
	    } else {
	    	setTrainingTime(1000);
	    }
		super.setOptions(options);
	}
	
	@Override
	public String[] getOptions() {
		Vector<String> options = new Vector<String>();
		options.add("-L");
		options.add("" + getLearningRate());
		options.add("-M");
		options.add("" + getAlpha());
		options.add("-N");
		options.add("" + getTrainingTime()); 
		options.add("-H");
	    options.add("" + getHiddenLayers());
	    Collections.addAll(options, super.getOptions());
	    return options.toArray(new String[options.size()]);
	}

	@Override
	public void setResume(boolean resume) throws Exception {
		this.resume = resume;
	}

	@Override
	public boolean getResume() {
		return resume;
	}
	
	/**
	 * This will return a string describing the classifier.
	 * @return The string.
	 */
	public String globalInfo() {
		return "自研發的倒傳遞神經網路";
	}

	/**
	 * @return a string to describe the learning rate option.
	 */
	public String learningRateTipText() {
		return "學習率";
	}

	/**
	 * @return a string to describe the momentum option.
	 */
	public String alphaTipText() {
		return "Momentum applied to the weight updates.";
	}
}
