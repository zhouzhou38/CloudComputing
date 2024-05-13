import java.io.IOException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class Car_Reducer extends Reducer<Text, Text, Text, Text>
{
    private String countryOfOrigin;
    private String msrp;
    private String askingPrice;
    private String displacementSize;
    private String numDoors;
    private String driveType;
    private String engineConfig;
    private String numCylinders;
    private String hp;
    private String numSeatRows;
    private String numSeats;
    private String transmission;
    private String isTurbocharged;
    private String vehicleType;

  @Override
  public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
  {
    // The things we care about
    countryOfOrigin = "NULL";
    msrp = "NULL";
    askingPrice = "NULL";
    displacementSize = "NULL";
    numDoors = "NULL";
    driveType = "NULL";
    engineConfig = "NULL";
    numCylinders = "NULL";
    hp = "NULL";
    numSeatRows = "NULL";
    numSeats = "NULL";
    transmission = "NULL";
    isTurbocharged = "NULL";
    vehicleType = "NULL";

    for (Text entry : values)
    {
        // Split the line into words
        String[] tokenList = entry.toString().trim().split(",+", -1);
        countryOfOrigin = tokenList[0];

        if (msrp.compareTo("NULL") == 0 && tokenList[1].compareTo("NULL") != 0 &&
            tokenList[1].compareTo("0") != 0)
        {
            msrp = tokenList[1];
        }
        if (askingPrice.compareTo("NULL") == 0 && tokenList[2].compareTo("NULL") != 0 &&
            tokenList[2].compareTo("0") != 0)
        {
            askingPrice = tokenList[2];
        }
        if (displacementSize.compareTo("NULL") == 0 && tokenList[3].compareTo("NULL") != 0)
        {
            displacementSize = tokenList[3];
        }
        if (numDoors.compareTo("NULL") == 0 && tokenList[4].compareTo("NULL") != 0)
        {
            numDoors = tokenList[4];
        }
        if (driveType.compareTo("NULL") == 0 && tokenList[5].compareTo("NULL") != 0)
        {
            driveType = tokenList[5];
        }
        if (engineConfig.compareTo("NULL") == 0 && tokenList[6].compareTo("NULL") != 0)
        {
            engineConfig = tokenList[6];
        }
        if (numCylinders.compareTo("NULL") == 0 && tokenList[7].compareTo("NULL") != 0)
        {
            numCylinders = tokenList[7];
        }
        if (hp.compareTo("NULL") == 0 && tokenList[8].compareTo("NULL") != 0)
        {
            hp = tokenList[8];
        }
        if (numSeatRows.compareTo("NULL") == 0 && tokenList[9].compareTo("NULL") != 0)
        {
            numSeatRows = tokenList[9];
        }
        if (numSeats.compareTo("NULL") == 0 && tokenList[10].compareTo("NULL") != 0)
        {
            numSeats = tokenList[10];
        }
        if (transmission.compareTo("NULL") == 0 && tokenList[11].compareTo("NULL") != 0)
        {
            transmission = tokenList[11];
        }
        if (isTurbocharged.compareTo("NULL") == 0 && tokenList[12].compareTo("NULL") != 0)
        {
            isTurbocharged = tokenList[12];
        }
        if (vehicleType.compareTo("NULL") == 0 && tokenList[13].compareTo("NULL") != 0)
        {
            vehicleType = tokenList[13];
        }

        // If we get all the attributes, we can break
        if (msrp.compareTo("NULL") != 0 &&
            askingPrice.compareTo("NULL") != 0 &&
            displacementSize.compareTo("NULL") != 0 &&
            numDoors.compareTo("NULL") != 0 &&
            driveType.compareTo("NULL") != 0 &&
            engineConfig.compareTo("NULL") != 0 &&
            numCylinders.compareTo("NULL") != 0 &&
            hp.compareTo("NULL") != 0 &&
            numSeatRows.compareTo("NULL") != 0 &&
            numSeats.compareTo("NULL") != 0 &&
            transmission.compareTo("NULL") != 0 &&
            isTurbocharged.compareTo("NULL") != 0 &&
            vehicleType.compareTo("NULL") != 0)
        {
            break;
        }
    }

    String value = countryOfOrigin + ","
                    + msrp + ","
                    + askingPrice + ","
                    + displacementSize + ","
                    + numDoors + ","
                    + driveType + ","
                    + engineConfig + ","
                    + numCylinders + ","
                    + hp + ","
                    + numSeatRows + ","
                    + numSeats + ","
                    + transmission + ","
                    + isTurbocharged + ","
                    + vehicleType;

    context.write(key, new Text(value));
  }
}