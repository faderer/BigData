package knn;

import java.io.IOException;  
import java.util.StringTokenizer;  

import org.apache.hadoop.conf.Configuration;  
import org.apache.hadoop.fs.Path;  
import org.apache.hadoop.io.Text;  
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapreduce.Job;  
import org.apache.hadoop.mapreduce.Mapper;  
import org.apache.hadoop.mapreduce.Reducer;  
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;  
import org.apache.hadoop.mapreduce.lib.input.FileSplit;  
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;  
import org.apache.hadoop.util.GenericOptionsParser;  
  


import java.util.Collections;
import java.util.Comparator;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class KNN  
{  
      
    public static class Dis_Label {
        public float dis;//距离
        public String label;//标签
    }

    public static class TokenizerMapper extends Mapper<Object, Text, Text, Text> {  
        private ArrayList<ArrayList<Float>> test = new ArrayList<ArrayList<Float>> ();
        
        protected void setup(org.apache.hadoop.mapreduce.Mapper<Object, Text, Text, Text>.Context context) throws java.io.IOException, InterruptedException {
            // load the test vectors
            FileSystem fs = FileSystem.get(context.getConfiguration());
            BufferedReader br = new BufferedReader(new InputStreamReader(fs.open(new Path(context.getConfiguration().get(
                    "org.niubility.learning.test", "/home/master/workspace/knn/src/knn/test_random.csv")))));
            String line = br.readLine();
            int count = 0;
            while (line != null) {
                String[] s = line.split(",");
                
                if (s.length <= 1){
                    break;
                }
        
                ArrayList<Float> testcase = new ArrayList<Float>();
                for (int i = 0; i < s.length-1; i++){
                    testcase.add(Float.parseFloat(s[i]));
                }
                test.add(testcase);
                line = br.readLine();
                count++;
            }
            System.out.println(test.size());
            br.close();
        }
        
        @Override  
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException  
        {  
            //key是训练数据行号
            context.setStatus(key.toString());
            String[] s = value.toString().split(",");
            String label = s[s.length - 1];        
            for (int i=0; i<test.size(); i++){
                ArrayList<Float> curr_test = test.get(i);
                double tmp = 0;
                for(int j=0; j<curr_test.size(); j++){
                    tmp += (curr_test.get(j) - Float.parseFloat(s[j]))*(curr_test.get(j) - Float.parseFloat(s[j]));
                }
                context.write(new Text(Integer.toString(i)), new Text(""+Double.toString(tmp)+","+label)); //测试样例编号,所有训练集距离&标签                 
            }

        }  
        
    }  

    public static class KNNCombiner extends Reducer<Text, Text, Text, Text>  
    {  
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException  
        {  
            ArrayList<Dis_Label> dis_Label_set = new ArrayList<Dis_Label>();

            for (Text value : values){
                String[] s = value.toString().split(","); //拆开所有 距离+标签

                Dis_Label tmp = new Dis_Label();

                try{
                    tmp.label = s[1];
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(value);
                }

                tmp.dis = Float.parseFloat(s[0]);
                dis_Label_set.add(tmp);
            }
            //排序 
            Collections.sort(dis_Label_set, new Comparator<Dis_Label>(){
                @Override
                public int compare(Dis_Label a, Dis_Label b){ 
                    if (a.dis > b.dis){
                        return 1; //小的在前
                    }
                    if (a.dis == b.dis){
                        return 0; 
                    }
                    return -1;
                }
            });

            final int k = 30; //K值

            for (int i=0; i<dis_Label_set.size() && i<k; i++){
                context.write(key, new Text(""+Double.toString(dis_Label_set.get(i).dis)+","+dis_Label_set.get(i).label));
            }
        }  
    }  

    public static class KNNReducer extends Reducer<Text, Text, Text, Text>  
    {  
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException  
        {  
            //KNN部分
            ArrayList<Dis_Label> dis_Label_set = new ArrayList<Dis_Label>();

            for (Text value : values){
                String[] s = value.toString().split(","); //拆开所有 距离+标签
                Dis_Label tmp = new Dis_Label();
                tmp.label = s[1];
                tmp.dis = Float.parseFloat(s[0]);
                dis_Label_set.add(tmp);
            }
            //排序 
            Collections.sort(dis_Label_set, new Comparator<Dis_Label>(){
                @Override
                public int compare(Dis_Label a, Dis_Label b){ 
                    if (a.dis > b.dis){
                        return 1; //小的在前
                    }
                    if (a.dis == b.dis){
                        return 0; 
                    }
                    return -1;
                }
            });

            HashMap<String, Integer> ans = new HashMap<String, Integer>();
            int count = 0;
            final int k = 15; //K值

            for (int i=0; i<dis_Label_set.size() && i<k; i++){
                String val = dis_Label_set.get(i).label;
                if (!ans.containsKey(val)){
                    ans.put(val.toString(), 0);
                }
                ans.put(val.toString(), ans.get(val.toString())+1); 
            }

            int mx = -1;
            String ansLabel = "";
            for (String l:ans.keySet()){
                if (mx < ans.get(l)){
                    mx = ans.get(l);
                    ansLabel = l;
                }
            }   
            context.write(key, new Text(ansLabel));  
        }
    }  
    public static double calculate_accuary(String groundtruth, String res){
    	Double acc = 0.0;
    	try{
    	 FileInputStream fis = new FileInputStream(groundtruth); 
         InputStreamReader isr = new InputStreamReader(fis, "UTF-8"); 
         BufferedReader br = new BufferedReader(isr); 
         String line = null; 
         int idx=0;
         Map<Integer,Integer> groundtruth_map=new HashMap<>();
         while ((line = br.readLine()) != null) { 
        	 line=line.trim();
        	 String[] s = line.split(",");
        	 if(s.length != 38){
        		 continue;
        	 }
        	  int label=Integer.parseInt(s[s.length-1]);
        	  groundtruth_map.put(idx,label);
        	  idx++;
         }
         
    	 fis = new FileInputStream(res); 
         isr = new InputStreamReader(fis, "UTF-8"); 
         br = new BufferedReader(isr); 
         line = null; 
         int correct=0;
         int sum=0;
         while ((line = br.readLine()) != null) { 
        	 sum++;
        	  String[] s = line.split("\\s+");
        	  int index=Integer.parseInt(s[0]);
        	  int label=Integer.parseInt(s[s.length - 1]);
        	  if(groundtruth_map.get(index) == label){
        		  correct++;
        		  
        	  }  
         }
         
         acc=(0.0+correct)/(0.0+groundtruth_map.size());
    	}
    	catch(Exception e){
    		System.out.println(e);
    	}
    	
    	return acc;
    }  
    public static void main(String[] args) throws Exception  
    {  
        Configuration conf=new Configuration();  

        Path inputPath=new Path("/home/master/workspace/knn/src/knn/train_random.csv");  
        Path outputPath=new Path("/home/master/workspace/knn/src/knn/output");  
        outputPath.getFileSystem(conf).delete(outputPath, true);  
          
        Job job=Job.getInstance(conf, "KNN");  
        job.setJarByClass(KNN.class);  
          
        job.setMapperClass(TokenizerMapper.class);  

        job.setMapOutputKeyClass(Text.class);  
        job.setMapOutputValueClass(Text.class);  
           
        job.setCombinerClass(KNNCombiner.class);  
          
        job.setReducerClass(KNNReducer.class);          
        job.setOutputKeyClass(Text.class);  
        job.setOutputValueClass(Text.class);  
          
        FileInputFormat.addInputPath(job, inputPath);  
        FileOutputFormat.setOutputPath(job, outputPath);  
        
        long startTime = System.currentTimeMillis();
        job.waitForCompletion(true);
        double runtime = (System.currentTimeMillis() - startTime) / 1000.0;
        System.out.println("runtime：" + runtime + " s");
        System.out.println(String.format("Acc: %f ",
                (KNN.calculate_accuary("/home/master/workspace/knn/src/knn/test_random.csv", outputPath.toString()+"/part-r-00000"))));
    }  
      
}  
