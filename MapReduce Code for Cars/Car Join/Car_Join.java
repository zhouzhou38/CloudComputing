import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Car_Join
{
  public static void main(String[] args) throws Exception
  {
    System.setProperty("hadoop.home.dir", "E:\\Eclipse\\Sandbox\\Car\\hadoop-3.4.0");
    
    Job job = Job.getInstance();
    job.setJarByClass(Car_Join.class);
    job.setJobName("Join car data tables");

    String src1 = "E:\\Eclipse\\Sandbox\\Dataset\\Make_Model.csv";
    String src2 = "E:\\Eclipse\\Sandbox\\Dataset\\Properties.csv";
    String dst = "E:\\Eclipse\\Sandbox\\Dataset\\joinResults";
    MultipleInputs.addInputPath(job, new Path(src1), TextInputFormat.class, Make_Model_Mapper.class);
    MultipleInputs.addInputPath(job, new Path(src2), TextInputFormat.class, Properties_Mapper.class);

    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    job.setReducerClass(Car_Join_Reducer.class);
    job.setNumReduceTasks(1);
    FileOutputFormat.setOutputPath(job, new Path(dst));

    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}