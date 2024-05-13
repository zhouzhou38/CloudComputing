import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Car
{
  public static void main(String[] args) throws Exception
  {
    System.setProperty("hadoop.home.dir", "E:\\Eclipse\\Sandbox\\Car\\hadoop-3.4.0");
    Job job = Job.getInstance();
    job.setJarByClass(Car.class);
    job.setJobName("Clean up CIS_Automotive_Kaggle_Sample.csv");

    String src = "E:\\Eclipse\\Sandbox\\Dataset\\CIS_Automotive_Kaggle_Sample.csv";
    String dst = "E:\\Eclipse\\Sandbox\\Dataset\\results";
    FileInputFormat.addInputPath(job, new Path(src));
    FileOutputFormat.setOutputPath(job, new Path(dst));

    job.setMapperClass(Car_Mapper.class);
    job.setReducerClass(Car_Reducer.class);
    job.setNumReduceTasks(1);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);

    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}