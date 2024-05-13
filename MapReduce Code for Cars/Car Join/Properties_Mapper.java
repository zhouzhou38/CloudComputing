import java.io.IOException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;

public class Properties_Mapper extends Mapper<LongWritable, Text, Text, Text>
{
  @Override
  public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
  {    
      String[] tokenList = value.toString().split(",+", -1);
      String brandName = tokenList[0];
      String modelName = tokenList[1];
      String countryOfOrigin = tokenList[2];
      String msrp = tokenList[3];
      String askingPrice = tokenList[4];
      String displacementSize = tokenList[5];
      String numDoors = tokenList[6];
      String driveType = tokenList[7];
      String engineConfig = tokenList[8];
      String numCylinders = tokenList[9];
      String hp = tokenList[10];
      String numSeatRows = tokenList[11];
      String numSeats = tokenList[12];
      String transmission = tokenList[13];
      String isTurbocharged = tokenList[14];

      // Key is make + "," + model
      context.write(new Text(brandName.toUpperCase() + "," + modelName.toUpperCase()),
                    new Text("B" + countryOfOrigin + "," + msrp + ","
                            + askingPrice + "," + displacementSize + ","
                            + numDoors + "," + driveType + ","
                            + engineConfig + "," + numCylinders + ","
                            + hp + "," + numSeatRows + ","
                            + numSeats + "," + transmission + ","
                            + isTurbocharged)
                   );
  }
}