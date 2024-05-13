import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;
import java.io.*;

public class Make_Model_Mapper extends Mapper<LongWritable, Text, Text, Text>
{
  @Override
  public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
  {    
    String[] tokenList = value.toString().split(",+", -1);
    String thisYear = tokenList[0];
    String thisMake = tokenList[1];
    String thisModel = tokenList[2];
    String properties = tokenList[3];

    // Key is make + "," + model
    context.write(new Text(thisMake.toUpperCase() + "," + thisModel.toUpperCase()),
                  new Text("A" + thisYear + "," + properties)
                 );
  }
}