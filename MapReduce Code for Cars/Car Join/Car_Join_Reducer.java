import java.io.*;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class Car_Join_Reducer extends Reducer<Text, Text, Text, Text>
{
    @Override
    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
    {
        String firstTableInfo = "";
        String secondTableInfo = "";
        boolean hasA = false;
        boolean hasB = false;

        for (Text entry : values)
        {
            if (entry.toString().charAt(0) == 'A')
            {
                firstTableInfo = entry.toString().substring(1);
                hasA = true;
            }
            else if (entry.toString().charAt(0) == 'B')
            {
                secondTableInfo = entry.toString().substring(1);
                hasB = true;
            }
        }

        if (hasA && hasB && firstTableInfo.isEmpty() == false &&
            secondTableInfo.isEmpty() == false)
        {
          context.write(key, new Text(firstTableInfo + "," + secondTableInfo));
        } // end if
    }
}