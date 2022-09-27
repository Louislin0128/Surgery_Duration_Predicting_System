package predict;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.IterativeClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Utils;

public class BPNN extends AbstractClassifier implements IterativeClassifier {
	private static final long serialVersionUID = -416172372730928306L;
	private SecureRandom random = new SecureRandom();
	private boolean resume = false;
	/**輸入層 | 輸出層個數 | 隱藏層個數 | 欄位總個數(Ninp+Nout) | 迭代次數*/
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
		
		for (int h = 0; h < hiddenNum; h++)
			for (int i = 0; i < inputNum; i++) {
				W_xh[i][h] = random.nextDouble();
				dW_xh[i][h] = 0.0;
			}
		for (int j = 0; j < outputNum; j++)
			for (int h = 0; h < hiddenNum; h++) {
				W_hy[h][j] = random.nextDouble();
				dW_hy[h][j] = 0.0;
			}
		for (int h = 0; h < hiddenNum; h++) {
			Q_h[h] = random.nextDouble();
			dQ_h[h] = 0.0;
			delta_h[h] = 0.0;
		}
		for (int j = 0; j < outputNum; j++) {
			Q_y[j] = random.nextDouble();
			dQ_y[j] = 0.0;
			delta_y[j] = 0.0;
		}
	}
	
	Instances instances;
	@Override
	public void initializeClassifier(Instances data) throws Exception {
		numOfPerform = 0;
		
		getCapabilities().testWithFail(data); // 分類器是否可以處理資料
		Instances insts = new Instances(data);
		insts.deleteWithMissingClass();	// 刪除具有缺失值類別的實例
		
		instances = new Instances(insts);
		totalNum = instances.numAttributes();
		outputNum = instances.numClasses();
		inputNum = totalNum - outputNum;
		
		initialize(inputNum, hiddenNum, outputNum);
	}
	
	@Override
	public boolean next() throws Exception {
		if(numOfPerform == epoch) {	// 執行迴圈次數已到
			return false;
		}
		
		double sum = 0.0;
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
		}
		
		numOfPerform++;
		return true;
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
	    while (next()) {}

	    // Clean up
	    done();
	}
	
	@Override
	public double classifyInstance(Instance instance) throws Exception {
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
	    	setLearningRate(0.5);
	    }
	    String momentumString = Utils.getOption('M', options);
	    if (momentumString.length() != 0) {
	    	setAlpha(Double.parseDouble(momentumString));
	    } else {
	    	setAlpha(0);
	    }
	    String hiddenLayers = Utils.getOption('H', options);
	    if (hiddenLayers.length() != 0) {
	    	setHiddenLayers(Integer.parseInt(hiddenLayers));
	    } else {
	    	setHiddenLayers(10);
	    }
	    String epochsString = Utils.getOption('N', options);
	    if (epochsString.length() != 0) {
	    	setTrainingTime(Integer.parseInt(epochsString));
	    } else {
	    	setTrainingTime(500);
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
