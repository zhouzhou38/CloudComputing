import java.io.IOException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;

public class Car_Mapper extends Mapper<LongWritable, Text, Text, Text>
{
  private String DetermineCountryOfOrigin(String make)
  {
    String country = "";
    switch (make.toUpperCase().trim())
    {
    case "ACURA":
    case "HONDA":
    case "SUBARU":
    case "TOYOTA":
    case "LEXUS":
    case "MITSUBISHI":
    case "INFINITI":
    case "NISSAN":
    case "MAZDA":
    case "DATSUN":
    case "ISUZU":
        country = "Japanese";
    break;

    case "ALFA ROMEO":
    case "FIAT":
    case "LAMBORGHINI":
    case "FERRARI":
    case "MASERATI":
        country = "Italian";
    break;

    case "ASTON MARTIN":
    case "BENTLEY":
    case "LOTUS":
    case "JAGUAR":
    case "LAND ROVER":
    case "ROLLS-ROYCE":
    case "MCLAREN":
    case "MINI":
        country = "British";
    break;

    case "AUDI":
    case "BMW":
    case "MERCEDES-BENZ":
    case "PORSCHE":
    case "VOLKSWAGEN":
    case "OPEL":
    case "SMART":
        country = "German";
    break;

    case "POLESTAR":
    case "VOLVO":
    case "SAAB":
        country = "Swedish";
    break;

    case "KIA":
    case "HYUNDAI":
    case "GENESIS":
    case "DAEWOO":
        country = "Korean";
    break;

    default:
        country = "American";
    }

    return country;
  }

  private String CleanUp(Text _inputValue)
  {
    String newText = _inputValue.toString().replaceAll(",,", ",NULL,").replaceAll(",,", ",NULL,");
    
    if (newText.charAt(newText.length() - 1) == ',')
    {
        newText = newText.substring(0, newText.length() - 1);
    }

    return newText;
  }

  private boolean CheckBrandNameValid(String brandName)
  {
      boolean result = true;

      switch (brandName.toUpperCase())
      {
      case "NULL":
      case "BAD BOY ENTERPRISES":
      case "CAN-AM":
      case "COASTLINE TRAILER MFG":
      case "COBRA INDUSTRIES":
      case "CRUISERS MFG INC (CMI)":
      case "DODGE CHRYSLER FIAT LANCIA":
      case "DODGE CHRYSLER VOLKSWAGEN JEEP FIAT RAM LANCIA":
      case "FABFORM INDUSTRIES":
      case "FELLING TRAILERS":
      case "FIAT RAM LANCIA DODGE CHRYSLER VOLKSWAGEN JEEP":
      case "FIRST UNITED INDUSTRIAL FOSHAN":
      case "FONTAINE TRAILER CO.":
      case "FOREST RIVER":
      case "FREIGHTLINER":
      case "GEM":
      case "HARLEY DAVIDSON":
      case "HIGHLAND":
      case "HILLSBORO":
      case "HINO":
      case "HLT TRAILERS":
      case "HOLDEN OLDSMOBILE":
      case "HYOSUNG MOTORS & MACHINERY":
      case "HYUNDAI GENESIS":
      case "HYUNDAI KIA":
      case "INDIAN MOTORCYCLE":
      case "INTERNATIONAL":
      case "JEEP FIAT RAM":
      case "KAWASAKI":
      case "KOUNTRY AIRE":
      case "KOUNTRY LITE":
      case "KTMMEX":
      case "MACK":
      case "MITSUBISHI FUSO":
      case "NISSAN INFINITI DATSUN":
      case "NISSAN INFINITI":
      case "OKT":
      case "POLAR TANK TRAILER":
      case "QUALITY CARGO LLC":
      case "ROYALS":
      case "SHERMAN + REILLY":
      case "SPORTSCOACH":
      case "SPRINTER (DODGE OR FREIGHTLINER)":
      case "SUZUKI KAWASAKI":
      case "SUZUKI":
      case "SYM":
      case "VICTORY":
      case "WORKHORSE":
      case "YAMAHA":
      case "AIRSTREAM INC.":
          result = false;
      break;
      
      default:
          result = true;
      }

      return result;
  }

  @Override
  public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
  {
    // Split the line into words
    String[] tokenList = CleanUp(value).toString().trim().split(",+", -1);

    // This dataset was taken from https://www.kaggle.com/datasets/cisautomotiveapi/large-car-dataset
    // The columns we are interested in are:
    // 4. msrp
    // 5. askingPrice
    // 10. brandName
    // 11. modelName
    // 62. vf_displacementL
    // 63. vf_doors
    // 64. vf_driveType
    // 71. vf_engineConfiguration
    // 72. vf_engineCylinders
    // 74. vf_engineHP
    // 127. vf_seatRows
    // 128. vf_Seats
    // 142. vf_transmissionStyle
    // 145. vf_Turbo
    // 148. vf_vehicleType
    String thisMSRP = tokenList[4]; 

    // Skip the first line as it only has all of the column headers
    if (thisMSRP.compareTo("msrp") != 0)
    {
      String msrp = tokenList[4];
      String askingPrice = tokenList[5];
      String brandName = tokenList[10];
      String modelName = tokenList[11];
      String displacementSize = tokenList[62];
      String numDoors = tokenList[63];
      String driveType = tokenList[64];
      String engineConfig = tokenList[71];
      String numCylinders = tokenList[72];
      String hp = tokenList[74];
      String numSeatRows = tokenList[127];
      String numSeats = tokenList[128];
      String transmission = tokenList[142];
      String isTurbocharged = tokenList[145];
      String vehicleType = tokenList[148];

      if (CheckBrandNameValid(brandName) &&
          modelName.compareTo("NULL") != 0)
      {
         String countryOfOrigin = DetermineCountryOfOrigin(brandName);
         Text newKey = new Text(brandName + "," + modelName);
         Text newValue = new Text(countryOfOrigin + ","
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
                                 + vehicleType);
         context.write(newKey, newValue);
      }
    }
  }
}