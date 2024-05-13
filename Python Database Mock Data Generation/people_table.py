from faker import Faker
import random

fake = Faker()
Faker.seed(random.seed())
id = 0

file = open("table1.csv", "a")
file.write("id,name,address,city,state,zipCode" + '\n')

for _ in range(100000):
    name = fake.name()
    address = fake.street_address()
    randLine = open('zip.csv').read().splitlines()
    cityStateZip = random.choice(randLine)
    file.write(str(id) + "," + name + ',' + address + ',' + cityStateZip + '\n')
    # print(str(id) + "," + name + ',' + address + ',' + cityStateZip)
    id = id + 1

print("Done")
file.close()