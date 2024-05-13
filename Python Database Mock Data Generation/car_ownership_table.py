from faker import Faker
import random

fake = Faker()
Faker.seed(random.seed())
id = 0

file = open("table3.csv", "a")
file.write("id,personID,carID,mileage,color" + '\n')

for _ in range(200000):
    personID = random.randrange(99999)
    carID = random.randrange(252)
    mileage = random.randrange(50000)
    color = fake.safe_color_name()
    file.write(str(id) + "," + str(personID) + ',' + str(carID) + ',' + str(mileage) + ',' + str(color) + '\n')
    id = id + 1

print("Done")
file.close()