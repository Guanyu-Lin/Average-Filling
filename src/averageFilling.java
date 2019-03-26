import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DecimalFormat;
import java.text.Format;

public class averageFilling {
	private static final int USER_NUM = 943;
	private static final int ITEM_NUM = 1682;
	private static final int TRAINNING_RECORD_NUM = 80000;
	private static final int TEST_RECORD_NUM = 20000;
	private static final int MIN_RATING = 1;
	private static final int MAX_RATING = 5;
	private static final String BASE_PATH = "C:\\Users\\LGY\\Desktop\\DATA\\ml-100k\\u1.base";
	private static final String TEST_PATH = "C:\\Users\\LGY\\Desktop\\DATA\\ml-100k\\u1.test";
	private static final String RESULT_PATH = "C:\\Users\\LGY\\Desktop\\DATA\\ml-100k\\averageFilling.result";
	private double ave_r_u[];
	private double ave_r_i[];
	private double r_u_i[][];
	private double b_u[];
	private double b_i[];
	private double ave_r;
	
	Record trainning[];
	Record test[];
	averageFilling() {
		trainning = new Record[TRAINNING_RECORD_NUM];
		test = new Record[TEST_RECORD_NUM];
		ave_r_u = new double[USER_NUM + 1];
		ave_r_i = new double[ITEM_NUM + 1];
		b_u = new double[USER_NUM + 1];
		r_u_i = new double[USER_NUM + 1][ITEM_NUM + 1];
		b_i = new double [ITEM_NUM + 1];
	}
	public static void main(String[] args) throws IOException {
		averageFilling aveFilling = new averageFilling();
		aveFilling.readIn();
		aveFilling.Initial();
		aveFilling.calAndShowError();
		aveFilling.savePreditedRating();
	}
	void Initial() {
		double u_sum_rating[] = new double [USER_NUM + 1];
		int itemNumOfU[] = new int[USER_NUM + 1];
		//int userNumOfI[] = new int[ITEM_NUM + 1];
		double sumOfRating = 0;
		for (Record tmp : trainning) {
			r_u_i[tmp.userID][tmp.itemID] = tmp.r_ui;
			u_sum_rating[tmp.userID] += tmp.r_ui;
			itemNumOfU[tmp.userID]++;
			sumOfRating += tmp.r_ui;
		}
		ave_r = sumOfRating / TRAINNING_RECORD_NUM;
		for (int u = 1; u <= USER_NUM; u++) {
			if (itemNumOfU[u] == 0) ave_r_u[u] = ave_r;
			else ave_r_u[u] = u_sum_rating[u] / itemNumOfU[u];
		}
		
		double i_sum_rating[] = new double [ITEM_NUM + 1];
		int userNumOfI[] = new int[ITEM_NUM + 1];
		for (Record tmp : trainning) {
			i_sum_rating[tmp.itemID] += tmp.r_ui;
			userNumOfI[tmp.itemID]++;
		}
		for (int i = 1; i <= ITEM_NUM; i++) {
			if (userNumOfI[i] == 0) ave_r_i[i] = ave_r;
			else ave_r_i[i] = i_sum_rating[i] / userNumOfI[i];
		}
		
		for (Record tmp : trainning) {
			b_u[tmp.userID] += tmp.r_ui - ave_r_i[tmp.itemID];
			b_i[tmp.itemID] += tmp.r_ui - ave_r_u[tmp.userID];
		}
		for (int u = 1; u <= USER_NUM; u++) {
			if (itemNumOfU[u] == 0) b_u[u] = 0;
			else b_u[u] /= itemNumOfU[u];
		}
		
		for (int i = 1; i <= ITEM_NUM; i++) {
			if (userNumOfI[i] == 0) b_i[i] = 0;
			else b_i[i] /= userNumOfI[i];
		}
	}
	void readIn() throws IOException {
		File baseFile = new File(BASE_PATH) ;
		FileInputStream baseFileIn = new FileInputStream(baseFile);
		InputStreamReader baseIn = new InputStreamReader(baseFileIn);
		BufferedReader baseReader =  new BufferedReader(baseIn);
		for (int i = 0; i < TRAINNING_RECORD_NUM; i++) {
			String data[] = baseReader.readLine().split("\\s+");
			Record newRecord = new Record(Integer.valueOf(data[0]), Integer.valueOf(data[1]), Double.valueOf(data[2]));
			trainning[i] = newRecord;
		}
		baseReader.close();
		baseIn.close();
		baseFileIn.close();
		
		File testFile = new File(TEST_PATH) ;
		FileInputStream testFileIn = new FileInputStream(testFile);
		InputStreamReader testIn = new InputStreamReader(testFileIn);
		BufferedReader testReader =  new BufferedReader(testIn);
		for (int i = 0; i < TEST_RECORD_NUM; i++) {
			String data[] = testReader.readLine().split("\\s+");
			Record newRecord = new Record(Integer.valueOf(data[0]), Integer.valueOf(data[1]), Double.valueOf(data[2]));
			test[i] = newRecord;
		}
		testReader.close();
		testIn.close();
		testFileIn.close();
		
	}
	void calAndShowError() {
		
		
		double MAE = 0.0, RMSE = 0.0;
		
		double errorSum_UserAve = 0.0;
		double squareSum_UserAve = 0.0;
		double error_UserAve = 0.0;
		
		double errorSum_ItemAve = 0.0;
		double squareSum_ItemAve = 0.0;
		double error_ItemAve = 0.0;
		
		double errorSum_M_u_i = 0.0;
		double squareSum_M_u_i = 0.0;
		double error_M_u_i = 0.0;
	
		double errorSum_UB_IA = 0.0;
		double squareSum_UB_IA = 0.0;
		double error_UB_IA = 0.0;
		
		double errorSum_UA_IB = 0.0;
		double squareSum_UA_IB = 0.0;
		double error_UA_IB = 0.0;
		
		double errorSum_GA_UB_IB = 0.0;
		double squareSum_GA_UB_IB = 0.0;
		double error_GA_UB_IB = 0.0;
		for (Record tmp : test) {
			error_UserAve = Math.abs(tmp.r_ui - ave_r_u[tmp.userID]);
			errorSum_UserAve += error_UserAve;
			squareSum_UserAve += error_UserAve * error_UserAve;
			
			error_ItemAve = Math.abs(tmp.r_ui - ave_r_i[tmp.itemID]);
			errorSum_ItemAve += error_ItemAve;
			squareSum_ItemAve += error_ItemAve * error_ItemAve;
			
			error_M_u_i = Math.abs(tmp.r_ui - (ave_r_i[tmp.itemID] / 2 + ave_r_u[tmp.userID] / 2));
			errorSum_M_u_i += error_M_u_i;
			squareSum_M_u_i += error_M_u_i * error_M_u_i;
			 
			error_UB_IA = Math.abs(tmp.r_ui - (ave_r_i[tmp.itemID] + b_u[tmp.userID]));
			errorSum_UB_IA += error_UB_IA;
			squareSum_UB_IA += error_UB_IA * error_UB_IA;
			
			error_UA_IB = Math.abs(tmp.r_ui - (b_i[tmp.itemID] + ave_r_u[tmp.userID]));
			errorSum_UA_IB += error_UA_IB;
			squareSum_UA_IB += error_UA_IB * error_UA_IB;
			
			error_GA_UB_IB = Math.abs(tmp.r_ui - (ave_r + b_i[tmp.itemID] + b_u[tmp.userID]));
			errorSum_GA_UB_IB += error_GA_UB_IB;
			squareSum_GA_UB_IB += error_GA_UB_IB * error_GA_UB_IB;
		}
		MAE = (double)errorSum_UserAve / TEST_RECORD_NUM;
		RMSE = Math.sqrt((double)squareSum_UserAve / TEST_RECORD_NUM);
		String data = "user average\t" + "RMSE:" + RMSE + "\tMAE: " + MAE + '\n';
		
		MAE = (double)errorSum_ItemAve / TEST_RECORD_NUM;
		RMSE = Math.sqrt((double)squareSum_ItemAve / TEST_RECORD_NUM);
		data = data + "item average\t" + "RMSE:" + RMSE + "\tMAE: " + MAE + '\n';
		
		MAE = (double)errorSum_M_u_i / TEST_RECORD_NUM;
		RMSE = Math.sqrt((double)squareSum_M_u_i / TEST_RECORD_NUM);
		data = data + "mean of user average and item average\t" + "RMSE:" + RMSE + "\tMAE: " + MAE + '\n';
		
		MAE = (double)errorSum_UB_IA / TEST_RECORD_NUM;
		RMSE = Math.sqrt((double)squareSum_UB_IA / TEST_RECORD_NUM);
		data = data + "user bias and item average\t" + "RMSE:" + RMSE + "\tMAE: " + MAE + '\n';
		
		MAE = (double)errorSum_UA_IB / TEST_RECORD_NUM;
		RMSE = Math.sqrt((double)squareSum_UA_IB / TEST_RECORD_NUM);
		data = data + "user average and item bias\t" + "RMSE:" + RMSE + "\tMAE: " + MAE + '\n';
		
		MAE = (double)errorSum_GA_UB_IB / TEST_RECORD_NUM;
		RMSE = Math.sqrt((double)squareSum_GA_UB_IB / TEST_RECORD_NUM);
		data = data + "global average, user bias and item bias\t" + "RMSE:" + RMSE + "\tMAE: " + MAE + '\n';
		
		
		System.out.println(data);
	
	}
	void savePreditedRating() throws IOException {
		File f = new File(RESULT_PATH);
		if (!f.exists()) f.createNewFile();
		DecimalFormat df = new DecimalFormat("0.0");
		FileWriter fw = new FileWriter(f);
		BufferedWriter save = new BufferedWriter(fw);
		
		for (int u = 1; u <= USER_NUM; u++) {
			for (int i = 1; i <= ITEM_NUM; i++) {
				if (r_u_i[u][i] == 0) {
					double result = b_u[u] + ave_r_i[i];
					if (result < MIN_RATING) result = MIN_RATING;
					else if (result > MAX_RATING) result = MAX_RATING;
					save.write("" + df.format(result));
				}
				else save.write("" + r_u_i[u][i]);
				save.write("\t");
			}
			save.write("\n");
		}
		save.close();
	}
}
class Record {
	int userID;
	int itemID;
	double r_ui;
	Record(int userID, int itemID, double r_ui) {
		this.userID = userID;
		this.itemID = itemID;
		this.r_ui = r_ui;
	}
}